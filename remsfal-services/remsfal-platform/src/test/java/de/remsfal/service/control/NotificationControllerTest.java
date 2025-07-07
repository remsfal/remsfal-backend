package de.remsfal.service.control;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.ConsumerTask;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;
import org.apache.kafka.common.serialization.Serdes;
import org.jboss.logging.Logger;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class NotificationControllerTest extends AbstractTest {

    @Inject
    NotificationController notificationController;

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Inject
    Logger logger;

    private static final String TOPIC = "user-notification";

    @BeforeEach
    void setup() {
        companion.registerSerde(UserJson.class, Serdes.serdeFrom(
                new ObjectMapperSerializer<>(),
                new ObjectMapperDeserializer<>(UserJson.class)
        ));
    }

    @Test
    void testNotificationIsSentToKafkaWithAllFieldsExceptAddress() {
        UserJson userToSend = ImmutableUserJson.builder()
                .id(TestData.USER_ID_2)
                .email(TestData.USER_EMAIL_2)
                .firstName(TestData.USER_FIRST_NAME_2)
                .lastName(TestData.USER_LAST_NAME_2)
                .registeredDate(LocalDate.now())
                .mobilePhoneNumber(null)
                .businessPhoneNumber(null)
                .privatePhoneNumber(null)
                .lastLoginDate(null)
                .build();

        logger.infov("UserJson object prepared for sending: {0}", userToSend);

        notificationController.informUserAboutProjectMembership(userToSend);


        ConsumerTask<String, UserJson> task = companion.consume(UserJson.class)
                .fromTopics(TOPIC, 1)
                .awaitCompletion();

        logger.infov("Kafka consumer task completed. Number of messages consumed: {0}", task.count());

        assertEquals(1, task.count(), "Expected exactly 1 message to be consumed from the Kafka topic.");

        UserJson payload = task.getRecords().get(0).value();
        logger.infov("Payload received from Kafka: {0}", payload.toString());

        assertEquals(TestData.USER_ID_2, payload.getId(), "User ID mismatch.");
        assertEquals(TestData.USER_EMAIL_2, payload.getEmail(), "Email mismatch.");
        assertNull(payload.getName(), "Name field should be null as it's not set in the test data.");
        assertEquals(TestData.USER_FIRST_NAME_2, payload.getFirstName(), "First name mismatch.");
        assertEquals(TestData.USER_LAST_NAME_2, payload.getLastName(), "Last name mismatch.");
        assertNull(payload.getMobilePhoneNumber(), "Mobile phone number should be null.");
        assertNull(payload.getBusinessPhoneNumber(), "Business phone number should be null.");
        assertNull(payload.getPrivatePhoneNumber(), "Private phone number should be null.");
        assertNotNull(payload.getRegisteredDate(), "Registered date should not be null.");
        assertEquals(LocalDate.now(), payload.getRegisteredDate(), "Registered date mismatch.");

        assertNull(payload.getLastLoginDate(), "Last login date should be null.");
    }
}
