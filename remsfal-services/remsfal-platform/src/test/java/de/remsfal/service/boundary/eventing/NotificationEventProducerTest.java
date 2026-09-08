package de.remsfal.service.boundary.eventing;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.NotificationEventJson;
import de.remsfal.core.json.organization.ImmutableOrganizationJson;
import de.remsfal.core.json.organization.OrganizationJson;
import de.remsfal.core.json.project.ImmutableProjectJson;
import de.remsfal.core.json.project.ProjectJson;
import de.remsfal.test.TestData;
import de.remsfal.test.kafka.AbstractKafkaTest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.KafkaCompanionResource;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.Set;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class NotificationEventProducerTest extends AbstractKafkaTest {

    @Inject
    NotificationEventProducer producer;

    @Test
    void testSendUserRegistration_publishesEventToTopic() {
        UserJson user = ImmutableUserJson.builder()
            .id(TestData.USER_ID)
            .email(TestData.USER_EMAIL)
            .locale("de")
            .build();

        producer.sendUserRegistration(user, "https://remsfal.de");

        given()
            .topic(NotificationEventJson.TOPIC)
        .assertThat()
            .json("user.id", Matchers.equalTo(TestData.USER_ID.toString()))
            .json("user.email", Matchers.equalTo(TestData.USER_EMAIL))
            .json("notificationEventType", Matchers.equalTo("USER_REGISTRATION"))
            .json("link", Matchers.equalTo("https://remsfal.de"));
    }

    @Test
    void testSendProjectAdmission_publishesEventToTopic() {
        UserJson user = ImmutableUserJson.builder()
            .id(TestData.USER_ID)
            .email(TestData.USER_EMAIL)
            .locale("de")
            .build();
        ProjectJson project = ImmutableProjectJson.builder()
            .id(TestData.PROJECT_ID)
            .title("Test Project")
            .members(Set.of())
            .build();

        producer.sendProjectAdmission(user, "https://remsfal.de/projects/" + TestData.PROJECT_ID, project);

        given()
            .topic(NotificationEventJson.TOPIC)
        .assertThat()
            .json("notificationEventType", Matchers.equalTo("PROJECT_ADMISSION"))
            .json("project.title", Matchers.equalTo("Test Project"))
            .json("link", Matchers.equalTo("https://remsfal.de/projects/" + TestData.PROJECT_ID));
    }

    @Test
    void testSendOrganizationAdmission_publishesEventToTopic() {
        UserJson user = ImmutableUserJson.builder()
            .id(TestData.USER_ID)
            .email(TestData.USER_EMAIL)
            .locale("de")
            .build();
        OrganizationJson organization = ImmutableOrganizationJson.builder()
            .id(TestData.ORGANIZATION_ID)
            .name("Test Organization")
            .phone("+491234567890")
            .email("organization@example.com")
            .trade("Property Management")
            .vatIdentificationNumber("DE123456789")
            .build();

        producer.sendOrganizationAdmission(user,
            "https://remsfal.de/organizations/" + TestData.ORGANIZATION_ID, organization);

        given()
            .topic(NotificationEventJson.TOPIC)
        .assertThat()
            .json("notificationEventType", Matchers.equalTo("ORGANIZATION_ADMISSION"))
            .json("organization.name", Matchers.equalTo("Test Organization"))
            .json("link", Matchers.equalTo("https://remsfal.de/organizations/" + TestData.ORGANIZATION_ID));
    }

    @Test
    void testSendAdditionalEmailVerification_publishesEventToTopic() {
        UserJson user = ImmutableUserJson.builder()
            .id(TestData.USER_ID)
            .email(TestData.USER_EMAIL)
            .locale("de")
            .build();

        producer.sendAdditionalEmailVerification(user,
            "https://remsfal.de/api/v1/authentication/verify-additional-email?token=abc");

        given()
            .topic(NotificationEventJson.TOPIC)
        .assertThat()
            .json("notificationEventType", Matchers.equalTo("ADDITIONAL_EMAIL_VERIFICATION"))
            .json("link", Matchers.equalTo(
                "https://remsfal.de/api/v1/authentication/verify-additional-email?token=abc"));
    }

}
