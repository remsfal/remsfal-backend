package de.remsfal.service.control;

import java.util.UUID;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.eventing.NotificationEventJson;
import de.remsfal.core.json.organization.ImmutableOrganizationEmployeeJson;
import de.remsfal.core.json.project.ImmutableProjectMemberJson;
import de.remsfal.core.model.OrganizationEmployeeModel.EmployeeRole;
import de.remsfal.core.model.project.ProjectMemberModel.MemberRole;
import de.remsfal.service.control.event.OrganizationEmployeeEvent;
import de.remsfal.service.control.event.ProjectMemberEvent;
import de.remsfal.service.control.event.UserEvent;
import de.remsfal.service.entity.dao.OrganizationRepository;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.OrganizationEntity;
import de.remsfal.service.entity.dto.ProjectEntity;
import de.remsfal.service.entity.dto.UserEntity;
import de.remsfal.test.TestData;
import de.remsfal.test.kafka.AbstractKafkaTest;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.KafkaCompanionResource;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Fires the CDI events without a transaction, so that the {@code AFTER_SUCCESS} observers of the
 * {@link NotificationController} are notified immediately.
 */
@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class NotificationControllerTest extends AbstractKafkaTest {

    @Inject
    NotificationController notificationController;

    @Inject
    Event<UserEvent> userEvent;

    @Inject
    Event<ProjectMemberEvent> projectMemberEvent;

    @Inject
    Event<OrganizationEmployeeEvent> organizationEmployeeEvent;

    @Inject
    UserRepository userRepository;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    OrganizationRepository organizationRepository;

    private UserEntity persistUser() {
        final UserEntity user = new UserEntity();
        user.generateId();
        user.setEmail(UUID.randomUUID() + "@example.org");
        QuarkusTransaction.requiringNew().run(() -> userRepository.persist(user));
        return user;
    }

    @Test
    void onProjectMemberAdded_sendsProjectAdmission() {
        final UserEntity user = persistUser();
        final ProjectEntity project = new ProjectEntity();
        project.generateId();
        project.setTitle("Test Project");
        QuarkusTransaction.requiringNew().run(() -> projectRepository.persist(project));

        projectMemberEvent.fire(new ProjectMemberEvent.Added(project.getId(), ImmutableProjectMemberJson.builder()
            .id(user.getId())
            .email(user.getEmail())
            .role(MemberRole.MANAGER)
            .build(), null));

        given()
            .topic(NotificationEventJson.TOPIC)
            .assertThat()
            .json("user.id", Matchers.equalTo(user.getId().toString()))
            .json("user.email", Matchers.equalTo(user.getEmail()))
            .json("user.locale", Matchers.equalTo("de"))
            .json("notificationEventType", Matchers.equalTo("PROJECT_ADMISSION"))
            .json("project.title", Matchers.equalTo("Test Project"))
            .json("link", Matchers.equalTo("https://remsfal.de/projects/" + project.getId() + "/dashboard"));
    }

    @Test
    void onOrganizationEmployeeAdded_sendsOrganizationAdmission() {
        final UserEntity user = persistUser();
        final OrganizationEntity organization = new OrganizationEntity();
        organization.generateId();
        organization.setName("Test Organization");
        organization.setEmail(UUID.randomUUID() + "@example.org");
        QuarkusTransaction.requiringNew().run(() -> organizationRepository.persist(organization));

        organizationEmployeeEvent.fire(new OrganizationEmployeeEvent.Added(organization.getId(),
            ImmutableOrganizationEmployeeJson.builder()
                .id(user.getId())
                .email(user.getEmail())
                .employeeRole(EmployeeRole.STAFF)
                .build(),
            null));

        given()
            .topic(NotificationEventJson.TOPIC)
            .assertThat()
            .json("user.id", Matchers.equalTo(user.getId().toString()))
            .json("user.locale", Matchers.equalTo("de"))
            .json("notificationEventType", Matchers.equalTo("ORGANIZATION_ADMISSION"))
            .json("organization.name", Matchers.equalTo("Test Organization"))
            .json("link", Matchers.equalTo("https://remsfal.de/contractor/account-settings"));
    }

    @Test
    void onUserRegistered_sendsRegistration() {
        userEvent.fire(new UserEvent.Registered(ImmutableUserJson.builder()
            .id(TestData.USER_ID)
            .email(TestData.USER_EMAIL)
            .build(), null));

        given()
            .topic(NotificationEventJson.TOPIC)
            .assertThat()
            .json("user.id", Matchers.equalTo(TestData.USER_ID.toString()))
            .json("user.email", Matchers.equalTo(TestData.USER_EMAIL))
            .json("user.locale", Matchers.equalTo("de"))
            .json("notificationEventType", Matchers.equalTo("USER_REGISTRATION"))
            .json("link", Matchers.equalTo("https://remsfal.de"));
    }

    @Test
    void onUserRegistered_usesUserLocaleOverDefault() {
        userEvent.fire(new UserEvent.Registered(ImmutableUserJson.builder()
            .id(TestData.USER_ID)
            .email(TestData.USER_EMAIL)
            .locale("en")
            .build(), null));

        given()
            .topic(NotificationEventJson.TOPIC)
            .assertThat()
            .json("user.id", Matchers.equalTo(TestData.USER_ID.toString()))
            .json("user.locale", Matchers.equalTo("en"));
    }

    @Test
    void onAdditionalEmailAdded_sendsVerification() {
        userEvent.fire(new UserEvent.AdditionalEmailAdded(ImmutableUserJson.builder()
            .id(TestData.USER_ID)
            .email(TestData.USER_EMAIL)
            .build(), "additional@example.org", "token+/=", null));

        given()
            .topic(NotificationEventJson.TOPIC)
            .assertThat()
            .json("user.email", Matchers.equalTo("additional@example.org"))
            .json("notificationEventType", Matchers.equalTo("ADDITIONAL_EMAIL_VERIFICATION"))
            .json("link", Matchers.equalTo("https://remsfal.de/verify-additional-email?token=token%2B%2F%3D"));
    }

    @Test
    void testInformUserAboutLoginCode_unregisteredAddress() {
        notificationController.informUserAboutLoginCode(null, "new@example.com", "12345678", "en");

        given()
            .topic(NotificationEventJson.TOPIC)
            .assertThat()
            .json("user.email", Matchers.equalTo("new@example.com"))
            .json("user.locale", Matchers.equalTo("en"))
            .json("notificationEventType", Matchers.equalTo("LOGIN_CODE"))
            .json("code", Matchers.equalTo("12345678"))
            .json("link", Matchers.equalTo("https://remsfal.de"));
    }

}
