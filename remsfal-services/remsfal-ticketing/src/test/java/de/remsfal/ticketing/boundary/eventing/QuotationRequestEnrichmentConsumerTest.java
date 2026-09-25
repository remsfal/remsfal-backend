package de.remsfal.ticketing.boundary.eventing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.awaitility.Awaitility;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.json.ImmutableAddressJson;
import de.remsfal.core.json.eventing.ImmutableIssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.json.project.ImmutableProjectJson;
import de.remsfal.core.json.project.ImmutableRentalAgreementJson;
import de.remsfal.core.json.project.ImmutableRentalUnitNodeDataJson;
import de.remsfal.core.json.project.ImmutableTenantJson;
import de.remsfal.core.json.ticketing.ImmutableIssueJson;
import de.remsfal.core.json.ticketing.ImmutableQuotationRequestJson;
import de.remsfal.core.model.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
@QuarkusTestResource(CassandraTestResource.class)
class QuotationRequestEnrichmentConsumerTest {

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Inject
    CqlSession cqlSession;

    @BeforeEach
    void setup() {
        companion = new KafkaCompanion(
            ConfigProvider.getConfig().getValue("quarkus.kafka.bootstrap-servers", String.class));
        companion.topics().clearIfExists(IssueEventJson.TOPIC_ENRICHED);
        companion.registerSerde(ImmutableIssueEventJson.class,
            new ObjectMapperSerde<>(ImmutableIssueEventJson.class));
    }

    @Test
    void consume_quotationRequestCreated_enrichesRequest() {
        final UUID issueId = UUID.randomUUID();
        final UUID requestId = UUID.randomUUID();
        insertQuotationRequest(issueId, requestId);

        send(buildEvent(IssueEventType.QUOTATION_REQUEST_CREATED, issueId, requestId));

        Awaitility.await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                final Row row = selectQuotationRequest(issueId, requestId);
                assertEquals("Mustermann Verwaltung GmbH", row.getString("project_owner"));
                assertEquals("Max Mustermann", row.getString("project_care_of"));
                assertEquals("Musterstraße 1", row.getString("project_billing_address_1"));
                assertEquals("10115 Berlin", row.getString("project_billing_address_2"));
                assertEquals("Berlin, DE", row.getString("project_billing_address_3"));
                assertEquals("Hauptstraße 5", row.getString("place_of_performance_address_1"));
                assertEquals("14467 Potsdam", row.getString("place_of_performance_address_2"));
                assertEquals("Brandenburg, DE", row.getString("place_of_performance_address_3"));
                assertEquals("Wohnung 3.2", row.getString("rental_unit_title"));
                assertEquals("3. OG links", row.getString("rental_unit_location"));
                final List<String> tenants = row.getList("tenants", String.class);
                assertEquals(1, tenants.size());
                assertTrue(tenants.get(0).contains("\"lastName\":\"Musterfrau\""));
                assertTrue(tenants.get(0).contains("\"email\":\"erika@example.com\""));
            });
    }

    @Test
    void consume_otherEventType_leavesRequestUntouched() {
        final UUID issueId = UUID.randomUUID();
        final UUID requestId = UUID.randomUUID();
        insertQuotationRequest(issueId, requestId);

        send(buildEvent(IssueEventType.QUOTATION_REQUEST_STATUS_CHANGED, issueId, requestId));
        // a subsequent marker event proves the preceding one has been consumed
        final UUID markerIssueId = UUID.randomUUID();
        final UUID markerRequestId = UUID.randomUUID();
        insertQuotationRequest(markerIssueId, markerRequestId);
        send(buildEvent(IssueEventType.QUOTATION_REQUEST_CREATED, markerIssueId, markerRequestId));

        Awaitility.await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> assertEquals("Mustermann Verwaltung GmbH",
                selectQuotationRequest(markerIssueId, markerRequestId).getString("project_owner")));

        final Row row = selectQuotationRequest(issueId, requestId);
        assertNull(row.getString("project_owner"));
        assertNull(row.getString("place_of_performance_address_1"));
        assertNull(row.getString("rental_unit_title"));
    }

    private void send(final ImmutableIssueEventJson event) {
        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();
    }

    private ImmutableIssueEventJson buildEvent(final IssueEventType type, final UUID issueId,
        final UUID requestId) {
        final UUID projectId = UUID.randomUUID();
        return ImmutableIssueEventJson.builder()
            .issueEventType(type)
            .issueId(issueId)
            .issue(ImmutableIssueJson.builder()
                .id(issueId)
                .projectId(projectId)
                .title("Wasserschaden")
                .type(IssueType.DEFECT)
                .status(IssueStatus.OPEN)
                .build())
            .project(ImmutableProjectJson.builder()
                .id(projectId)
                .title("Musterprojekt")
                .owner("Mustermann Verwaltung GmbH")
                .careOf("Max Mustermann")
                .billingAddress(ImmutableAddressJson.builder()
                    .street("Musterstraße 1")
                    .city("Berlin")
                    .province("Berlin")
                    .zip("10115")
                    .countryCode("DE")
                    .build())
                .build())
            .rentalAgreement(ImmutableRentalAgreementJson.builder()
                .id(UUID.randomUUID())
                .tenants(List.of(ImmutableTenantJson.builder()
                    .firstName("Erika")
                    .lastName("Musterfrau")
                    .email("erika@example.com")
                    .build()))
                .build())
            .rentalUnit(ImmutableRentalUnitNodeDataJson.builder()
                .id(UUID.randomUUID())
                .type(UnitType.APARTMENT)
                .title("Wohnung 3.2")
                .location("3. OG links")
                .build())
            .placeOfPerformance(ImmutableAddressJson.builder()
                .street("Hauptstraße 5")
                .city("Potsdam")
                .province("Brandenburg")
                .zip("14467")
                .countryCode("DE")
                .build())
            .quotationRequest(ImmutableQuotationRequestJson.builder()
                .id(requestId)
                .issueId(issueId)
                .build())
            .build();
    }

    private void insertQuotationRequest(final UUID issueId, final UUID requestId) {
        cqlSession.execute("INSERT INTO remsfal.quotation_requests"
                + " (issue_id, request_id, project_id, contractor_id, status, created_at, modified_at)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?)",
            issueId, requestId, UUID.randomUUID(), UUID.randomUUID(), "REQUESTED", Instant.now(), Instant.now());
    }

    private Row selectQuotationRequest(final UUID issueId, final UUID requestId) {
        return cqlSession.execute("SELECT * FROM remsfal.quotation_requests WHERE issue_id = ? AND request_id = ?",
            issueId, requestId).one();
    }

}
