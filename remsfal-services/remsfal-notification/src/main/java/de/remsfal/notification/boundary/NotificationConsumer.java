package de.remsfal.notification.boundary;

import de.remsfal.core.json.eventing.EmailEventJson;
import de.remsfal.notification.control.MailingController;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
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
        Locale locale = mail.getLocale() != null ? Locale.forLanguageTag(mail.getLocale()) : Locale.GERMAN;

        Span span = Span.current();
        if (span != null) {
            span.setAttribute("remsfal.notification.user.email", email);
            if (link != null) {
                span.setAttribute("remsfal.notification.link", link);
            }
            span.setAttribute("remsfal.notification.type", mail.getType().name());
        }

        logger.infov("Received user-notification for user email: {0}", email);
        logger.infov("Type: {0}", mail.getType());

        try {
            switch (mail.getType()) {
                case PROJECT_ADMISSION:
                    mailingController.sendNewMembershipEmail(mail.getUser(), link, locale);
                    break;
                case USER_REGISTRATION:
                    // bleibt für Vollständigkeit im Trace
                    mailingController.sendWelcomeEmail(mail.getUser(), link, locale);
                    break;
            }
            logger.infov("Email has been send");
        } catch (RuntimeException e) {
            if (span != null) {
                span.recordException(e);
                span.setStatus(StatusCode.ERROR, "Failed to process notification event");
            }
            throw e;
        }
        return msg.ack();
    }

}
