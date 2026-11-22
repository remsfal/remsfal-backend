package de.remsfal.service.control;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.organization.OrganizationJson;
import de.remsfal.core.json.project.ProjectJson;
import de.remsfal.core.model.CustomerModel;
import de.remsfal.service.boundary.eventing.NotificationEventProducer;
import de.remsfal.service.control.event.OrganizationEmployeeEvent;
import de.remsfal.service.control.event.ProjectMemberEvent;
import de.remsfal.service.control.event.UserEvent;
import de.remsfal.service.entity.dao.OrganizationRepository;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.OrganizationEntity;
import de.remsfal.service.entity.dto.ProjectEntity;
import de.remsfal.service.entity.dto.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import io.opentelemetry.instrumentation.annotations.WithSpan;

@ApplicationScoped
public class NotificationController {

    @ConfigProperty(name = "de.remsfal.frontend.url.base")
    public String frontendBaseUrl;

    @ConfigProperty(name = "de.remsfal.frontend.path.projects",
        defaultValue = "/projects")
    public String frontendProjectsPath;

    @ConfigProperty(name = "de.remsfal.frontend.path.organizations",
        defaultValue = "/contractor/account-settings")
    public String frontendOrganizationsPath;

    @ConfigProperty(name = "de.remsfal.frontend.path.additional-email-verification",
        defaultValue = "/verify-additional-email")
    public String frontendAdditionalEmailVerificationPath;

    @ConfigProperty(name = "de.remsfal.user.language.default", defaultValue = "de")
    public String defaultLanguage;

    @Inject
    NotificationEventProducer notificationEventProducer;

    @Inject
    UserRepository userRepository;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    OrganizationRepository organizationRepository;

    /**
     * Sends the welcome email once the registration has been committed.
     */
    @WithSpan("NotificationController.onUserEvent")
    void onUserEvent(@Observes(during = TransactionPhase.AFTER_SUCCESS) final UserEvent event) {
        if (event instanceof UserEvent.Registered registered) {
            notificationEventProducer.sendUserRegistration(withLocale(registered.user()), frontendBaseUrl);
        } else if (event instanceof UserEvent.AdditionalEmailAdded added) {
            final String encodedToken = URLEncoder.encode(added.verificationToken(), StandardCharsets.UTF_8);
            final UserJson userJson = withLocale(added.user()).withEmail(added.email());
            final String link = frontendBaseUrl + frontendAdditionalEmailVerificationPath + "?token=" + encodedToken;
            notificationEventProducer.sendAdditionalEmailVerification(userJson, link);
        }
    }

    /**
     * Informs a user about the admission to a project once the membership has been committed. Reads the user and the
     * project after the commit, a read-only access needs no transaction.
     */
    @WithSpan("NotificationController.onProjectMemberAdded")
    void onProjectMemberEvent(@Observes(during = TransactionPhase.AFTER_SUCCESS) final ProjectMemberEvent event) {
        if (event instanceof ProjectMemberEvent.Added added) {
            final Optional<UserEntity> user = userRepository.findByIdOptional(added.member().getId());
            final ProjectEntity project = projectRepository.findById(added.projectId());
            if (user.isEmpty() || project == null) {
                return;
            }
            final String link = frontendBaseUrl + frontendProjectsPath + "/" + project.getId() + "/dashboard";
            notificationEventProducer.sendProjectAdmission(toJson(user.get()), link, ProjectJson.valueOf(project));
        }
    }

    /**
     * Informs a user about the admission to an organization once the employment has been committed. Reads the user
     * and the organization after the commit, a read-only access needs no transaction.
     */
    @WithSpan("NotificationController.onOrganizationEmployeeAdded")
    void onOrganizationEmployeeEvent(
        @Observes(during = TransactionPhase.AFTER_SUCCESS) final OrganizationEmployeeEvent event) {
        if (event instanceof OrganizationEmployeeEvent.Added added) {
            final Optional<UserEntity> user = userRepository.findByIdOptional(added.employee().getId());
            final OrganizationEntity organization = organizationRepository.findById(added.organizationId());
            if (user.isEmpty() || organization == null) {
                return;
            }
            final String link = frontendBaseUrl + frontendOrganizationsPath;
            notificationEventProducer.sendOrganizationAdmission(toJson(user.get()), link,
                OrganizationJson.valueOf(organization));
        }
    }

    /**
     * Sends the one-time code of the passwordless email login. The user is null if the email address is not
     * registered yet, then the requested locale is used.
     */
    @WithSpan("NotificationController.informUserAboutLoginCode")
    public void informUserAboutLoginCode(final CustomerModel user, final String email, final String code,
        final String locale) {
        final UserJson userJson;
        if (user != null) {
            userJson = UserJson.valueOf(user).withEmail(email).withLocale(resolveLocale(user));
        } else {
            userJson = ImmutableUserJson.builder()
                .email(email)
                .locale(locale != null && !locale.isBlank() ? locale : defaultLanguage)
                .build();
        }
        notificationEventProducer.sendLoginCode(userJson, frontendBaseUrl, code);
    }

    private UserJson toJson(final CustomerModel user) {
        return UserJson.valueOf(user).withLocale(resolveLocale(user));
    }

    private ImmutableUserJson withLocale(final UserJson user) {
        final ImmutableUserJson json = ImmutableUserJson.copyOf(user);
        return user.getLocale() != null && !user.getLocale().isBlank() ? json : json.withLocale(defaultLanguage);
    }

    private String resolveLocale(final CustomerModel user) {
        return user.getLocale() != null && !user.getLocale().isBlank() ? user.getLocale() : defaultLanguage;
    }

}
