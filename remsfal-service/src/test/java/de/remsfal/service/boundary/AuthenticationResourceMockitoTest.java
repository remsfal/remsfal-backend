package de.remsfal.service.boundary;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.matcher.RestAssuredMatchers;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.json.webtoken.JsonWebSignature;
import de.remsfal.service.TestData;
import de.remsfal.service.boundary.authentication.GoogleAuthenticator;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response.Status;

@QuarkusTest
class AuthenticationResourceMockitoTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/authentication";

    @InjectMock
    protected GoogleAuthenticator authenticator;

    @Test
    void session_SUCCESS_userIsCreated() {
        final String code = "anyRandomCode";
        final GoogleIdToken.Payload idTokenPayload = new GoogleIdToken.Payload()
            .setSubject(TestData.USER_TOKEN_1)
            .setEmail(TestData.USER_EMAIL_1);

        when(authenticator.getIdToken(eq(code), any()))
            .thenReturn(new GoogleIdToken(new JsonWebSignature.Header(), idTokenPayload, new byte[1], new byte[1]));

        long enties = entityManager
            .createQuery("SELECT count(user) FROM UserEntity user where user.email = :email", Long.class)
            .setParameter("email", TestData.USER_EMAIL_1)
            .getSingleResult();
        assertEquals(0, enties);

        given()
            .when()
            .queryParam("code", code)
            .queryParam("state", "/my/callback")
            .queryParam("anyOtherParam", "toBeIgnored")
            .redirects().follow(false)
            .get(BASE_PATH + "/session")
            .then()
            .statusCode(Status.FOUND.getStatusCode())
            .header("location", Matchers.equalTo("http://localhost:8081/my/callback"))
            .cookie("remsfal_session", RestAssuredMatchers.detailedCookie()
                .path("/")
                .sameSite("Strict")
                .maxAge(60 * 30));

        enties = entityManager
            .createQuery("SELECT count(user) FROM UserEntity user where user.email = :email", Long.class)
            .setParameter("email", TestData.USER_EMAIL_1)
            .getSingleResult();
        assertEquals(1, enties);
    }

    @Test
    void session_SUCCESS_userAlreadyExists() {
        final String code = "anyRandomCode";
        final GoogleIdToken.Payload idTokenPayload = new GoogleIdToken.Payload()
            .setSubject(TestData.USER_TOKEN_1)
            .setEmail(TestData.USER_EMAIL_1);

        when(authenticator.getIdToken(eq(code), any()))
            .thenReturn(new GoogleIdToken(new JsonWebSignature.Header(), idTokenPayload, new byte[1], new byte[1]));

        setupTestUsers();
        long enties = entityManager
            .createQuery("SELECT count(user) FROM UserEntity user where user.email = :email", Long.class)
            .setParameter("email", TestData.USER_EMAIL_1)
            .getSingleResult();
        assertEquals(1, enties);

        given()
            .when()
            .queryParam("code", code)
            .queryParam("anyOtherParam", "toBeIgnored")
            .redirects().follow(false)
            .get(BASE_PATH + "/session")
            .then()
            .statusCode(Status.FOUND.getStatusCode())
            .header("location", Matchers.equalTo("http://localhost:8081/"))
            .cookie("remsfal_session", RestAssuredMatchers.detailedCookie()
                .path("/")
                .sameSite("Strict")
                .maxAge(60 * 30));

        enties = entityManager
            .createQuery("SELECT count(user) FROM UserEntity user where user.email = :email", Long.class)
            .setParameter("email", TestData.USER_EMAIL_1)
            .getSingleResult();
        assertEquals(1, enties);
        // TODO: Use Awaitility to test AuthenticationEvent
    }

    @Test
    void session_FAILED_noCode() {
        given()
            .when()
            .get(BASE_PATH + "/session")
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void session_FAILED_errorIsProvided() {
        given()
            .queryParam("error", "Any Error from Google")
            .when()
            .get(BASE_PATH + "/session")
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void session_FAILED_invalidToken() {
        when(authenticator.getIdToken(any(), any()))
            .thenReturn(null);

        given()
            .when()
            .queryParam("code", "anyValidCode")
            .get(BASE_PATH + "/session")
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

}