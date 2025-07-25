package de.remsfal.service.boundary;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.matcher.RestAssuredMatchers;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.json.webtoken.JsonWebSignature;

import de.remsfal.service.boundary.authentication.GoogleAuthenticator;
import de.remsfal.test.TestData;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response.Status;

import java.util.concurrent.TimeUnit;

@QuarkusTest
class AuthenticationResourceMockitoTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/authentication";

    @InjectMock
    protected GoogleAuthenticator authenticator;

    @Inject
    protected AuthenticationEventObserver observer;

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
            .cookie("remsfal_access_token", RestAssuredMatchers.detailedCookie()
                .path("/")
                .sameSite("Strict")
                .maxAge(60 * 5))
            .cookie("remsfal_refresh_token", RestAssuredMatchers.detailedCookie()
                .path("/")
                .sameSite("Strict")
                .maxAge(60 * 60 * 24 * 7));

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
            .cookie("remsfal_access_token", RestAssuredMatchers.detailedCookie()
                    .path("/")
                    .sameSite("Strict")
                    .maxAge(60 * 5))
            .cookie("remsfal_refresh_token", RestAssuredMatchers.detailedCookie()
                    .path("/")
                    .sameSite("Strict")
                    .maxAge(60 * 60 * 24 * 7));

        enties = entityManager
            .createQuery("SELECT count(user) FROM UserEntity user where user.email = :email", Long.class)
            .setParameter("email", TestData.USER_EMAIL_1)
            .getSingleResult();
        assertEquals(1, enties);

        await()
            .atMost(5, TimeUnit.SECONDS)       // Maximum time to wait
            .pollInterval(500, TimeUnit.MILLISECONDS) // frequency of checking
            .until(() -> observer.getNumberOfEvents() == 1);  // Condition to wait for
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