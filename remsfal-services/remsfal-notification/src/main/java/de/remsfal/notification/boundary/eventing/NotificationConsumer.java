package de.remsfal.notification.boundary.eventing;

import de.remsfal.core.json.eventing.EmailEventJson;
import de.remsfal.notification.control.MailingController;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import java.util.Locale;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class NotificationConsumer {

    @Inject
    Logger logger;

    @Inject
    MailingController mailingController;

    @Blocking
    @Incoming(EmailEventJson.TOPIC)
    @WithSpan("NotificationConsumer.consumeUserNotification")
    public CompletionStage<Void> consumeUserNotification(Message<EmailEventJson> msg) {
        EmailEventJson mail = msg.getPayload();

        String email = mail.getUser().getEmail();
        String link = mail.getLink();
        String userLocale = mail.getUser().getLocale();
        Locale locale = userLocale != null ? Locale.forLanguageTag(userLocale) : Locale.GERMAN;

        logger.infov("Received user-notification for user email: {0}", email);
        logger.infov("Type: {0}", mail.getNotificationEventType());

        Uni<Void> sendUni;
        switch (mail.getNotificationEventType()) {
            case PROJECT_ADMISSION:
                sendUni = mailingController.sendNewMembershipEmail(mail.getUser(), link, locale, mail.getProject());
                break;
            case USER_REGISTRATION:
                sendUni = mailingController.sendWelcomeEmail(mail.getUser(), link, locale);
                break;
            case ADDITIONAL_EMAIL_VERIFICATION:
                sendUni = mailingController.sendAdditionalEmailVerificationEmail(mail.getUser(), link, locale);
                break;
            case ORGANIZATION_ADMISSION:
                sendUni = mailingController.sendNewEmploymentEmail(
                    mail.getUser(), link, locale, mail.getOrganization());
                break;
            default:
                sendUni = Uni.createFrom().voidItem();
        }

        return sendUni
            .invoke(() -> logger.infov("Email has been sent to {0}", email))
            .subscribeAsCompletionStage()
            .thenCompose(ignored -> msg.ack());
    }

}
