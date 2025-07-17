package de.remsfal.notification.boundary;

import de.remsfal.core.json.MailJson;
import de.remsfal.notification.control.MailingController;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class NotificationConsumer {

    @Inject
    Logger logger;

    @Inject
    MailingController mailingController;

    @Incoming("user-notification-consumer")
    public CompletionStage<Void> consumeUserNotification(Message<MailJson> msg) {
        MailJson mail = msg.getPayload();

        String email = mail.getUser().getEmail();
        String type = mail.getType();
        String link = mail.getLink();
        Locale locale = mail.getLocale() != null ? Locale.forLanguageTag(mail.getLocale()) : Locale.GERMAN;

        logger.infov("Received user-notification for user email: {0}", email);
        logger.infov("Type: {0}", type);

        return CompletableFuture.runAsync(() -> {
                    switch (type) {
                        case "new Membership":
                            mailingController.sendNewMembershipEmail(mail.getUser(), link, locale);
                            break;
                        case "new Registration":
                            mailingController.sendWelcomeEmail(mail.getUser(), link, locale);
                            break;
                        default:
                            logger.warnv("Unknown notification type received: {0}", type);
                    }
                })
                .thenCompose(v -> msg.ack());
    }
}
