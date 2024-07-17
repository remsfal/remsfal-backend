package de.remsfal.service.boundary;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.remsfal.service.TestData;
import de.remsfal.service.boundary.authentication.SessionInfo;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        final String value = "any.invalid.jwe.as.cookie";
        final Cookie cookie = new Cookie.Builder("remsfal_session", value)
            .setMaxAge(600)
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
            .setMaxAge(600)
            .build();

        given()
            .when()
            .cookie(cookie)
            .get(BASE_PATH)
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getUser_FAILED_noExpirationTime() {
        final SessionInfo sessionInfo = SessionInfo.builder()
            .userId(TestData.USER_ID)
            .userEmail(TestData.USER_EMAIL)
            .build();
        final String value = sessionManager.encryptSessionObject(sessionInfo);
        final Cookie cookie = new Cookie.Builder("remsfal_session", value)
            .setMaxAge(600)
            .build();

        given()
            .when()
            .cookie(cookie)
            .get(BASE_PATH)
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getUser_FAILED_noUserId() {
        final SessionInfo sessionInfo = SessionInfo.builder()
            .expireAfter(Duration.ofMinutes(10))
            .userEmail(TestData.USER_EMAIL)
            .build();
        final String value = sessionManager.encryptSessionObject(sessionInfo);
        final Cookie cookie = new Cookie.Builder("remsfal_session", value)
            .setMaxAge(600)
            .build();

        given()
            .when()
            .cookie(cookie)
            .get(BASE_PATH)
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getUser_FAILED_noUserEmail() {
        final SessionInfo sessionInfo = SessionInfo.builder()
            .expireAfter(Duration.ofMinutes(10))
            .userId(TestData.USER_ID)
            .build();
        final String value = sessionManager.encryptSessionObject(sessionInfo);
        final Cookie cookie = new Cookie.Builder("remsfal_session", value)
            .setMaxAge(600)
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
            .createNativeQuery("INSERT INTO USER (ID, EMAIL, AUTHENTICATED_AT, FIRST_NAME, LAST_NAME) VALUES (?,?,?,?,?)")
            .setParameter(1, TestData.USER_ID)
            .setParameter(2, TestData.USER_EMAIL)
            .setParameter(3, new Date())
            .setParameter(4, TestData.USER_FIRST_NAME)
            .setParameter(5, TestData.USER_LAST_NAME)
            .executeUpdate());

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.equalTo(TestData.USER_ID))
            .and().body("firstName", Matchers.equalTo(TestData.USER_FIRST_NAME))
            .and().body("lastName", Matchers.equalTo(TestData.USER_LAST_NAME))
            .and().body("email", Matchers.equalTo(TestData.USER_EMAIL))
            .and().body("registeredDate", Matchers.is(Matchers.notNullValue()))
            .and().body("lastLoginDate", Matchers.is(Matchers.notNullValue()));
    }

    @Test
    void updateUser_SUCCESS_userInfoChanged() {
        setupTestUsers();

        final String update = "{ \"firstName\":\"john\","
            + "\"mobilePhoneNumber\":\"+491773289245\","
            + "\"businessPhoneNumber\":\"+49302278349\","
            + "\"privatePhoneNumber\":\"+4933012345611\"}";
        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body(update)
            .patch(BASE_PATH)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("firstName", Matchers.equalTo("john"))
            .and().body("lastName", Matchers.equalTo(TestData.USER_LAST_NAME))
            .and().body("email", Matchers.equalTo(TestData.USER_EMAIL))
            .and().body("mobilePhoneNumber", Matchers.equalTo("+491773289245"))
            .and().body("businessPhoneNumber", Matchers.equalTo("+49302278349"))
            .and().body("privatePhoneNumber", Matchers.equalTo("+4933012345611"));

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("firstName", Matchers.equalTo("john"))
            .and().body("lastName", Matchers.equalTo(TestData.USER_LAST_NAME))
            .and().body("email", Matchers.equalTo(TestData.USER_EMAIL))
            .and().body("mobilePhoneNumber", Matchers.equalTo("+491773289245"))
            .and().body("businessPhoneNumber", Matchers.equalTo("+49302278349"))
            .and().body("privatePhoneNumber", Matchers.equalTo("+4933012345611"));
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = {
        "{\"mobilePhoneNumber\":\"491773289245\"}",
        "{\"businessPhoneNumber\":\"+49 177 3289245\"}",
        "{\"privatePhoneNumber\":\"+4930\"}"
        })
    void updateUser_FAILED_invalidPhoneNumber(String invalidPatch) {
        setupTestUsers();

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body(invalidPatch)
            .patch(BASE_PATH)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void updateUser_SUCCESS_addressChanged() {
        setupTestUsers();

        final String update = "{ \"address\":{"
            + "\"street\":\"" + TestData.ADDRESS_STREET + "\","
            + "\"city\":\"" + TestData.ADDRESS_CITY + "\","
            + "\"province\":\"" + TestData.ADDRESS_PROVINCE + "\","
            + "\"zip\":\"" + TestData.ADDRESS_ZIP + "\","
            + "\"countryCode\":\"" + TestData.ADDRESS_COUNTRY + "\"}}";
        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body(update)
            .patch(BASE_PATH)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("firstName", Matchers.equalTo(TestData.USER_FIRST_NAME))
            .and().body("lastName", Matchers.equalTo(TestData.USER_LAST_NAME))
            .and().body("email", Matchers.equalTo(TestData.USER_EMAIL))
            .and().body("address.street", Matchers.equalTo(TestData.ADDRESS_STREET))
            .and().body("address.city", Matchers.equalTo(TestData.ADDRESS_CITY))
            .and().body("address.province", Matchers.equalTo(TestData.ADDRESS_PROVINCE))
            .and().body("address.zip", Matchers.equalTo(TestData.ADDRESS_ZIP))
            .and().body("address.countryCode", Matchers.equalTo(TestData.ADDRESS_COUNTRY));

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("address.street", Matchers.equalTo(TestData.ADDRESS_STREET))
            .and().body("address.city", Matchers.equalTo(TestData.ADDRESS_CITY))
            .and().body("address.province", Matchers.equalTo(TestData.ADDRESS_PROVINCE))
            .and().body("address.zip", Matchers.equalTo(TestData.ADDRESS_ZIP))
            .and().body("address.countryCode", Matchers.equalTo(TestData.ADDRESS_COUNTRY));
    }

    @Test
    void updateUser_FAILED_invalidZipCityCombination() {
        setupTestUsers();

        final String update = "{ \"address\":{"
            + "\"street\":\"" + TestData.ADDRESS_STREET + "\","
            + "\"city\":\"" + TestData.ADDRESS_CITY + "\","
            + "\"province\":\"" + TestData.ADDRESS_PROVINCE + "\","
            + "\"zip\":\"20359\","
            + "\"countryCode\":\"" + TestData.ADDRESS_COUNTRY + "\"}}";
        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body(update)
            .patch(BASE_PATH)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
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