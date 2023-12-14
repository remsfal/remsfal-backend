package de.remsfal.service.boundary;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import de.remsfal.service.TestData;
import de.remsfal.service.boundary.authentication.SessionInfo;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

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
        final String value = "any.invalid.jwe.as.cookie";
        final Cookie cookie = new Cookie.Builder("remsfal_session", value)
            .setMaxAge(60)
            .build();

        given()
            .when()
            .cookie(cookie)
            .get(BASE_PATH)
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getUser_FAILED_expiredCookie() {
        final SessionInfo sessionInfo = SessionInfo.builder()
            .expireAfter(Duration.ofMinutes(-10))
            .userId(TestData.USER_ID)
            .userEmail(TestData.USER_EMAIL)
            .build();
        final String value = sessionManager.encryptSessionObject(sessionInfo);
        final Cookie cookie = new Cookie.Builder("remsfal_session", value)
            .setMaxAge(60)
            .build();

        given()
            .when()
            .cookie(cookie)
            .get(BASE_PATH)
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getUser_FAILED_userNotExists() {
        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH)
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getUser_SUCCESS_userIsReturned() {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO USER (ID, NAME, EMAIL) VALUES (?,?,?)")
            .setParameter(1, TestData.USER_ID)
            .setParameter(2, TestData.USER_NAME)
            .setParameter(3, TestData.USER_EMAIL)
            .executeUpdate());

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.equalTo(TestData.USER_ID))
            .and().body("name", Matchers.equalTo(TestData.USER_NAME))
            .and().body("email", Matchers.equalTo(TestData.USER_EMAIL))
            .and().body("registeredDate", Matchers.is(Matchers.notNullValue()))
            .and().body("lastLoginDate", Matchers.is(Matchers.notNullValue()));
    }

    @Test
    void updateUser_SUCCESS_userNameChanged() {
        setupTestUsers();

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("name", Matchers.equalTo(TestData.USER_NAME))
            .and().body("email", Matchers.equalTo(TestData.USER_EMAIL));

        final String update = "{ \"name\":\"" + "john" + "\"}";

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body(update)
            .patch(BASE_PATH)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("name", Matchers.equalTo("john"));

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("name", Matchers.equalTo("john"))
            .and().body("email", Matchers.equalTo(TestData.USER_EMAIL));
    }

    @Test
    void deleteUser_SUCCESS_userDeleted() {
        setupTestUsers();

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("name", Matchers.equalTo(TestData.USER_NAME))
            .and().body("email", Matchers.equalTo(TestData.USER_EMAIL));

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .delete(BASE_PATH)
            .then()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        long enties = entityManager
            .createQuery("SELECT count(user) FROM UserEntity user where user.email = :email", Long.class)
            .setParameter("email", TestData.USER_EMAIL)
            .getSingleResult();
        assertEquals(0, enties);
    }

}