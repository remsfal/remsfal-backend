package de.remsfal.service.boundary;

import de.remsfal.service.boundary.authentication.SessionManager;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.containsString;

import java.time.Duration;
import java.util.Date;

import jakarta.ws.rs.core.Response.Status;

@QuarkusTest
class UserResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/user";

    @Test
    void getUser_FAILED_noAuthentication() {
        given()
                .when().get(BASE_PATH)
                .then()
                .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getUser_FAILED_invalidCookie() {
        final Cookie accessToken = new Cookie.Builder(SessionManager.ACCESS_COOKIE_NAME, "invalid.jwt.token")
                .setMaxAge(600)
                .build();

        given()
                .when()
                .cookie(accessToken)
                .get(BASE_PATH)
                .then()
                .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getUser_FAILED_expiredAccessToken() {
        final Cookie accessToken = buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(-10)); // Abgelaufen

        given()
                .when()
                .cookie(accessToken)
                .get(BASE_PATH)
                .then()
                .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getUser_FAILED_noUserId() {
        final Cookie accessToken = buildAccessTokenCookie(null, TestData.USER_EMAIL, Duration.ofMinutes(10));

        given()
                .when()
                .cookie(accessToken)
                .get(BASE_PATH)
                .then()
                .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getUser_FAILED_noUserEmail() {
        final Cookie accessToken = buildAccessTokenCookie(TestData.USER_ID, null, Duration.ofMinutes(10));

        given()
                .when()
                .cookie(accessToken)
                .get(BASE_PATH)
                .then()
                .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getUser_FAILED_userNotExists() {
        final Cookie accessToken = buildAccessTokenCookie(java.util.UUID.randomUUID(), TestData.USER_EMAIL, Duration.ofMinutes(10));

        given()
                .when()
                .cookie(accessToken)
                .get(BASE_PATH)
                .then()
                .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getUser_SUCCESS_userIsReturned() {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO USER (ID, EMAIL, AUTHENTICATED_AT, FIRST_NAME, LAST_NAME) VALUES (?,?,?,?,?)")
                .setParameter(1, TestData.USER_ID)
                .setParameter(2, TestData.USER_EMAIL)
                .setParameter(3, new Date())
                .setParameter(4, TestData.USER_FIRST_NAME)
                .setParameter(5, TestData.USER_LAST_NAME)
                .executeUpdate());

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofDays(7)))
                .get(BASE_PATH)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("id", Matchers.equalTo(TestData.USER_ID.toString()))
                .and().body("firstName", Matchers.equalTo(TestData.USER_FIRST_NAME))
                .and().body("lastName", Matchers.equalTo(TestData.USER_LAST_NAME))
                .and().body("email", Matchers.equalTo(TestData.USER_EMAIL));
    }

    @Test
    void updateUser_SUCCESS_userInfoChanged() {
        setupTestUsers();

        final String update = "{ \"firstName\":\"john\"," +
                "\"mobilePhoneNumber\":\"+491773289245\"," +
                "\"businessPhoneNumber\":\"+49302278349\"," +
                "\"privatePhoneNumber\":\"+4933012345611\"}";

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofDays(7)))
                .contentType(ContentType.JSON)
                .body(update)
                .patch(BASE_PATH)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("firstName", Matchers.equalTo("john"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"mobilePhoneNumber\":\"491773289245\"}",
            "{\"businessPhoneNumber\":\"+49 177 3289245\"}",
            "{\"privatePhoneNumber\":\"+4930\"}"
    })
    void updateUser_FAILED_invalidPhoneNumber(String invalidPatch) {
        setupTestUsers();

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofDays(7)))
                .contentType(ContentType.JSON)
                .body(invalidPatch)
                .patch(BASE_PATH)
                .then()
                .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void deleteUser_SUCCESS_userDeleted() {
        setupTestUsers();

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofDays(7)))
                .delete(BASE_PATH)
                .then()
                .statusCode(Status.NO_CONTENT.getStatusCode());

        long entries = entityManager
                .createQuery("SELECT count(user) FROM UserEntity user where user.email = :email", Long.class)
                .setParameter("email", TestData.USER_EMAIL)
                .getSingleResult();

        assertEquals(0, entries);
    }

    @Test
    void metricGenerated_afterGetUser() {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO USER (ID, EMAIL, AUTHENTICATED_AT, FIRST_NAME, LAST_NAME) VALUES (?,?,?,?,?)")
                .setParameter(1, TestData.USER_ID)
                .setParameter(2, TestData.USER_EMAIL)
                .setParameter(3, new Date())
                .setParameter(4, TestData.USER_FIRST_NAME)
                .setParameter(5, TestData.USER_LAST_NAME)
                .executeUpdate());
        given()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofDays(7)))
                .when().get(BASE_PATH)
                .then().statusCode(Status.OK.getStatusCode());
        given()
                .when().get("/q/metrics")
                .then()
                .statusCode(200)
                .body(containsString("GetUserTimer"));
    }
    @Test
    void metricGenerated_afterUpdateUser() {
        setupTestUsers();
        String update = "{ \"firstName\":\"john\"," +
                "\"mobilePhoneNumber\":\"+491773289245\"," +
                "\"businessPhoneNumber\":\"+49302278349\"," +
                "\"privatePhoneNumber\":\"+4933012345611\"}";
        given()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofDays(7)))
                .contentType(ContentType.JSON)
                .body(update)
                .when().patch(BASE_PATH)
                .then().statusCode(Status.OK.getStatusCode());

        given()
                .when().get("/q/metrics")
                .then()
                .statusCode(200)
                .body(containsString("UpdateUserTimer"));
    }
    @Test
    void metricGenerated_afterDeleteUser() {
        setupTestUsers();
        given()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofDays(7)))
                .when().delete(BASE_PATH)
                .then()
                .statusCode(Status.NO_CONTENT.getStatusCode());
        given()
                .when().get("/q/metrics")
                .then()
                .statusCode(200)
                .body(containsString("DeleteUserTimer"));
    }
}