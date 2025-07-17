package de.remsfal.notification.boundary;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.MailJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.notification.control.MailingController;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import static org.mockito.Mockito.*;

class NotificationConsumerTest {

    @Test
    void testConsumeUserNotification_NewRegistration() throws Exception {
        UserJson user = ImmutableUserJson.builder()
                .id(UUID.randomUUID().toString())
                .email("test@example.com")
                .firstName("Test")
                .lastName("Consumer")
                .build();

        MailJson mailJson = new MailJson();
        mailJson.setUser(user);
        mailJson.setType("new Registration");
        mailJson.setLocale("en");
        mailJson.setLink("https://remsfal.de");

        Message<MailJson> testMessage = Message.of(mailJson);

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

        MailJson mailJson = new MailJson();
        mailJson.setUser(user);
        mailJson.setType("new Membership");
        mailJson.setLocale("de");
        mailJson.setLink("https://remsfal.de");

        Message<MailJson> testMessage = Message.of(mailJson);

        MailingController mockController = mock(MailingController.class);

        NotificationConsumer notificationConsumer = new NotificationConsumer();
        notificationConsumer.mailingController = mockController;
        notificationConsumer.logger = mock(Logger.class);

        CompletionStage<Void> result = notificationConsumer.consumeUserNotification(testMessage);

        result.toCompletableFuture().get();

        verify(mockController, times(1))
                .sendNewMembershipEmail(user, "https://remsfal.de", Locale.GERMAN);
    }

    @Test
    void testConsumeUserNotification_InvalidMessage() throws Exception {
        MailJson mailJson = new MailJson();
        mailJson.setUser(null);

        Message<MailJson> testMessage = Message.of(mailJson);

        MailingController mockController = mock(MailingController.class);

        NotificationConsumer notificationConsumer = new NotificationConsumer();
        notificationConsumer.mailingController = mockController;
        notificationConsumer.logger = mock(Logger.class);

        CompletionStage<Void> result = notificationConsumer.consumeUserNotification(testMessage);

        result.toCompletableFuture().get();

        verifyNoInteractions(mockController);
    }
}
