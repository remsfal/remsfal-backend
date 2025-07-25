package de.remsfal.service.boundary.project;

import de.remsfal.service.boundary.AbstractResourceTest;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static io.restassured.RestAssured.given;

@QuarkusTest
class MemberResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/projects/{projectId}/members";
    static final String MEMBER_PATH = BASE_PATH + "/{memberId}";

    @Override
    @BeforeEach
    protected void setupTestProperties() {
        super.setupTestUsers();
        super.setupTestProjects();
        super.setupTestProperties();
    }

    @Test
    void getProjectMembers_FAILED_notMember() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .get(BASE_PATH, TestData.PROJECT_ID)
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void addProjectMember_FAILED_notMember() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"email\":\"" + TestData.USER_EMAIL_2 + "\",  \"role\":\"LESSOR\"}")
            .post(BASE_PATH, TestData.PROJECT_ID)
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void updateProjectMember_FAILED_notMember() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"role\":\"LESSOR\"}")
            .patch(MEMBER_PATH, TestData.PROJECT_ID, TestData.USER_ID_1)
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void deleteProjectMember_FAILED_notMember() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .delete(MEMBER_PATH, TestData.PROJECT_ID, TestData.USER_ID_1)
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void deleteProjectMember_FAILED_notOwner() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .delete(MEMBER_PATH, TestData.PROJECT_ID, TestData.USER_ID_1)
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void updateProjectMember_FAILED_emailMustBeNull() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"email\":\"" + TestData.USER_EMAIL_2 + "\",  \"role\":\"LESSOR\"}")
            .patch(MEMBER_PATH, TestData.PROJECT_ID, TestData.USER_ID_1)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void getProjectMembers_SUCCESS_oneMemberReturned() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .get(BASE_PATH, TestData.PROJECT_ID)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("members.size()", Matchers.equalTo(1))
            .and().body("members.id", Matchers.hasItem(TestData.USER_ID_1))
            .and().body("members.email", Matchers.hasItem(TestData.USER_EMAIL_1))
            .and().body("members.active", Matchers.hasItem(true))
            .and().body("members.role", Matchers.hasItem("MANAGER"));
    }

    @Test
    void addProjectMember_SUCCESS_newMemberReturned() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"email\":\"newUser@example.org\",  \"role\":\"STAFF\"}")
            .post(BASE_PATH, TestData.PROJECT_ID)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.notNullValue())
            .and().body("email", Matchers.equalTo("newUser@example.org"))
            .and().body("active", Matchers.is(false))
            .and().body("role", Matchers.equalTo("STAFF"));
    }

    @Test
    void addProjectMember_SUCCESS_existingMemberReturned() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"email\":\"" + TestData.USER_EMAIL_2 + "\",  \"role\":\"LESSOR\"}")
            .post(BASE_PATH, TestData.PROJECT_ID)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.equalTo(TestData.USER_ID_2))
            .and().body("email", Matchers.equalTo(TestData.USER_EMAIL_2))
            .and().body("active", Matchers.is(true))
            .and().body("role", Matchers.equalTo("LESSOR"));
    }

    @Test
    void updateProjectMember_SUCCESS_memberWithChangedRoleReturned() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"role\":\"PROPRIETOR\"}")
            .patch(MEMBER_PATH, TestData.PROJECT_ID, TestData.USER_ID_1)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.equalTo(TestData.USER_ID_1))
            .and().body("email", Matchers.equalTo(TestData.USER_EMAIL_1))
            .and().body("active", Matchers.is(true))
            .and().body("role", Matchers.equalTo("PROPRIETOR"));
    }

    @Test
    void deleteProjectMember_SUCCESS_userDeleted() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"email\":\"" + TestData.USER_EMAIL_2 + "\",  \"role\":\"PROPRIETOR\"}")
            .post(BASE_PATH, TestData.PROJECT_ID)
            .then()
            .statusCode(Status.OK.getStatusCode());

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .delete(MEMBER_PATH, TestData.PROJECT_ID, TestData.USER_ID_1)
            .then()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void getUser_SUCCESS_userHasCorrectRole() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .get("/api/v1/user")
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
                .and().body("id", Matchers.equalTo(TestData.USER_ID))
                .and().body("email", Matchers.equalTo(TestData.USER_EMAIL))
                .and().body("userRoles.size()", Matchers.equalTo(1))
                .and().body("userRoles[0]", Matchers.equalTo("MANAGER"));
    }

}