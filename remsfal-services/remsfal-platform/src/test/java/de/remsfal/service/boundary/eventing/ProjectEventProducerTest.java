package de.remsfal.service.boundary.eventing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import de.remsfal.core.json.eventing.ProjectEventJson;
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
class ProjectEventProducerTest extends AbstractKafkaTest {

    @Inject
    ProjectEventProducer producer;

    @Override
    @BeforeEach
    protected void clearAllTopics() {
        companion.topics().clearIfExists(ProjectEventJson.TOPIC);
    }

    @Test
    void testSendProjectDeleted_publishesEventToTopic() {
        producer.sendProjectDeleted(TestData.PROJECT_ID);

        given()
            .topic(ProjectEventJson.TOPIC)
        .assertThat()
            .json("projectId", Matchers.equalTo(TestData.PROJECT_ID.toString()))
            .json("projectEventType", Matchers.equalTo("PROJECT_DELETED"));
    }

    @Test
    void testSendRentalAgreementDeleted_publishesEventToTopic() {
        producer.sendRentalAgreementDeleted(TestData.PROJECT_ID, TestData.AGREEMENT_ID);

        given()
            .topic(ProjectEventJson.TOPIC)
        .assertThat()
            .json("projectId", Matchers.equalTo(TestData.PROJECT_ID.toString()))
            .json("agreementId", Matchers.equalTo(TestData.AGREEMENT_ID.toString()))
            .json("projectEventType", Matchers.equalTo("RENTAL_AGREEMENT_DELETED"));
    }

    @Test
    void testSendProjectDeleted_projectIdNull_skipsSendingEvent() {
        producer.sendProjectDeleted(null);

        final List<?> records = companion.consumeStrings()
            .fromTopics(ProjectEventJson.TOPIC)
            .awaitNoRecords(Duration.ofSeconds(2))
            .getRecords();
        assertTrue(records.isEmpty());
    }

    @Test
    void testSendRentalAgreementDeleted_agreementIdNull_skipsSendingEvent() {
        producer.sendRentalAgreementDeleted(TestData.PROJECT_ID, null);

        final List<?> records = companion.consumeStrings()
            .fromTopics(ProjectEventJson.TOPIC)
            .awaitNoRecords(Duration.ofSeconds(2))
            .getRecords();
        assertTrue(records.isEmpty());
    }

}
