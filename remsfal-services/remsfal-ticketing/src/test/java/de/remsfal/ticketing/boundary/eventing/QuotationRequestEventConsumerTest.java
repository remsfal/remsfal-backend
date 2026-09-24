package de.remsfal.ticketing.boundary.eventing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.quarkus.test.CassandraTestResource;
import de.remsfal.core.json.eventing.ImmutableIssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.json.project.ImmutableRentalAgreementJson;
import de.remsfal.core.json.project.ImmutableTenantJson;
import de.remsfal.core.json.project.RentalAgreementJson;
import de.remsfal.core.json.ticketing.ImmutableQuotationRequestJson;
import de.remsfal.core.model.ticketing.QuotationRequestModel.RequestStatus;
import de.remsfal.ticketing.entity.dao.QuotationRequestRepository;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;
import de.remsfal.ticketing.entity.dto.QuotationRequestKey;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.awaitility.Awaitility;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
@QuarkusTestResource(CassandraTestResource.class)
class QuotationRequestEventConsumerTest {

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Inject
    QuotationRequestRepository repository;

    @Inject
    CqlSession cqlSession;

    @BeforeEach
    void setup() {
        Config config = ConfigProvider.getConfig();
        String bootstrapServers = config.getValue("quarkus.kafka.bootstrap-servers", String.class);
        companion = new KafkaCompanion(bootstrapServers);
        companion.topics().clearIfExists(IssueEventJson.TOPIC_ENRICHED);
        companion.registerSerde(ImmutableIssueEventJson.class,
            new ObjectMapperSerde<>(ImmutableIssueEventJson.class));

        cqlSession.execute("TRUNCATE quotation_requests");
    }

    @Test
    void consume_quotationRequestCreated_storesTenantContacts() {
        final QuotationRequestEntity request = insertRequest();
        final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
            .id(UUID.randomUUID())
            .addTenants(ImmutableTenantJson.builder()
                .firstName("Max")
                .lastName("Mustermann")
                .privatePhoneNumber("030 123456")
                .mobilePhoneNumber("0170 1234567")
                .email("max@example.de")
                .build())
            .addTenants(ImmutableTenantJson.builder()
                .firstName("Erika")
                .lastName("Musterfrau")
                .email("erika@example.de")
                .build())
            .build();

        produce(IssueEventType.QUOTATION_REQUEST_CREATED, request, agreement);

        Awaitility.await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> assertEquals(
                List.of("Max Mustermann, Tel. 0170 1234567, max@example.de", "Erika Musterfrau, erika@example.de"),
                reload(request).getTenants()));
    }

    @Test
    void consume_otherEventType_leavesRequestUnchanged() {
        final QuotationRequestEntity request = insertRequest();
        final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
            .addTenants(ImmutableTenantJson.builder().firstName("Max").lastName("Mustermann").build())
            .build();

        produce(IssueEventType.QUOTATION_REQUEST_STATUS_CHANGED, request, agreement);
        // a subsequent relevant event proves the consumer has processed the preceding one
        final QuotationRequestEntity marker = insertRequest();
        produce(IssueEventType.QUOTATION_REQUEST_CREATED, marker, agreement);

        Awaitility.await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> assertEquals(List.of("Max Mustermann"), reload(marker).getTenants()));
        // Cassandra does not distinguish between an empty and an unset collection
        assertEquals(List.of(), reload(request).getTenants());
    }

    @Test
    void consume_withoutRentalAgreement_leavesTenantsEmpty() {
        final QuotationRequestEntity request = insertRequest();
        produce(IssueEventType.QUOTATION_REQUEST_CREATED, request, null);

        final QuotationRequestEntity marker = insertRequest();
        produce(IssueEventType.QUOTATION_REQUEST_CREATED, marker, ImmutableRentalAgreementJson.builder()
            .addTenants(ImmutableTenantJson.builder().lastName("Marker").build())
            .build());

        Awaitility.await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> assertEquals(List.of("Marker"), reload(marker).getTenants()));
        // Cassandra does not distinguish between an empty and an unset collection
        assertEquals(List.of(), reload(request).getTenants());
    }

    @Test
    void consume_unknownQuotationRequest_isAcknowledged() {
        final QuotationRequestEntity unknown = new QuotationRequestEntity();
        unknown.setIssueId(UUID.randomUUID());
        unknown.generateId();
        final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
            .addTenants(ImmutableTenantJson.builder().lastName("Mustermann").build())
            .build();
        produce(IssueEventType.QUOTATION_REQUEST_CREATED, unknown, agreement);

        final QuotationRequestEntity marker = insertRequest();
        produce(IssueEventType.QUOTATION_REQUEST_CREATED, marker, agreement);

        Awaitility.await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> assertEquals(List.of("Mustermann"), reload(marker).getTenants()));
    }

    private QuotationRequestEntity insertRequest() {
        final QuotationRequestEntity request = new QuotationRequestEntity();
        request.setIssueId(UUID.randomUUID());
        request.generateId();
        request.setProjectId(UUID.randomUUID());
        request.setContractorId(UUID.randomUUID());
        request.setOrganizationId(UUID.randomUUID());
        request.setStatus(RequestStatus.REQUESTED);
        return repository.insert(request);
    }

    private QuotationRequestEntity reload(final QuotationRequestEntity request) {
        final QuotationRequestKey key = new QuotationRequestKey();
        key.setIssueId(request.getIssueId());
        key.setRequestId(request.getRequestId());
        return repository.findById(key).orElseThrow();
    }

    private void produce(final IssueEventType type, final QuotationRequestEntity request,
        final RentalAgreementJson agreement) {
        final ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(type)
            .issueId(request.getIssueId())
            .quotationRequest(ImmutableQuotationRequestJson.builder()
                .id(request.getRequestId())
                .issueId(request.getIssueId())
                .build())
            .rentalAgreement(agreement)
            .build();
        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();
    }

}
