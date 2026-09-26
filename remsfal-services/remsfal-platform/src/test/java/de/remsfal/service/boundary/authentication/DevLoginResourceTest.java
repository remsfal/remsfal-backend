package de.remsfal.service.boundary.authentication;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Map;

import jakarta.ws.rs.core.Response.Status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import de.remsfal.service.boundary.AbstractResourceTest;
import de.remsfal.service.control.DevDataSeedController;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DevLoginResourceTest.DevLoginProfile.class)
class DevLoginResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/authentication";
    static final String DEV_LOGIN_PATH = BASE_PATH + "/dev-login";

    public static class DevLoginProfile implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(DevLoginResource.ENABLED_PROPERTY, "true");
        }

    }

    @Test
    void login_SUCCESS_redirectsToDevLogin() {
        given()
            .queryParam("route", "/projects")
            .redirects().follow(false)
            .when().get(BASE_PATH + "/login")
            .then()
            .statusCode(Status.FOUND.getStatusCode())
            .header("location", Matchers.containsString(DEV_LOGIN_PATH + "?route="))
            .header("location", Matchers.containsString("projects"));
    }

    @Test
    void loginPage_SUCCESS_listsSeedUsers() {
        given()
            .queryParam("route", "/projects")
            .when().get(DEV_LOGIN_PATH)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(Matchers.startsWith("text/html"))
            .body(Matchers.containsString(DevDataSeedController.MANAGER.email()))
            .body(Matchers.containsString(DevDataSeedController.TENANT.email()))
            .body(Matchers.containsString(DevDataSeedController.CONTRACTOR.email()));
    }

    @Test
    void loginPage_SUCCESS_doesNotReflectRoute() {
        given()
            .queryParam("route", "/\"><script>alert(1)</script>")
            .when().get(DEV_LOGIN_PATH)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .body(Matchers.not(Matchers.containsString("alert(1)")))
            .body(Matchers.not(Matchers.containsString("action=")));
    }

    @Test
    void login_SUCCESS_createsUserAndSession() {
        final String email = "Someone@Remsfal.dev";

        given()
            .formParam("email", email)
            .queryParam("route", "/projects")
            .redirects().follow(false)
            .when().post(DEV_LOGIN_PATH)
            .then()
            .statusCode(Status.FOUND.getStatusCode())
            .header("location", Matchers.endsWith("/projects"))
            .cookie("remsfal_access_token", Matchers.notNullValue())
            .cookie("remsfal_refresh_token", Matchers.notNullValue());

        final String tokenId = entityManager
            .createQuery("SELECT user.tokenId FROM UserEntity user where user.email = :email", String.class)
            .setParameter("email", email.toLowerCase())
            .getSingleResult();
        assertEquals(DevDataSeedController.tokenIdOf(email), tokenId);
    }

    @Test
    void login_SUCCESS_existingUserIsReused() {
        final String email = DevDataSeedController.TENANT.email();
        for (int i = 0; i < 2; i++) {
            given()
                .formParam("email", email)
                .redirects().follow(false)
                .when().post(DEV_LOGIN_PATH)
                .then()
                .statusCode(Status.FOUND.getStatusCode());
        }

        final Long count = entityManager
            .createQuery("SELECT count(user) FROM UserEntity user where user.email = :email", Long.class)
            .setParameter("email", email)
            .getSingleResult();
        assertEquals(1L, count);
    }

    @Test
    void login_SUCCESS_foreignRouteIsIgnored() {
        given()
            .formParam("email", "someone@remsfal.dev")
            .queryParam("route", "//evil.example.org/path")
            .redirects().follow(false)
            .when().post(DEV_LOGIN_PATH)
            .then()
            .statusCode(Status.FOUND.getStatusCode())
            .header("location", Matchers.not(Matchers.containsString("evil.example.org")));
    }

    @Test
    void login_FAILED_regexDenialOfServiceInputIsRejectedFast() {
        final String malicious = "!@!." + "!.".repeat(20_000) + "@";
        final long start = System.nanoTime();

        given()
            .formParam("email", malicious)
            .redirects().follow(false)
            .when().post(DEV_LOGIN_PATH)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());

        assertTrue(Duration.ofNanos(System.nanoTime() - start).toSeconds() < 5);
    }

    @Test
    void login_FAILED_blankEmail() {
        given()
            .formParam("email", " ")
            .redirects().follow(false)
            .when().post(DEV_LOGIN_PATH)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void login_FAILED_invalidEmail() {
        given()
            .formParam("email", "not-an-email")
            .redirects().follow(false)
            .when().post(DEV_LOGIN_PATH)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

}
