package de.remsfal.notification.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
public class MailResourceTest {

    @Test
    public void testWelcomeTemplateResponse() {
        RestAssured.given()
                .queryParam("to", "test@example.com")
                .queryParam("subject", "Welcome")
                .queryParam("name", "TestUser")
                .queryParam("template", "welcome")
                .queryParam("token", "abc123")
                .when()
                .get("/notification/test")
                .then()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());
    }

    @Test
    public void testMissingParams() {
        RestAssured.given()
                .queryParam("name", "NoRecipient")
                .when()
                .get("/notification/test")
                .then()
                .statusCode(500); // because IllegalArgumentException will be thrown
    }
    @Test
    public void testMissingRequiredParameters() {
        given()
                .when()
                .get("/notification/test")  // without required query parameters
                .then()
                .statusCode(500);
    }
}
