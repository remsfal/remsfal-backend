package de.remsfal.notification.boundary;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.notification.control.EmailSettingsController;
import de.remsfal.test.AbstractTest;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class EmailSettingsResourceTest extends AbstractTest {

    static final String BASE_PATH = "/notification/v1/settings/issues/emails";

    @Inject
    EmailSettingsController controller;

    @BeforeEach
    void cleanSettings() {
        controller.deleteSettings(TestData.USER_ID);
    }

    @Test
    void getEmailSettings_FAILED_noAuthentication() {
        given()
            .when()
            .get(BASE_PATH)
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getEmailSettings_FAILED_noManager() {
        given()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, TestData.USER_NAME,
                Map.of(), Map.of(), Map.of(TestData.AGREEMENT_ID.toString(), TestData.PROJECT_ID.toString())))
            .when()
            .get(BASE_PATH)
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void updateEmailSettings_FAILED_noManager() {
        given()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, TestData.USER_NAME,
                Map.of(), Map.of(), Map.of()))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"issueCreated\": false}")
            .when()
            .patch(BASE_PATH)
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void getEmailSettings_SUCCESS_defaultsWithoutStoredSettings() {
        given()
            .cookie(buildManagerCookie())
            .when()
            .get(BASE_PATH)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .body("userId", equalTo(TestData.USER_ID.toString()))
            .body("issueCreated", equalTo(true))
            .body("issueUpdated", equalTo(true))
            .body("issueAssigned", equalTo(true))
            .body("issueMessageCreated", equalTo(true));
    }

    @Test
    void updateEmailSettings_SUCCESS_onlyProvidedFlagsChanged() {
        given()
            .cookie(buildManagerCookie())
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"issueUpdated\": false}")
            .when()
            .patch(BASE_PATH)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .body("issueCreated", equalTo(true))
            .body("issueUpdated", equalTo(false))
            .body("issueAssigned", equalTo(true))
            .body("issueMessageCreated", equalTo(true));

        given()
            .cookie(buildManagerCookie())
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"issueMessageCreated\": false, \"issueUpdated\": true}")
            .when()
            .patch(BASE_PATH)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .body("issueUpdated", equalTo(true))
            .body("issueMessageCreated", equalTo(false));

        given()
            .cookie(buildManagerCookie())
            .when()
            .get(BASE_PATH)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .body("issueCreated", equalTo(true))
            .body("issueUpdated", equalTo(true))
            .body("issueAssigned", equalTo(true))
            .body("issueMessageCreated", equalTo(false));
    }

    @Test
    void updateEmailSettings_FAILED_userIdNotAllowed() {
        given()
            .cookie(buildManagerCookie())
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"userId\": \"" + TestData.USER_ID_2 + "\", \"issueCreated\": false}")
            .when()
            .patch(BASE_PATH)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void updateEmailSettings_FAILED_missingBody() {
        given()
            .cookie(buildManagerCookie())
            .contentType(MediaType.APPLICATION_JSON)
            .when()
            .patch(BASE_PATH)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

}
