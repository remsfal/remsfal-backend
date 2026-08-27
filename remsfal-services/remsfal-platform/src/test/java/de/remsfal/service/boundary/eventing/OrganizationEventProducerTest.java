package de.remsfal.service.boundary.eventing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import de.remsfal.core.json.eventing.AffectedContractorJson;
import de.remsfal.core.json.eventing.ImmutableAffectedContractorJson;
import de.remsfal.core.json.eventing.OrganizationEventJson;
import de.remsfal.core.json.organization.ImmutableOrganizationJson;
import de.remsfal.core.json.organization.OrganizationJson;
import de.remsfal.test.TestData;
import de.remsfal.test.kafka.AbstractKafkaTest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.KafkaCompanionResource;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class OrganizationEventProducerTest extends AbstractKafkaTest {

    @Inject
    OrganizationEventProducer producer;

    @Override
    @BeforeEach
    protected void clearAllTopics() {
        companion.topics().clearIfExists(OrganizationEventJson.TOPIC);
    }

    @Test
    void testSendOrganizationUpdated_publishesEventToTopic() {
        final OrganizationJson updatedProfile = ImmutableOrganizationJson.builder()
            .name(TestData.ORGANIZATION_NAME)
            .email(TestData.ORGANIZATION_EMAIL)
            .build();
        final List<AffectedContractorJson> affectedContractors = List.of(
            ImmutableAffectedContractorJson.builder()
                .contractorId(TestData.USER_ID)
                .projectId(TestData.PROJECT_ID)
                .build());

        producer.sendOrganizationUpdated(TestData.ORGANIZATION_ID, updatedProfile, affectedContractors,
            TestData.USER_ID, TestData.USER_NAME);

        given()
            .topic(OrganizationEventJson.TOPIC)
        .assertThat()
            .json("organizationId", Matchers.equalTo(TestData.ORGANIZATION_ID.toString()))
            .json("organizationEventType", Matchers.equalTo("ORGANIZATION_UPDATED"));
    }

    @Test
    void testSendOrganizationUpdated_organizationIdNull_skipsSendingEvent() {
        producer.sendOrganizationUpdated(null, null, List.of(), null, null);

        final List<?> records = companion.consumeStrings()
            .fromTopics(OrganizationEventJson.TOPIC)
            .awaitNoRecords(Duration.ofSeconds(2))
            .getRecords();
        assertTrue(records.isEmpty());
    }

}
