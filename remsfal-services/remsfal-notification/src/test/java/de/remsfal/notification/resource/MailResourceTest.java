package de.remsfal.notification.resource;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class MailResourceTest {

    static final String BASE_PATH = "/notification/test";

    @Test
    void sendWelcomeTemplate_SUCCESS() {
        given()
                .queryParam("to", "test@example.com")
                .queryParam("subject", "Welcome")
                .queryParam("name", "TestUser")
                .queryParam("template", "welcome")
                .queryParam("token", "abc123")
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(Status.ACCEPTED.getStatusCode());
    }

    @Test
    void sendNewMembershipTemplate_SUCCESS() {
        given()
                .queryParam("to", "test@example.com")
                .queryParam("subject", "Welcome")
                .queryParam("name", "TestUser")
                .queryParam("template", "new-membership")
                .queryParam("token", "abc123")
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(Status.ACCEPTED.getStatusCode());
    }

    @Test
    void missingParams_FAILED() {
        given()
                .queryParam("name", "NoRecipient")
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    void missingRequiredParameters_FAILED() {
        given()
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
}