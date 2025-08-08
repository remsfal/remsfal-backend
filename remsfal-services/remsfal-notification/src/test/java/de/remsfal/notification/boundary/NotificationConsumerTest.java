package de.remsfal.notification.boundary;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.EmailEventJson;
import de.remsfal.core.json.eventing.ImmutableEmailEventJson;
import de.remsfal.notification.control.MailingController;
import de.remsfal.test.kafka.AbstractKafkaTest;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.kafka.KafkaCompanionResource;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class NotificationConsumerTest extends AbstractKafkaTest {

    @InjectSpy
    MailingController mailingController;

    @Inject
    NotificationConsumer consumer;

    @BeforeEach
    void registerSerde() {
        companion.registerSerde(ImmutableEmailEventJson.class,
            new ObjectMapperSerde<>(ImmutableEmailEventJson.class));
    }

    @Test
    void testConsumeUserNotification_NewRegistration() {
        UserJson user = ImmutableUserJson.builder()
                .id(UUID.randomUUID().toString())
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
                .id(UUID.randomUUID().toString())
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

}
