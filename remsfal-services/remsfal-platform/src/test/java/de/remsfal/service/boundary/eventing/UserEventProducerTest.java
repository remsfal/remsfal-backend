package de.remsfal.service.boundary.eventing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.UserEventJson;
import de.remsfal.service.control.event.UserEvent;
import de.remsfal.test.TestData;
import de.remsfal.test.kafka.AbstractKafkaTest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.KafkaCompanionResource;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Fires the CDI events without a transaction, so that the {@code AFTER_SUCCESS} observers are notified immediately.
 */
@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class UserEventProducerTest extends AbstractKafkaTest {

    @Inject
    Event<UserEvent> userEvent;

    @Override
    @BeforeEach
    protected void clearAllTopics() {
        companion.topics().clearIfExists(UserEventJson.TOPIC);
    }

    private static UserJson user() {
        return ImmutableUserJson.builder()
            .id(TestData.USER_ID)
            .email(TestData.USER_EMAIL)
            .firstName(TestData.USER_FIRST_NAME)
            .lastName(TestData.USER_LAST_NAME)
            .build();
    }

    @Test
    void onUserDeleted_publishesEventWithUser() {
        userEvent.fire(new UserEvent.Deleted(user(), TestData.USER_ID));

        given()
            .topic(UserEventJson.TOPIC)
        .assertThat()
            .json("userId", Matchers.equalTo(TestData.USER_ID.toString()))
            .json("userEventType", Matchers.equalTo("USER_DELETED"))
            .json("user.email", Matchers.equalTo(TestData.USER_EMAIL))
            .json("eventId", Matchers.notNullValue(String.class));
    }

    @Test
    void onContactDataChanged_publishesUserUpdated() {
        userEvent.fire(new UserEvent.ContactDataChanged(user(), TestData.USER_ID));

        given()
            .topic(UserEventJson.TOPIC)
        .assertThat()
            .json("userId", Matchers.equalTo(TestData.USER_ID.toString()))
            .json("userEventType", Matchers.equalTo("USER_UPDATED"))
            .json("user.firstName", Matchers.equalTo(TestData.USER_FIRST_NAME))
            .json("actingUserId", Matchers.equalTo(TestData.USER_ID.toString()));
    }

    @Test
    void onUserUpdated_publishesNoEvent() {
        userEvent.fire(new UserEvent.Updated(user(), TestData.USER_ID));

        final List<?> records = companion.consumeStrings()
            .fromTopics(UserEventJson.TOPIC)
            .awaitNoRecords(Duration.ofSeconds(2))
            .getRecords();
        assertTrue(records.isEmpty());
    }

}
