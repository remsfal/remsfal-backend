package de.remsfal.service.boundary;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.service.TestData;
import de.remsfal.service.boundary.authentication.TokenInfo;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

@QuarkusTest
class UserResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/user";

    @Test
    void getUser_FAILED_userNotExist() {
        when(tokenValidator.validate("Bearer " + TestData.USER_TOKEN))
            .thenReturn(new TokenInfo(new Payload()
                .setSubject(TestData.USER_TOKEN)
                .setEmail(TestData.USER_EMAIL)
                .set("name", TestData.USER_NAME)));
        
        given()
            .header("Authorization", "Bearer " + TestData.USER_TOKEN)
            .when().get(BASE_PATH)
            .then()
            .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void getUser_FAILED_noAuthentication() {
        given()
            .contentType(ContentType.JSON)
            .when().get(BASE_PATH)
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
            .thenReturn(new TokenInfo(new Payload()
                .setSubject(TestData.USER_TOKEN)
                .setEmail(TestData.USER_EMAIL)
                .set("name", TestData.USER_NAME)));

        given()
            .when()
            .header("Authorization", "Bearer " + TestData.USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .body(user)
            .post(BASE_PATH)
            .then()
            .statusCode(Status.OK.getStatusCode());

        given()
            .when()
            .header("Authorization", "Bearer " + TestData.USER_TOKEN)
            .get(BASE_PATH)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("name", Matchers.equalTo(TestData.USER_NAME))
            .and().body("email", Matchers.equalTo(TestData.USER_EMAIL));
    }

}