package de.remsfal.service.boundary;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.matcher.RestAssuredMatchers;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import de.remsfal.test.TestData;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import jakarta.ws.rs.core.Response.Status;

import java.time.LocalDateTime;
import java.util.UUID;

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

    @Test
    void refresh_SUCCESS_tokensAreRefreshed() {
        // Don't use setupTestUsers() here as it inserts a random refresh token
        // Instead, build the refresh token cookie which will create the proper token in the database
        insertAddress(TestData.ADDRESS_ID_1, TestData.ADDRESS_STREET_1,
            TestData.ADDRESS_CITY_1, TestData.ADDRESS_PROVINCE_1,
            TestData.ADDRESS_ZIP_1, TestData.ADDRESS_COUNTRY_1);
        insertUser(TestData.USER_ID_1, TestData.USER_TOKEN_1,
            TestData.USER_EMAIL_1, TestData.USER_FIRST_NAME_1,
            TestData.USER_LAST_NAME_1, TestData.ADDRESS_ID_1);

        final var refreshCookie = buildRefreshTokenCookie(
            TestData.USER_ID_1,
            TestData.USER_EMAIL_1,
            null);

        given()
            .cookie(refreshCookie)
            .when().post(BASE_PATH + "/refresh")
            .then()
            .statusCode(Status.NO_CONTENT.getStatusCode())
            .cookie("remsfal_access_token", RestAssuredMatchers.detailedCookie()
                    .path("/")
                    .sameSite("Strict")
                    .secured(true))
            .cookie("remsfal_refresh_token", RestAssuredMatchers.detailedCookie()
                    .path("/api")
                    .sameSite("Strict")
                    .secured(true));
    }

    @Test
    void refresh_FAILED_noRefreshToken() {
        given()
            .when().post(BASE_PATH + "/refresh")
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void verifyAdditionalEmail_SUCCESS_marksAdditionalEmailAsVerified() {
        final UUID additionalEmailId = UUID.randomUUID();
        final String verificationToken = UUID.randomUUID().toString();

        insertAddress(TestData.ADDRESS_ID_1, TestData.ADDRESS_STREET_1,
            TestData.ADDRESS_CITY_1, TestData.ADDRESS_PROVINCE_1,
            TestData.ADDRESS_ZIP_1, TestData.ADDRESS_COUNTRY_1);
        insertUser(TestData.USER_ID_1, TestData.USER_TOKEN_1,
            TestData.USER_EMAIL_1, TestData.USER_FIRST_NAME_1,
            TestData.USER_LAST_NAME_1, TestData.ADDRESS_ID_1);

        runInTransaction(() -> entityManager.createNativeQuery(
            "INSERT INTO user_additional_email (id, user_id, email, verified, verification_token, verification_token_expires_at) "
                + "VALUES (?,?,?,?,?,?)")
            .setParameter(1, additionalEmailId)
            .setParameter(2, TestData.USER_ID_1)
            .setParameter(3, TestData.ALTERNATIVE_EMAIL_1)
            .setParameter(4, false)
            .setParameter(5, verificationToken)
            .setParameter(6, LocalDateTime.now().plusHours(1))
            .executeUpdate());

        given()
            .queryParam("token", verificationToken)
            .when().get(BASE_PATH + "/verify-additional-email")
            .then()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        final Boolean verified = runInTransaction(() -> entityManager.createQuery(
            "SELECT ae.verified FROM AdditionalEmailEntity ae WHERE ae.id = :id",
            Boolean.class)
            .setParameter("id", additionalEmailId)
            .getSingleResult());
        assertEquals(Boolean.TRUE, verified);
    }

    @Test
    void verifyAdditionalEmail_FAIL_tokenExpired() {
        final UUID additionalEmailId = UUID.randomUUID();
        final String verificationToken = UUID.randomUUID().toString();

        insertAddress(TestData.ADDRESS_ID_1, TestData.ADDRESS_STREET_1,
            TestData.ADDRESS_CITY_1, TestData.ADDRESS_PROVINCE_1,
            TestData.ADDRESS_ZIP_1, TestData.ADDRESS_COUNTRY_1);
        insertUser(TestData.USER_ID_1, TestData.USER_TOKEN_1,
            TestData.USER_EMAIL_1, TestData.USER_FIRST_NAME_1,
            TestData.USER_LAST_NAME_1, TestData.ADDRESS_ID_1);

        runInTransaction(() -> entityManager.createNativeQuery(
            "INSERT INTO user_additional_email (id, user_id, email, verified, verification_token, verification_token_expires_at) "
                + "VALUES (?,?,?,?,?,?)")
            .setParameter(1, additionalEmailId)
            .setParameter(2, TestData.USER_ID_1)
            .setParameter(3, TestData.ALTERNATIVE_EMAIL_1)
            .setParameter(4, false)
            .setParameter(5, verificationToken)
            .setParameter(6, LocalDateTime.now().minusHours(1))
            .executeUpdate());

        given()
            .queryParam("token", verificationToken)
            .when().get(BASE_PATH + "/verify-additional-email")
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

}
