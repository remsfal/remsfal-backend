package de.remsfal.service.boundary;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.matcher.RestAssuredMatchers;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import jakarta.ws.rs.core.Response.Status;

@QuarkusTest
class AuthenticationResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/authentication";

    static final String REDIRECT_URI_URL = "http%3A%2F%2Flocalhost%3A8081%2Fapi%2Fv1%2Fauthentication%2Fsession";

    @Test
    void login_SUCCESS_userIsRedirected() {
        given()
            .redirects().follow(false)
            .when().get(BASE_PATH + "/login")
            .then()
            .statusCode(Status.FOUND.getStatusCode())
            .header("location", Matchers.startsWith("https://accounts.google.com/o/oauth2/auth?"))
            .header("location", Matchers.containsString("response_type=code"))
            .header("location", Matchers.containsString("client_id="))
            .header("location", Matchers.containsString("redirect_uri="))
            .header("location", Matchers.containsString(REDIRECT_URI_URL))
            .header("location", Matchers.containsString("scope=openid+email"))
            .header("location", Matchers.containsString("state=%2F"));
    }

    @Test
    void login_SUCCESS_userProvidesState() {
        final String route = "%2Fmy%2Fcallback%2Froute";

        given()
            .queryParam("route", route)
            .redirects().follow(false)
            .when().get(BASE_PATH + "/login")
            .then()
            .statusCode(Status.FOUND.getStatusCode())
            .header("location", Matchers.startsWith("https://accounts.google.com/o/oauth2/auth?"))
            .header("location", Matchers.containsString("response_type=code"))
            .header("location", Matchers.containsString("client_id="))
            .header("location", Matchers.containsString("redirect_uri="))
            .header("location", Matchers.containsString(REDIRECT_URI_URL))
            .header("location", Matchers.containsString("scope=openid+email"))
            .header("location", Matchers.containsString("state=" + route));
    }

    @Test
    void login_SUCCESS_proxyConfiguration() {
        final String proxyRedirectUri = REDIRECT_URI_URL
            .replace("localhost", "example.org")
            .replace("8081", "8899");

        given()
            .redirects().follow(false)
            .when()
            .header("X-Forwarded-Host", "example.org:8899")
            .get(BASE_PATH + "/login")
            .then()
            .statusCode(Status.FOUND.getStatusCode())
            .header("location", Matchers.startsWith("https://accounts.google.com/o/oauth2/auth?"))
            .header("location", Matchers.containsString("response_type=code"))
            .header("location", Matchers.containsString("client_id="))
            .header("location", Matchers.containsString("redirect_uri="))
            .header("location", Matchers.containsString(proxyRedirectUri))
            .header("location", Matchers.containsString("scope=openid+email"))
            .header("location", Matchers.containsString("state=%2F"));
    }

    @Test
    void login_FAILED_parentDirectoryRelativePath() {
        given()
            .when().get(BASE_PATH + "/login/../../user")
            .then()
            .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void logout_SUCCESS_CookieIsRemoved() {
        given()
            .redirects().follow(false)
            .when().get(BASE_PATH + "/logout")
            .then()
            .statusCode(Status.FOUND.getStatusCode())
            .header("location", Matchers.equalTo("http://localhost:8081/"))
            .cookie("remsfal_access_token", RestAssuredMatchers.detailedCookie()
                    .path("/")
                    .sameSite("Strict")
                    .maxAge(0))
            .cookie("remsfal_refresh_token", RestAssuredMatchers.detailedCookie()
                    .path("/api")
                    .sameSite("Strict")
                    .maxAge(0));
    }

    @Test
    void jwks_SUCCESS_returnsKeySet() {
        given()
            .when().get(BASE_PATH + "/jwks")
            .then()
            .statusCode(Status.OK.getStatusCode())
            .body("keys[0].kty", Matchers.equalTo("RSA"))
            .body("keys[0].kid", Matchers.equalTo("remsfal-platform-key"))
            .body("keys[0].alg", Matchers.equalTo("RS256"));
    }

}
