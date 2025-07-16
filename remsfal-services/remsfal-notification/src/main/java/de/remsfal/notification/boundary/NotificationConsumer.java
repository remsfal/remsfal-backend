package de.remsfal.notification.boundary;

import de.remsfal.core.json.MailJson;
import de.remsfal.notification.control.MailingController;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;
import io.smallrye.mutiny.Uni;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class NotificationConsumer {

    @Inject
    Logger logger;

    @Inject
    MailingController mailingController;

    @Incoming("user-notification-consumer")
    public Uni<Void> consumeUserNotification(Message<MailJson> msg) {
        MailJson mail = msg.getPayload();

        if (mail == null || mail.getUser() == null) {
            logger.warn("Received invalid mail notification: missing user.");
            return Uni.createFrom().completionStage(msg.ack());
        }

        String email = mail.getUser().getEmail();
        String type = mail.getType();
        String link = mail.getLink();
        Locale locale = mail.getLocale() != null ? Locale.forLanguageTag(mail.getLocale()) : Locale.GERMAN;

        logger.infov("Received user-notification for user email: {0}", email);
        logger.infov("Type: {0}", type);

        return Uni.createFrom().completionStage(
                CompletableFuture.runAsync(() -> {
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
                        .thenCompose(x -> msg.ack())
        );
    }
}
