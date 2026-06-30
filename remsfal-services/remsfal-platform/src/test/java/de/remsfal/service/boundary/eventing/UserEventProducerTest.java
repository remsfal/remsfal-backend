package de.remsfal.service.boundary.eventing;

import de.remsfal.core.json.eventing.UserEventJson;
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
class UserEventProducerTest extends AbstractKafkaTest {

    @Inject
    UserEventProducer producer;

    @Override
    @BeforeEach
    protected void clearAllTopics() {
        companion.topics().clearIfExists(UserEventJson.TOPIC);
    }

    @Test
    void testSendUserDeleted_publishesEventToTopic() {
        producer.sendUserDeleted(TestData.USER_ID);

        given()
            .topic(UserEventJson.TOPIC)
        .assertThat()
            .json("userId", Matchers.equalTo(TestData.USER_ID.toString()))
            .json("userEventType", Matchers.equalTo("USER_DELETED"));
    }

}
