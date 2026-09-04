package de.remsfal.service.control;

import java.util.Set;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.eventing.EmailEventJson;
import de.remsfal.core.json.organization.ImmutableOrganizationJson;
import de.remsfal.core.json.organization.OrganizationJson;
import de.remsfal.core.json.project.ImmutableProjectJson;
import de.remsfal.core.json.project.ProjectJson;
import de.remsfal.core.model.CustomerModel;
import de.remsfal.test.TestData;
import de.remsfal.test.kafka.AbstractKafkaTest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.KafkaCompanionResource;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class NotificationControllerTest extends AbstractKafkaTest {

    @Inject
    NotificationController notificationController;

    @Test
    void testInformUserAboutProjectMembership() {
        CustomerModel user =
                ImmutableUserJson.builder()
                        .id(TestData.USER_ID)
                        .email(TestData.USER_EMAIL)
                        .build();

        ProjectJson project = ImmutableProjectJson.builder()
                .id(TestData.PROJECT_ID)
                .title("Test Project")
                .members(Set.of())
                .build();

        notificationController.informUserAboutProjectMembership(user, project);

        given()
            .topic(EmailEventJson.TOPIC)
        .assertThat()
            .json("user.id", Matchers.equalTo(TestData.USER_ID.toString()))
            .json("user.email", Matchers.equalTo(TestData.USER_EMAIL))
            .json("user.locale", Matchers.equalTo("de"))
            .json("notificationEventType", Matchers.equalTo("PROJECT_ADMISSION"))
            .json("project.title", Matchers.equalTo("Test Project"))
            .json("link", Matchers.equalTo("https://remsfal.de/projects/" + TestData.PROJECT_ID));
    }

    @Test
    void testInformUserAboutOrganizationMembership() {
        CustomerModel user =
                ImmutableUserJson.builder()
                        .id(TestData.USER_ID)
                        .email(TestData.USER_EMAIL)
                        .build();

        OrganizationJson organization = ImmutableOrganizationJson.builder()
                .id(TestData.ORGANIZATION_ID)
                .name("Test Organization")
                .phone("+491234567890")
                .email("organization@example.com")
                .trade("Property Management")
                .vatIdentificationNumber("DE123456789")
                .build();

        notificationController.informUserAboutOrganizationMembership(user, organization);

        given()
            .topic(EmailEventJson.TOPIC)
        .assertThat()
            .json("user.id", Matchers.equalTo(TestData.USER_ID.toString()))
            .json("user.email", Matchers.equalTo(TestData.USER_EMAIL))
            .json("user.locale", Matchers.equalTo("de"))
            .json("notificationEventType", Matchers.equalTo("ORGANIZATION_ADMISSION"))
            .json("organization.name", Matchers.equalTo("Test Organization"))
            .json("link", Matchers.equalTo("https://remsfal.de/organizations/" + TestData.ORGANIZATION_ID));
    }

    @Test
    void testInformUserAboutRegistration() {
        CustomerModel user =
            ImmutableUserJson.builder()
                    .id(TestData.USER_ID)
                    .email(TestData.USER_EMAIL)
                    .build();

        notificationController.informUserAboutRegistration(user);

        given()
            .topic(EmailEventJson.TOPIC)
        .assertThat()
            .json("user.id", Matchers.equalTo(TestData.USER_ID.toString()))
            .json("user.email", Matchers.equalTo(TestData.USER_EMAIL))
            .json("user.locale", Matchers.equalTo("de"))
            .json("notificationEventType", Matchers.equalTo("USER_REGISTRATION"))
            .json("link", Matchers.equalTo("https://remsfal.de"));
    }

    @Test
    void testInformUserAboutRegistration_usesUserLocaleOverDefault() {
        CustomerModel user =
            ImmutableUserJson.builder()
                    .id(TestData.USER_ID)
                    .email(TestData.USER_EMAIL)
                    .locale("en")
                    .build();

        notificationController.informUserAboutRegistration(user);

        given()
            .topic(EmailEventJson.TOPIC)
        .assertThat()
            .json("user.id", Matchers.equalTo(TestData.USER_ID.toString()))
            .json("user.locale", Matchers.equalTo("en"));
    }

}
