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
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static de.remsfal.service.TestData.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class NotificationControllerTest extends AbstractTest {

    @Inject
    NotificationController notificationController;

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Test
    void testNotificationIsSentToKafkaWithAllFieldsExceptAddress() {
        String topic = "user-notification";


        UserJson user = ImmutableUserJson.builder()
                .id(TestData.USER_ID)
                .email(TestData.USER_EMAIL)
                .firstName(TestData.USER_FIRST_NAME)
                .lastName(TestData.USER_LAST_NAME)
                .address(addressBuilder1().build())
                .mobilePhoneNumber("+49123456789")
                .businessPhoneNumber("+4987654321")
                .privatePhoneNumber("+4901234567")
                .registeredDate(LocalDate.of(2020, 5, 20))
                .lastLoginDate(LocalDateTime.of(2025, 6, 24, 15, 30))
                .active(Boolean.TRUE)
                .build();

        notificationController.informUserAboutProjectMembership(user);

        ConsumerTask<String, UserJson> task = companion.consume(UserJson.class)
                .fromTopics(topic, 1);

        task.awaitCompletion();

        assertEquals(1, task.count());
        UserJson payload = task.getRecords().get(0).value();

        assertEquals(TestData.USER_ID, payload.getId());
        assertEquals(TestData.USER_EMAIL, payload.getEmail());
        assertNull(payload.getName());
        assertEquals(TestData.USER_FIRST_NAME, payload.getFirstName());
        assertEquals(TestData.USER_LAST_NAME, payload.getLastName());
        assertEquals(addressBuilder1().build(), payload.getAddress());
        assertEquals("+49123456789", payload.getMobilePhoneNumber());
        assertEquals("+4987654321", payload.getBusinessPhoneNumber());
        assertEquals("+4901234567", payload.getPrivatePhoneNumber());
        assertEquals(LocalDate.of(2020, 5, 20), payload.getRegisteredDate());
        assertEquals(LocalDateTime.of(2025, 6, 24, 15, 30), payload.getLastLoginDate());
        assertTrue(payload.isActive());
    }
}
