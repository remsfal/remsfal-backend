//package de.remsfal.notification.boundary;
//
//import de.remsfal.core.json.ImmutableUserJson;
//import de.remsfal.core.json.MailJson;
//import de.remsfal.core.json.UserJson;
//import de.remsfal.notification.boundary.NotificationConsumer;
//import de.remsfal.notification.control.MailingController;
//import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
//import org.eclipse.microprofile.reactive.messaging.Message;
//import org.junit.jupiter.api.Test;
//
//import java.util.Locale;
//import java.util.UUID;
//
//import static org.mockito.Mockito.*;
//
//class NotificationConsumerTest {
//
//    @Test
//    void testConsumeUserNotification_NewRegistration() {
//        // ImmutableUserJson bauen
//        UserJson user = ImmutableUserJson.builder()
//                .id(UUID.randomUUID().toString())
//                .email("test@example.com")
//                .firstName("Test")
//                .lastName("Consumer")
//                .build();
//
//        // MailJson zusammenbauen und User setzen
//        MailJson mailJson = new MailJson();
//        mailJson.setUser(user);
//        mailJson.setType("new Registration");
//        mailJson.setLocale("en");
//        mailJson.setLink("https://remsfal.de");
//
//        // Test-Message
//        Message<MailJson> testMessage = Message.of(mailJson);
//
//        // MailingController mocken
//        MailingController mockController = mock(MailingController.class);
//
//        // NotificationConsumer bauen, mockController injizieren
//        NotificationConsumer notificationConsumer = new NotificationConsumer();
//        notificationConsumer.mailingController = mockController;
//        notificationConsumer.logger = mock(org.jboss.logging.Logger.class);
//
//        // Act & Assert
//        notificationConsumer
//                .consumeUserNotification(testMessage)
//                .subscribe()
//                .withSubscriber(UniAssertSubscriber.create())
//                .assertCompleted();
//
//        // Verify: sendWelcomeEmail wurde korrekt aufgerufen
//        verify(mockController, times(1))
//                .sendWelcomeEmail(user, "https://remsfal.de", Locale.ENGLISH);
//    }
//
//    @Test
//    void testConsumeUserNotification_NewMembership() {
//        UserJson user = ImmutableUserJson.builder()
//                .id(UUID.randomUUID().toString())
//                .email("test2@example.com")
//                .firstName("Test")
//                .lastName("Membership")
//                .build();
//
//        MailJson mailJson = new MailJson();
//        mailJson.setUser(user);
//        mailJson.setType("new Membership");
//        mailJson.setLocale("de");
//        mailJson.setLink("https://remsfal.de");
//
//        Message<MailJson> testMessage = Message.of(mailJson);
//
//        MailingController mockController = mock(MailingController.class);
//        NotificationConsumer notificationConsumer = new NotificationConsumer();
//        notificationConsumer.mailingController = mockController;
//        notificationConsumer.logger = mock(org.jboss.logging.Logger.class);
//
//        notificationConsumer
//                .consumeUserNotification(testMessage)
//                .subscribe()
//                .withSubscriber(UniAssertSubscriber.create())
//                .assertCompleted();
//
//        verify(mockController, times(1))
//                .sendNewMembershipEmail(user, "https://remsfal.de", Locale.GERMAN);
//    }
//
//    @Test
//    void testConsumeUserNotification_InvalidMessage() {
//        MailJson mailJson = new MailJson();
//        mailJson.setUser(null); // explizit kein User
//
//        Message<MailJson> testMessage = Message.of(mailJson);
//
//        MailingController mockController = mock(MailingController.class);
//        NotificationConsumer notificationConsumer = new NotificationConsumer();
//        notificationConsumer.mailingController = mockController;
//        notificationConsumer.logger = mock(org.jboss.logging.Logger.class);
//
//        notificationConsumer
//                .consumeUserNotification(testMessage)
//                .subscribe()
//                .withSubscriber(UniAssertSubscriber.create())
//                .assertCompleted();
//
//        // Keine Aufrufe!
//        verifyNoInteractions(mockController);
//    }
//}
