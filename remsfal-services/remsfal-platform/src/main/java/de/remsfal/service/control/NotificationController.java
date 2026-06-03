package de.remsfal.service.control;

import java.util.UUID;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.EmailEventJson;
import de.remsfal.core.json.eventing.EmailEventJson.EmailEventType;
import de.remsfal.core.json.eventing.ImmutableEmailEventJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.model.CustomerModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.opentelemetry.instrumentation.annotations.WithSpan;

@ApplicationScoped
public class NotificationController {

    @ConfigProperty(name = "de.remsfal.frontend.url.base")
    public String frontendBaseUrl;

    @ConfigProperty(name = "de.remsfal.frontend.path.projects", defaultValue = "/projects")
    public String frontendProjectsPath;

    @ConfigProperty(name = "de.remsfal.frontend.path.additional-email-verification",
        defaultValue = "/api/v1/authentication/verify-additional-email")
    public String frontendAdditionalEmailVerificationPath;

    @ConfigProperty(name = "de.remsfal.user.language.default", defaultValue = "de")
    public String defaultLanguage;

    @Inject
    Logger logger;

    @Inject
    @Channel(EmailEventJson.TOPIC)
    Emitter<EmailEventJson> notificationEmitter;

    @WithSpan("NotificationController.informUserAboutRegistration")
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
        logger.infov("Sending information about new membership (email={0})", user.getEmail());
        EmailEventJson mail = ImmutableEmailEventJson.builder()
            .user(UserJson.valueOf(user))
            .locale(defaultLanguage)
            .type(EmailEventType.PROJECT_ADMISSION)
            .link(frontendBaseUrl + frontendProjectsPath + "/" + projectId)
            .build();
        logger.info("Test: " + mail.toString());
        notificationEmitter.send(mail);
    }

    @WithSpan("NotificationController.informUserAboutAdditionalEmailVerification")
    public void informUserAboutAdditionalEmailVerification(final CustomerModel user, final String additionalEmail,
            final String verificationToken) {
        logger.infov("Sending information about additional email verification (email={0})", additionalEmail);
        final String encodedToken = URLEncoder.encode(verificationToken, StandardCharsets.UTF_8);
        EmailEventJson mail = ImmutableEmailEventJson.builder()
            .user(UserJson.valueOf(user).withEmail(additionalEmail))
            .locale(defaultLanguage)
            .type(EmailEventType.ADDITIONAL_EMAIL_VERIFICATION)
            .link(frontendBaseUrl + frontendAdditionalEmailVerificationPath + "?token=" + encodedToken)
            .build();
        notificationEmitter.send(mail);
    }

}
