package de.remsfal.service.boundary;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import de.remsfal.core.dto.ImmutableUserJson;
import de.remsfal.core.dto.UserJson;
import de.remsfal.service.TestData;
import de.remsfal.service.boundary.authentication.TokenInfo;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.when;

import java.util.UUID;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

@QuarkusTest
class UserResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/users";

    @Test
    void getUser_FAILED_userNotExist() {
        given()
            .header("Authorization", "Bearer " + TestData.USER_TOKEN)
            .when().get(BASE_PATH + "/" + UUID.randomUUID())
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getUser_FAILED_noAuthentication() {
        given()
            .contentType(ContentType.JSON)
            .when().get(BASE_PATH + "/anyId")
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getProjects_FAILED_noAuthentication() {
        given()
            .contentType(ContentType.JSON)
            .when().get("/api/v1/projects")
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testCreateAndGet() {
        final UserJson user = ImmutableUserJson.builder()
            .name(TestData.USER_NAME)
            .email(TestData.USER_EMAIL)
            .build();

        when(tokenValidator.validate("Bearer " + TestData.USER_TOKEN))
            .thenReturn(new TokenInfo(ImmutableUserJson.builder()
                .id(TestData.USER_TOKEN)
                .email(TestData.USER_EMAIL)
                .name(TestData.USER_NAME)
                .build()));

        final Response res = given()
            .when()
            .header("Authorization", "Bearer " + TestData.USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .body(user)
            .post(BASE_PATH)
            .thenReturn();

        res.then()
            .statusCode(Status.CREATED.getStatusCode())
            .header("location", Matchers.startsWith("http://localhost:8081/api/v1/users"));

        final String userResourceUrl = res.header("location");

        given()
            .when()
            .header("Authorization", "Bearer " + TestData.USER_TOKEN)
            .get(userResourceUrl)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("name", Matchers.equalTo(TestData.USER_NAME))
            .and().body("email", Matchers.equalTo(TestData.USER_EMAIL));
    }

}