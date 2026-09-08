package de.remsfal.service.control;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.organization.OrganizationJson;
import de.remsfal.core.json.project.ProjectJson;
import de.remsfal.core.model.CustomerModel;
import de.remsfal.core.model.OrganizationModel;
import de.remsfal.core.model.project.ProjectModel;
import de.remsfal.service.boundary.eventing.NotificationEventProducer;
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
    NotificationEventProducer notificationEventProducer;

    @WithSpan("NotificationController.informUserAboutRegistration")
    public void informUserAboutRegistration(final CustomerModel user) {
        final UserJson userJson = UserJson.valueOf(user).withLocale(resolveLocale(user));
        notificationEventProducer.sendUserRegistration(userJson, frontendBaseUrl);
    }

    @WithSpan("NotificationController.informUserAboutProjectMembership")
    public void informUserAboutProjectMembership(final CustomerModel user, final ProjectModel project) {
        final UserJson userJson = UserJson.valueOf(user).withLocale(resolveLocale(user));
        final String link = frontendBaseUrl + frontendProjectsPath + "/" + project.getId();
        notificationEventProducer.sendProjectAdmission(userJson, link, ProjectJson.valueOf(project));
    }

    @WithSpan("NotificationController.informUserAboutOrganizationMembership")
    public void informUserAboutOrganizationMembership(final CustomerModel user, final OrganizationModel organization) {
        final UserJson userJson = UserJson.valueOf(user).withLocale(resolveLocale(user));
        final String link = frontendBaseUrl + frontendOrganizationsPath + "/" + organization.getId();
        notificationEventProducer.sendOrganizationAdmission(userJson, link, OrganizationJson.valueOf(organization));
    }

    @WithSpan("NotificationController.informUserAboutAdditionalEmailVerification")
    public void informUserAboutAdditionalEmailVerification(final CustomerModel user, final String additionalEmail,
        final String verificationToken) {
        final String encodedToken = URLEncoder.encode(verificationToken, StandardCharsets.UTF_8);
        final UserJson userJson = UserJson.valueOf(user).withEmail(additionalEmail).withLocale(resolveLocale(user));
        final String link = frontendBaseUrl + frontendAdditionalEmailVerificationPath + "?token=" + encodedToken;
        notificationEventProducer.sendAdditionalEmailVerification(userJson, link);
    }

    private String resolveLocale(final CustomerModel user) {
        return user.getLocale() != null && !user.getLocale().isBlank() ? user.getLocale() : defaultLanguage;
    }

}
