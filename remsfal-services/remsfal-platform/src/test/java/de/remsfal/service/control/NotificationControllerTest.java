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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class NotificationControllerTest extends AbstractTest {

    @Inject
    NotificationController notificationController;

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @BeforeEach
    void setup() {
        companion.registerSerde(UserJson.class, Serdes.serdeFrom(
                new ObjectMapperSerializer<>(),
                new ObjectMapperDeserializer<>(UserJson.class)
        ));
    }

    @Test
    void testNotificationIsSentToKafkaWithAllFieldsExceptAddress() {
        String topic = "user-notification";

        UserJson user = ImmutableUserJson.builder()
                .id(TestData.USER_ID_2)
                .email(TestData.USER_EMAIL_2)
                .firstName(TestData.USER_FIRST_NAME_2)
                .lastName(TestData.USER_LAST_NAME_2)
                .mobilePhoneNumber(null)
                .businessPhoneNumber(null)
                .privatePhoneNumber(null)
                .registeredDate(null)
                .lastLoginDate(null)
                .build();

        notificationController.informUserAboutProjectMembership(user);

        ConsumerTask<String, UserJson> task = companion.consume(UserJson.class)
                .fromTopics(topic, 1);

        task.awaitCompletion();
        logger.infov("task: " + task);

        assertEquals(1, task.count());
        UserJson payload = task.getRecords().get(0).value();
        logger.infov("payload: " + payload.toString());

        assertEquals(TestData.USER_ID_2, payload.getId());
        assertEquals(TestData.USER_EMAIL_2, payload.getEmail());
        assertNull(payload.getName());
        assertEquals(TestData.USER_FIRST_NAME_2, payload.getFirstName());
        assertEquals(TestData.USER_LAST_NAME_2, payload.getLastName());
        assertNull(payload.getMobilePhoneNumber());
        assertNull(payload.getBusinessPhoneNumber());
        assertNull(payload.getPrivatePhoneNumber());
        assertEquals(LocalDate.now(), payload.getRegisteredDate());
        assertNull(payload.getLastLoginDate());
    }
}
