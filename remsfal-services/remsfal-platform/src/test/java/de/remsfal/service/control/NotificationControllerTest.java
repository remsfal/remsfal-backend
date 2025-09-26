package de.remsfal.service.control;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.eventing.EmailEventJson;
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
                        .id(TestData.USER_ID.toString())
                        .email(TestData.USER_EMAIL)
                        .build();

        notificationController.informUserAboutProjectMembership(user, "fakeId");

        given()
            .topic(EmailEventJson.TOPIC)
        .assertThat()
            .json("user.id", Matchers.equalTo(TestData.USER_ID.toString()))
            .json("user.email", Matchers.equalTo(TestData.USER_EMAIL))
            .json("type", Matchers.equalTo("PROJECT_ADMISSION"))
            .json("link", Matchers.equalTo("https://remsfal.de/projects/fakeId"));
    }

    @Test
    void testInformUserAboutRegistration() {
        CustomerModel user =
            ImmutableUserJson.builder()
                    .id(TestData.USER_ID.toString())
                    .email(TestData.USER_EMAIL)
                    .build();

        notificationController.informUserAboutRegistration(user);

        given()
            .topic(EmailEventJson.TOPIC)
        .assertThat()
            .json("user.id", Matchers.equalTo(TestData.USER_ID.toString()))
            .json("user.email", Matchers.equalTo(TestData.USER_EMAIL))
            .json("type", Matchers.equalTo("USER_REGISTRATION"))
            .json("link", Matchers.equalTo("https://remsfal.de"));
    }

}
