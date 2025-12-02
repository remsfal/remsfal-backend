package de.remsfal.service.control;

import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.EmailEventJson;
import de.remsfal.core.json.eventing.EmailEventJson.EmailEventType;
import de.remsfal.core.json.eventing.ImmutableEmailEventJson;
import de.remsfal.core.model.CustomerModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class NotificationController {

    @ConfigProperty(name = "de.remsfal.frontend.url.base")
    public String frontendBaseUrl;

    @ConfigProperty(name = "de.remsfal.frontend.path.projects", defaultValue = "/projects")
    public String frontendProjectsPath;

    @ConfigProperty(name = "de.remsfal.user.language.default", defaultValue = "de")
    public String defaultLanguage;

    @Inject
    Logger logger;

    @Inject
    @Channel(EmailEventJson.TOPIC)
    Emitter<EmailEventJson> notificationEmitter;

    public void informUserAboutRegistration(final CustomerModel user) {
        logger.infov("Sending information about user registration (email={0})", user.getEmail());
        EmailEventJson mail = ImmutableEmailEventJson.builder()
            .user(UserJson.valueOf(user))
            .locale(defaultLanguage)
            .type(EmailEventType.USER_REGISTRATION)
            .link(frontendBaseUrl)
            .build();
        notificationEmitter.send(mail);
    }

    @WithSpan("NotificationController.informUserAboutProjectMembership")
    public void informUserAboutProjectMembership(final CustomerModel user, final UUID projectId) {
        Span span = Span.current();
        if (span != null) {
            span.setAttribute("remsfal.notification.type", "PROJECT_ADMISSION");
            span.setAttribute("remsfal.user.email", user.getEmail());
            span.setAttribute("remsfal.project.id", projectId.toString());
        }
        logger.infov("Sending information about new membership (email={0})", user.getEmail());
        try {
            EmailEventJson mail = ImmutableEmailEventJson.builder()
                .user(UserJson.valueOf(user))
                .locale(defaultLanguage)
                .type(EmailEventType.PROJECT_ADMISSION)
                .link(frontendBaseUrl + frontendProjectsPath + "/" + projectId)
                .build();
            logger.info("Test: " + mail.toString());
            notificationEmitter.send(mail);

            if (span != null) {
                span.addEvent("EmailEventJson PROJECT_ADMISSION sent to Kafka");
            }
        } catch (RuntimeException e) {
            if (span != null) {
                span.recordException(e);
                span.setStatus(StatusCode.ERROR, "Failed to send PROJECT_ADMISSION event");
            }
            throw e;
        }
    }
}
