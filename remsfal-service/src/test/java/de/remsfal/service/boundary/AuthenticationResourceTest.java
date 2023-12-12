package de.remsfal.service.boundary;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.matcher.RestAssuredMatchers;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import de.remsfal.service.TestData;
import de.remsfal.service.boundary.authentication.GoogleAuthenticator;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response.Status;

@QuarkusTest
class AuthenticationResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/authentication";

    @InjectMock
    protected GoogleAuthenticator authenticator;

    @Test
    void login_SUCCESS_userIsRedirected() {
        when(authenticator.getAuthorizationCodeURI(anyString(), eq("/")))
            .thenCallRealMethod();
        
        given()
            .when().get(BASE_PATH + "/login")
            .then()
            .statusCode(Status.FOUND.getStatusCode())
            .header("location", Matchers.startsWith("https://accounts.google.com/o/oauth2/auth?"))
            .header("location", Matchers.containsString("response_type=code"))
            .header("location", Matchers.containsString("client_id="))
            .header("location", Matchers.containsString("redirect_uri="))
            .header("location", Matchers.containsString(BASE_PATH + "/session"))
            .header("location", Matchers.containsString("scope=openid%20email"))
            .header("location", Matchers.containsString("state=/"));
    }

    @Test
    void login_SUCCESS_userProvidesState() {
        final String route = "/my/callback/route";
        when(authenticator.getAuthorizationCodeURI(anyString(), eq(route)))
            .thenCallRealMethod();

        given()
            .queryParam("route", route)
            .when().get(BASE_PATH + "/login")
            .then()
            .statusCode(Status.FOUND.getStatusCode())
            .header("location", Matchers.startsWith("https://accounts.google.com/o/oauth2/auth?"))
            .header("location", Matchers.containsString("response_type=code"))
            .header("location", Matchers.containsString("client_id="))
            .header("location", Matchers.containsString("redirect_uri="))
            .header("location", Matchers.containsString(BASE_PATH + "/session"))
            .header("location", Matchers.containsString("scope=openid%20email"))
            .header("location", Matchers.containsString("state=" + route));
    }

    @Test
    void login_FAILED_parentDirectoryRelativePath() {
        given()
            .when().get(BASE_PATH + "/login/../../user")
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void session_SUCCESS_userIsCreated() {
        final String code = "anyRandomCode";
        final GoogleIdToken.Payload idTokenPayload = new GoogleIdToken.Payload()
            .setSubject(TestData.USER_TOKEN_1)
            .setEmail(TestData.USER_EMAIL_1)
            .set("name", TestData.USER_NAME_1);

        when(authenticator.getIdToken(eq(code), any()))
            .thenReturn(new GoogleIdToken(null, idTokenPayload, null, null));

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
            .get(BASE_PATH + "/session")
            .then()
            .statusCode(Status.FOUND.getStatusCode())
            .header("location", Matchers.equalTo("http://localhost:8081/my/callback"))
            .cookie("remsfal_session", RestAssuredMatchers.detailedCookie()
                .path("/")
                .sameSite("STRICT")
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
            .setEmail(TestData.USER_EMAIL_1)
            .set("name", TestData.USER_NAME_1);

        when(authenticator.getIdToken(eq(code), any()))
            .thenReturn(new GoogleIdToken(null, idTokenPayload, null, null));

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
            .get(BASE_PATH + "/session")
            .then()
            .statusCode(Status.FOUND.getStatusCode())
            .header("location", Matchers.equalTo("http://localhost:8081/"))
            .cookie("remsfal_session", RestAssuredMatchers.detailedCookie()
                .path("/")
                .sameSite("STRICT")
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
    void session_FAILED_ErrorIsProvided() {
        given()
            .queryParam("error", "Any Error from Google")
            .when()
            .get(BASE_PATH + "/session")
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void logout_SUCCESS_CookieIsRemoved() {
        given()
            .when()
            .get(BASE_PATH + "/logout")
            .then()
            .statusCode(Status.FOUND.getStatusCode())
            .header("location", Matchers.equalTo("http://localhost:8081/"))
            .cookie("remsfal_session", RestAssuredMatchers.detailedCookie()
                .value("")
                .path("/")
                .sameSite("STRICT")
                .maxAge(0));
    }

}