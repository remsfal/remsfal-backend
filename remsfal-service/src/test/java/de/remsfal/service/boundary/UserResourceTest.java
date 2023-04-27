package de.remsfal.service.boundary;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import de.remsfal.core.dto.ImmutableUserJson;
import de.remsfal.core.dto.UserJson;
import de.remsfal.service.AbstractTest;

import static io.restassured.RestAssured.given;

import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

@QuarkusTest
class UserResourceTest extends AbstractTest {

    static final String BASE_PATH = "/api/v2/users";

    static {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @Test
    void testNotFound() {
        given()
            .when().get(BASE_PATH + "/" + UUID.randomUUID())
            .then()
            .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testInvalidUserId() {
        given()
            .when().get(BASE_PATH + "/anyId")
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testCreateAndGet() {
        final UserJson user = ImmutableUserJson.builder()
            .name("Test")
            .email("any@example.org")
            .build();
        final Response res = given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .body(user)
            .post(BASE_PATH)
            .thenReturn();

        res.then()
            .statusCode(Status.CREATED.getStatusCode())
            .header("location", Matchers.startsWith("http://localhost:8081/api/v2/users"));

        final String userResourceUrl = res.header("location");

        given()
            .when().get(userResourceUrl)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("user_name", Matchers.equalTo("Test"))
            .and().body("user_email", Matchers.equalTo("any@example.org"));
    }

}