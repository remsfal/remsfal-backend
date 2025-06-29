package de.remsfal.notification.resource;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class MailResourceTest {

    static final String BASE_PATH = "/notification/test";

    @Test
    void shouldFail_whenMissingRecipient() {
        given()
                .queryParam("name", "NoRecipient")
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void shouldFail_whenTemplateIsUnknown() {
        given()
                .queryParam("to", "test@example.com")
                .queryParam("name", "TestUser")
                .queryParam("template", "unknown")
                .queryParam("link", "https://remsfal.de")
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(422);
    }
}