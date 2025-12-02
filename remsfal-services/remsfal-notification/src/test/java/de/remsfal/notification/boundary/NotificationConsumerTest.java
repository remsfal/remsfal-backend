package de.remsfal.notification.boundary;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.EmailEventJson;
import de.remsfal.core.json.eventing.ImmutableEmailEventJson;
import de.remsfal.core.json.eventing.EmailEventJson.EmailEventType;
import de.remsfal.core.model.UserModel;
import de.remsfal.notification.control.MailingController;
import de.remsfal.notification.boundary.NotificationConsumer;
import de.remsfal.test.kafka.AbstractKafkaTest;
import de.remsfal.test.TestData;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.kafka.KafkaCompanionResource;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Message;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class NotificationConsumerTest extends AbstractKafkaTest {

    @InjectSpy
    MailingController mailingController;

    @Inject
    NotificationConsumer consumer;

    @Override
    @BeforeEach
    protected void clearAllTopics() {
        // Skipping topic clearing to avoid offset issues
        companion.registerSerde(ImmutableEmailEventJson.class,
            new ObjectMapperSerde<>(ImmutableEmailEventJson.class));
    }

    @Test
    void testConsumeUserNotification_NewRegistration() {
        UserJson user = ImmutableUserJson.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("Test")
                .lastName("Consumer")
                .build();

        ImmutableEmailEventJson json = ImmutableEmailEventJson.builder()
                .user(user)
                .locale("en")
                .type(EmailEventJson.EmailEventType.USER_REGISTRATION)
                .link("https://remsfal.de")
                .build();

        companion.produce(ImmutableEmailEventJson.class)
            .fromRecords(new ProducerRecord<>(EmailEventJson.TOPIC, json))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() ->
                verify(mailingController, atLeastOnce())
                    .sendWelcomeEmail(user, "https://remsfal.de", Locale.ENGLISH)
                );
    }

    @Test
    void testConsumeUserNotification_NewMembership() {
        UserJson user = ImmutableUserJson.builder()
                .id(UUID.randomUUID())
                .email("test2@example.com")
                .firstName("Test")
                .lastName("Membership")
                .build();

        ImmutableEmailEventJson json = ImmutableEmailEventJson.builder()
                .user(user)
                .locale("de")
                .type(EmailEventJson.EmailEventType.PROJECT_ADMISSION)
                .link("https://remsfal.de")
                .build();

        companion.produce(ImmutableEmailEventJson.class)
            .fromRecords(new ProducerRecord<>(EmailEventJson.TOPIC, json))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() ->
                verify(mailingController, atLeastOnce())
                    .sendNewMembershipEmail(user, "https://remsfal.de", Locale.GERMAN)
                );
    }

    @Test
    void consumeUserNotification_failureTriggersErrorPath() {
        UserJson user = ImmutableUserJson.builder()
                .id(TestData.USER_ID)
                .email(TestData.USER_EMAIL)
                .build();

        EmailEventJson mail = ImmutableEmailEventJson.builder()
                .user(user)
                .locale(Locale.GERMAN.toLanguageTag())
                .type(EmailEventType.PROJECT_ADMISSION)
                .link("https://remsfal.de/projects/" + TestData.PROJECT_ID)
                .build();

        @SuppressWarnings("unchecked")
        Message<EmailEventJson> msg = mock(Message.class);
        when(msg.getPayload()).thenReturn(mail);

        doThrow(new RuntimeException("Mail sending failed"))
                .when(mailingController)
                .sendNewMembershipEmail(any(), anyString(), any(Locale.class));

        assertThrows(RuntimeException.class,
                () -> consumer.consumeUserNotification(msg));
    }

}
