package de.remsfal.service.boundary;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

import de.remsfal.service.TestData;
import de.remsfal.service.boundary.authentication.SessionInfo;
import de.remsfal.service.boundary.authentication.SessionManager;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response.Status;

@QuarkusTest
class UserResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/user";
    
//    @Test
//    void getUser_FAILED_userNotExist() {
//        when(tokenValidator.validate("Bearer " + TestData.USER_TOKEN))
//            .thenReturn(new SessionInfo(new Payload()
//                .setSubject(TestData.USER_TOKEN)
//                .setEmail(TestData.USER_EMAIL)
//                .set("name", TestData.USER_NAME)));
//        
//        given()
//            .header("Authorization", "Bearer " + TestData.USER_TOKEN)
//            .when().get(BASE_PATH)
//            .then()
//            .statusCode(Status.NOT_FOUND.getStatusCode());
//    }
//
//    @Test
//    void getUser_FAILED_invalidSignature() {
//        when(tokenValidator.validate("Bearer " + TestData.USER_TOKEN))
//            .thenReturn(null);
//        
//        given()
//            .header("Authorization", "Bearer " + TestData.USER_TOKEN)
//            .when().get(BASE_PATH)
//            .then()
//            .statusCode(Status.UNAUTHORIZED.getStatusCode());
//    }

    @Test
    void getUser_FAILED_noAuthentication() {
        given()
            .when().get(BASE_PATH)
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
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

//    @Test
//    void registerUser_SUCCESS_userDeleted() {
//        when(tokenValidator.validate("Bearer " + TestData.USER_TOKEN_4))
//            .thenReturn(new SessionInfo(new Payload()
//            .setSubject(TestData.USER_TOKEN_4)
//            .setEmail(TestData.USER_EMAIL_4)
//            .set("name", TestData.USER_NAME_4)));
//
//        long enties = entityManager
//            .createQuery("SELECT count(user) FROM UserEntity user where user.email = :email", Long.class)
//            .setParameter("email", TestData.USER_EMAIL_4)
//            .getSingleResult();
//        assertEquals(0, enties);
//
//        given()
//            .when()
//            .header("Authorization", "Bearer " + TestData.USER_TOKEN_4)
//            .post(BASE_PATH)
//            .then()
//            .statusCode(Status.OK.getStatusCode())
//            .contentType(ContentType.JSON);
//
//        enties = entityManager
//            .createQuery("SELECT count(user) FROM UserEntity user where user.email = :email", Long.class)
//            .setParameter("email", TestData.USER_EMAIL_4)
//            .getSingleResult();
//        assertEquals(1, enties);
//
//        given()
//            .when()
//            .header("Authorization", "Bearer " + TestData.USER_TOKEN_4)
//            .delete(BASE_PATH)
//            .then()
//            .statusCode(Status.NO_CONTENT.getStatusCode());
//
//        enties = entityManager
//            .createQuery("SELECT count(user) FROM UserEntity user where user.email = :email", Long.class)
//            .setParameter("email", TestData.USER_EMAIL_4)
//            .getSingleResult();
//        assertEquals(0, enties);
//    }
//    
//    @Test
//    void registerUser_SUCCESS_updateUser() {
//        when(tokenValidator.validate("Bearer " + TestData.USER_TOKEN_4))
//            .thenReturn(new SessionInfo(new Payload()
//                .setSubject(TestData.USER_TOKEN_4)
//                .setEmail(TestData.USER_EMAIL_4)
//                .set("name", TestData.USER_NAME_4)));
//
//        given()
//            .when()
//            .header("Authorization", "Bearer " + TestData.USER_TOKEN_4)
//            .post(BASE_PATH)
//            .then()
//            .statusCode(Status.OK.getStatusCode())
//            .contentType(ContentType.JSON)
//            .and().body("name", Matchers.equalTo(TestData.USER_NAME_4))
//            .and().body("email", Matchers.equalTo(TestData.USER_EMAIL_4));
//
//        final String update = "{ \"name\":\"" + "john" + "\"}";
//
//        given()
//            .when()
//            .header("Authorization", "Bearer " + TestData.USER_TOKEN_4)
//            .contentType(ContentType.JSON)
//            .body(update)
//            .patch(BASE_PATH)
//            .then()
//            .statusCode(Status.OK.getStatusCode())
//            .contentType(ContentType.JSON)
//            .and().body("name", Matchers.equalTo("john"));
//
//        given()
//            .when()
//            .header("Authorization", "Bearer " + TestData.USER_TOKEN_4)
//            .get(BASE_PATH)
//            .then()
//            .statusCode(Status.OK.getStatusCode())
//            .contentType(ContentType.JSON)
//            .and().body("name", Matchers.equalTo("john"))
//            .and().body("email", Matchers.equalTo(TestData.USER_EMAIL_4));
//    }

}