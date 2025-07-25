package de.remsfal.notification.boundary;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.eventing.EmailEventJson;
import de.remsfal.core.json.eventing.ImmutableEmailEventJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.notification.control.MailingController;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import static org.mockito.Mockito.*;

@QuarkusTest
class NotificationConsumerTest {

    @Test
    void testConsumeUserNotification_NewRegistration() throws Exception {
        UserJson user = ImmutableUserJson.builder()
                .id(UUID.randomUUID().toString())
                .email("test@example.com")
                .firstName("Test")
                .lastName("Consumer")
                .build();

        EmailEventJson mailJson = ImmutableEmailEventJson.builder()
                .user(user)
                .locale("en")
                .type(EmailEventJson.EmailEventType.USER_REGISTRATION)
                .link("https://remsfal.de")
                .build();

        Message<EmailEventJson> testMessage = Message.of(mailJson);

        MailingController mockController = mock(MailingController.class);

        NotificationConsumer notificationConsumer = new NotificationConsumer();
        notificationConsumer.mailingController = mockController;
        notificationConsumer.logger = mock(Logger.class);

        CompletionStage<Void> result = notificationConsumer.consumeUserNotification(testMessage);

        result.toCompletableFuture().get();

        verify(mockController, times(1))
                .sendWelcomeEmail(user, "https://remsfal.de", Locale.ENGLISH);
    }

    @Test
    void testConsumeUserNotification_NewMembership() throws Exception {
        UserJson user = ImmutableUserJson.builder()
                .id(UUID.randomUUID().toString())
                .email("test2@example.com")
                .firstName("Test")
                .lastName("Membership")
                .build();

        EmailEventJson mailJson = ImmutableEmailEventJson.builder()
                .user(user)
                .locale("de")
                .type(EmailEventJson.EmailEventType.PROJECT_ADMISSION)
                .link("https://remsfal.de")
                .build();

        Message<EmailEventJson> testMessage = Message.of(mailJson);

        MailingController mockController = mock(MailingController.class);

        NotificationConsumer notificationConsumer = new NotificationConsumer();
        notificationConsumer.mailingController = mockController;
        notificationConsumer.logger = mock(Logger.class);

        CompletionStage<Void> result = notificationConsumer.consumeUserNotification(testMessage);

        result.toCompletableFuture().get();

        verify(mockController, times(1))
                .sendNewMembershipEmail(user, "https://remsfal.de", Locale.GERMAN);
    }
}
