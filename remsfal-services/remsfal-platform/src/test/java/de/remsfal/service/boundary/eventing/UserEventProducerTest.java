package de.remsfal.service.boundary.eventing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.AffectedTenantJson;
import de.remsfal.core.json.eventing.ImmutableAffectedTenantJson;
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

    @Test
    void testSendUserUpdated_publishesEventToTopic() {
        final UserJson updatedProfile = ImmutableUserJson.builder()
            .firstName(TestData.USER_FIRST_NAME)
            .lastName(TestData.USER_LAST_NAME)
            .build();
        final List<AffectedTenantJson> affectedTenants = List.of(
            ImmutableAffectedTenantJson.builder()
                .tenantId(TestData.TENANT_ID)
                .projectId(TestData.PROJECT_ID)
                .build());

        producer.sendUserUpdated(TestData.USER_ID, updatedProfile, affectedTenants);

        given()
            .topic(UserEventJson.TOPIC)
        .assertThat()
            .json("userId", Matchers.equalTo(TestData.USER_ID.toString()))
            .json("userEventType", Matchers.equalTo("USER_UPDATED"));
    }

    @Test
    void testSendUserUpdated_userIdNull_skipsSendingEvent() {
        producer.sendUserUpdated(null, null, List.of());

        final List<?> records = companion.consumeStrings()
            .fromTopics(UserEventJson.TOPIC)
            .awaitNoRecords(Duration.ofSeconds(2))
            .getRecords();
        assertTrue(records.isEmpty());
    }

}
