package de.remsfal.service.control;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.EmailEventJson;
import de.remsfal.core.json.eventing.EmailEventJson.NotificationEventType;
import de.remsfal.core.json.eventing.ImmutableEmailEventJson;
import de.remsfal.core.json.organization.OrganizationJson;
import de.remsfal.core.json.project.ProjectJson;
import de.remsfal.core.model.CustomerModel;
import de.remsfal.core.model.OrganizationModel;
import de.remsfal.core.model.project.ProjectModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.opentelemetry.instrumentation.annotations.WithSpan;

@ApplicationScoped
public class NotificationController {

    @ConfigProperty(name = "de.remsfal.frontend.url.base")
    public String frontendBaseUrl;

    @ConfigProperty(name = "de.remsfal.frontend.path.projects", defaultValue = "/projects")
    public String frontendProjectsPath;

    @ConfigProperty(name = "de.remsfal.frontend.path.organizations", defaultValue = "/organizations")
    public String frontendOrganizationsPath;

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
            .user(UserJson.valueOf(user).withLocale(resolveLocale(user)))
            .notificationEventType(NotificationEventType.USER_REGISTRATION)
            .link(frontendBaseUrl)
            .build();
        notificationEmitter.send(mail);
    }

    @WithSpan("NotificationController.informUserAboutProjectMembership")
    public void informUserAboutProjectMembership(final CustomerModel user, final ProjectModel project) {
        logger.infov("Sending information about new membership (email={0})", user.getEmail());
        EmailEventJson mail = ImmutableEmailEventJson.builder()
            .user(UserJson.valueOf(user).withLocale(resolveLocale(user)))
            .notificationEventType(NotificationEventType.PROJECT_ADMISSION)
            .link(frontendBaseUrl + frontendProjectsPath + "/" + project.getId())
            .project(ProjectJson.valueOf(project))
            .build();
        notificationEmitter.send(mail);
    }

    @WithSpan("NotificationController.informUserAboutOrganizationMembership")
    public void informUserAboutOrganizationMembership(final CustomerModel user, final OrganizationModel organization) {
        logger.infov("Sending information about new organization membership (email={0})", user.getEmail());
        EmailEventJson mail = ImmutableEmailEventJson.builder()
            .user(UserJson.valueOf(user).withLocale(resolveLocale(user)))
            .notificationEventType(NotificationEventType.ORGANIZATION_ADMISSION)
            .link(frontendBaseUrl + frontendOrganizationsPath + "/" + organization.getId())
            .organization(OrganizationJson.valueOf(organization))
            .build();
        notificationEmitter.send(mail);
    }

    @WithSpan("NotificationController.informUserAboutAdditionalEmailVerification")
    public void informUserAboutAdditionalEmailVerification(final CustomerModel user, final String additionalEmail,
        final String verificationToken) {
        logger.infov("Sending information about additional email verification (email={0})", additionalEmail);
        final String encodedToken = URLEncoder.encode(verificationToken, StandardCharsets.UTF_8);
        EmailEventJson mail = ImmutableEmailEventJson.builder()
            .user(UserJson.valueOf(user).withEmail(additionalEmail).withLocale(resolveLocale(user)))
            .notificationEventType(NotificationEventType.ADDITIONAL_EMAIL_VERIFICATION)
            .link(frontendBaseUrl + frontendAdditionalEmailVerificationPath + "?token=" + encodedToken)
            .build();
        notificationEmitter.send(mail);
    }

    private String resolveLocale(final CustomerModel user) {
        return user.getLocale() != null && !user.getLocale().isBlank() ? user.getLocale() : defaultLanguage;
    }

}
