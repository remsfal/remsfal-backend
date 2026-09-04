package de.remsfal.notification.boundary.eventing;

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
import de.remsfal.core.json.eventing.EmailEventJson.NotificationEventType;
import de.remsfal.core.json.eventing.ImmutableEmailEventJson;
import de.remsfal.core.json.organization.ImmutableOrganizationJson;
import de.remsfal.core.json.organization.OrganizationJson;
import de.remsfal.core.json.project.ImmutableProjectJson;
import de.remsfal.core.json.project.ProjectJson;
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
                .locale("en")
                .build();

        ImmutableEmailEventJson json = ImmutableEmailEventJson.builder()
                .user(user)
                .notificationEventType(NotificationEventType.USER_REGISTRATION)
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
                .locale("de")
                .build();

        ProjectJson project = ImmutableProjectJson.builder()
                .id(UUID.randomUUID())
                .title("Test Project")
                .build();

        ImmutableEmailEventJson json = ImmutableEmailEventJson.builder()
                .user(user)
                .notificationEventType(NotificationEventType.PROJECT_ADMISSION)
                .link("https://remsfal.de")
                .project(project)
                .build();

        companion.produce(ImmutableEmailEventJson.class)
            .fromRecords(new ProducerRecord<>(EmailEventJson.TOPIC, json))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() ->
                verify(mailingController, atLeastOnce())
                    .sendNewMembershipEmail(user, "https://remsfal.de", Locale.GERMAN, project)
                );
    }

    @Test
    void testConsumeUserNotification_NewEmployment() {
        UserJson user = ImmutableUserJson.builder()
                .id(UUID.randomUUID())
                .email("test3@example.com")
                .firstName("Test")
                .lastName("Employment")
                .locale("de")
                .build();

        OrganizationJson organization = ImmutableOrganizationJson.builder()
                .id(UUID.randomUUID())
                .name("Test Organization")
                .phone("+491234567890")
                .email("organization@example.com")
                .trade("Property Management")
                .vatIdentificationNumber("DE123456789")
                .build();

        ImmutableEmailEventJson json = ImmutableEmailEventJson.builder()
                .user(user)
                .notificationEventType(NotificationEventType.ORGANIZATION_ADMISSION)
                .link("https://remsfal.de")
                .organization(organization)
                .build();

        companion.produce(ImmutableEmailEventJson.class)
            .fromRecords(new ProducerRecord<>(EmailEventJson.TOPIC, json))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() ->
                verify(mailingController, atLeastOnce())
                    .sendNewEmploymentEmail(user, "https://remsfal.de", Locale.GERMAN, organization)
                );
    }

    @Test
    void testConsumeUserNotification_AdditionalEmailVerification() {
        UserJson user = ImmutableUserJson.builder()
                .id(UUID.randomUUID())
                .email("additional@example.com")
                .firstName("Test")
                .lastName("Verification")
                .locale("en")
                .build();

        ImmutableEmailEventJson json = ImmutableEmailEventJson.builder()
                .user(user)
                .notificationEventType(NotificationEventType.ADDITIONAL_EMAIL_VERIFICATION)
                .link("https://remsfal.de/api/v1/authentication/verify-additional-email?token=token")
                .build();

        companion.produce(ImmutableEmailEventJson.class)
            .fromRecords(new ProducerRecord<>(EmailEventJson.TOPIC, json))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() ->
                verify(mailingController, atLeastOnce())
                    .sendAdditionalEmailVerificationEmail(
                        user,
                        "https://remsfal.de/api/v1/authentication/verify-additional-email?token=token",
                        Locale.ENGLISH
                    )
                );
    }

}
