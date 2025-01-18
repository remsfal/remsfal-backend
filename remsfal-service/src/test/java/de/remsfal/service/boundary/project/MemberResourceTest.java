package de.remsfal.service.boundary.project;

import de.remsfal.service.TestData;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class MemberResourceTest extends AbstractProjectResourceTest {

    static final String BASE_PATH = "/api/v1/projects/{projectId}/members";
/*
    @Override
    @BeforeEach
    protected void setupTestProperties() {
        super.setupTestUsers();
        super.setupTestProjects();
        super.setupTestProperties();
    }

    @Test
    void addProjectMemeber_SUCCESS_multiUser() {
        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"projectId\":\" " + TestData.PROJECT_ID_1 + "\",  \"id\":\"" + TestData.USER_ID_2 + "\", \"role\":\"LESSOR\", \"email\":\"" + TestData.USER_EMAIL_2 + "\"}")
                .post(BASE_PATH,  TestData.PROJECT_ID)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");


        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100)))
                .get(BASE_PATH,  TestData.PROJECT_ID)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("members.id", Matchers.hasItem(TestData.USER_ID_1));

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).contentType(MediaType.APPLICATION_JSON)
                .body("{ \"projectId\":\" " + TestData.PROJECT_ID_1 + "\",  \"id\":\"" + TestData.USER_ID_2 + "\", \"role\":\"LESSOR\", \"email\":\"" + TestData.USER_EMAIL_2 + "\"}")
                .post(BASE_PATH,  TestData.PROJECT_ID)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).get(BASE_PATH + "/" + user1projectId2 + "/members")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("members.id", Matchers.hasItem(TestData.USER_ID_1));

        final String user1projectId3 = given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_3 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).contentType(MediaType.APPLICATION_JSON)
                .body("{ \"projectId\":\" " + TestData.PROJECT_ID_1 + "\",  \"id\":\"" + TestData.USER_ID_2 + "\", \"role\":\"LESSOR\", \"email\":\"" + TestData.USER_EMAIL_2 + "\"}")
                .post(BASE_PATH + "/" + user1projectId3 + "/members")
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).get(BASE_PATH + "/" + user1projectId3 + "/members")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("members.id", Matchers.hasItem(TestData.USER_ID_1));

        final String user1projectId4 = given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_4 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).contentType(MediaType.APPLICATION_JSON)
                .body("{ \"projectId\":\" " + TestData.PROJECT_ID_1 + "\",  \"id\":\"" + TestData.USER_ID_2 + "\", \"role\":\"LESSOR\", \"email\":\"" + TestData.USER_EMAIL_2 + "\"}")
                .post(BASE_PATH + "/" + user1projectId4 + "/members")
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).get(BASE_PATH + "/" + user1projectId4 + "/members")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("members.id", Matchers.hasItem(TestData.USER_ID_1));

        final String user1projectId5 = given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_5 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).contentType(MediaType.APPLICATION_JSON)
                .body("{ \"projectId\":\" " + TestData.PROJECT_ID_1 + "\",  \"id\":\"" + TestData.USER_ID_2 + "\", \"role\":\"LESSOR\", \"email\":\"" + TestData.USER_EMAIL_2 + "\"}")
                .post(BASE_PATH + "/" + user1projectId5 + "/members")
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).get(BASE_PATH + "/" + user1projectId5 + "/members")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("members.id", Matchers.hasItem(TestData.USER_ID_1));

        long entries1 = entityManager
                .createQuery("SELECT count(m) FROM ProjectEntity p JOIN p.memberships m WHERE p.title = :projectTitle", Long.class)
                .setParameter("projectTitle", TestData.PROJECT_TITLE_1)
                .getSingleResult();
        assertEquals(2, entries1);

        long entries2 = entityManager
                .createQuery("SELECT count(m) FROM ProjectEntity p JOIN p.memberships m WHERE p.title = :projectTitle", Long.class)
                .setParameter("projectTitle", TestData.PROJECT_TITLE_2)
                .getSingleResult();
        assertEquals(2, entries2);

        long entries3 = entityManager
                .createQuery("SELECT count(m) FROM ProjectEntity p JOIN p.memberships m WHERE p.title = :projectTitle", Long.class)
                .setParameter("projectTitle", TestData.PROJECT_TITLE_4)
                .getSingleResult();
        assertEquals(2, entries3);

        long entries4 = entityManager
                .createQuery("SELECT count(m) FROM ProjectEntity p JOIN p.memberships m WHERE p.title = :projectTitle", Long.class)
                .setParameter("projectTitle", TestData.PROJECT_TITLE_5)
                .getSingleResult();
        assertEquals(2, entries4);

        long entries5 = entityManager
                .createQuery("SELECT count(m) FROM ProjectEntity p JOIN p.memberships m WHERE p.title = :projectTitle", Long.class)
                .setParameter("projectTitle", TestData.PROJECT_TITLE_5)
                .getSingleResult();
        assertEquals(2, entries5);
    }

    @Test
    void addProjectMemeber_noAuthentication() {
        final String user1projectId1 = given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).get(BASE_PATH + "/" + user1projectId1 + "/members")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON);

        given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"projectId\":\" " + TestData.PROJECT_ID_1 + "\",  \"id\":\"" + TestData.USER_ID_2 + "\", \"role\":\"LESSOR\", \"email\":\"" + TestData.USER_EMAIL_2 + "\"}")
                .post(BASE_PATH + "/" + user1projectId1 + "/members")
                .then()
                .statusCode(Matchers.equalTo(401));
    }

    @Test
    void getProjectMembers_SUCCESS() {
        final String user1projectId1 = given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).get(BASE_PATH + "/" + user1projectId1 + "/members")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("members.id", Matchers.hasItem(TestData.USER_ID_1));
    }

    @Test
    void updateProjectMember_SUCCESS() {
        final String user1projectId1 = given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).contentType(MediaType.APPLICATION_JSON)
                .body("{ \"projectId\":\" " + user1projectId1 + "\",  \"id\":\"" + TestData.USER_ID_1 + "\", \"role\":\"LESSOR\", \"email\":\"max.mustermann@example.org\", \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .patch(BASE_PATH + "/" + user1projectId1 + "/members/" + TestData.USER_ID_1)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("id", Matchers.equalTo(user1projectId1))
                .and().body("title", Matchers.equalTo(TestData.PROJECT_TITLE_1))
                .and().body("members[0].role", Matchers.equalTo("LESSOR"));
    }

    @Test
    void updateProjectMember_noRole() {
        final String user1projectId1 = given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).contentType(MediaType.APPLICATION_JSON)
                .body("{ \"projectId\":\" " + user1projectId1 + "\",  \"id\":\"" + TestData.USER_ID_1 + "\", \"email\":\"max.mustermann@example.org\", \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .patch(BASE_PATH + "/" + user1projectId1 + "/members/" + TestData.USER_ID_1)
                .then()
                .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void updateProjectMember_noAuthentication() {
        final String user1projectId1 = given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).get(BASE_PATH + "/" + user1projectId1 + "/members")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON);

        given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"projectId\":\" " + TestData.PROJECT_ID_2 + "\",  \"id\":\"" + TestData.USER_ID_2 + "\", \"role\":\"LESSOR\", \"email\":\"" + TestData.USER_EMAIL_2 + "\"}")
                .post(BASE_PATH + "/" + user1projectId1 + "/members")
                .then()
                .statusCode(Matchers.equalTo(401));
    }

    @Test
    void deleteProjectMember_SUCCESS() {
        final String user1projectId1 = given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).get(BASE_PATH + "/" + user1projectId1 + "/members")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON);

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).contentType(MediaType.APPLICATION_JSON)
                .delete(BASE_PATH + "/" + user1projectId1 + "/members/" + TestData.USER_ID_2)
                .then()
                .statusCode(Matchers.equalTo(204));
    }

    @Test
    void deleteProjectMember_noFound() {
        final String user1projectId1 = given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).get(BASE_PATH + "/" + user1projectId1 + "/members")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON);

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).contentType(MediaType.APPLICATION_JSON)
                .delete(BASE_PATH + "/" + user1projectId1 + "/members/" + TestData.USER_ID_1)
                .then()
                .statusCode(Matchers.equalTo(404));
    }

    @Test
    void deleteProjectMember_noAuthentication() {
        final String user1projectId1 = given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100))).get(BASE_PATH + "/" + user1projectId1 + "/members")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON);

        given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .delete(BASE_PATH + "/" + user1projectId1 + "/members/" + TestData.USER_ID_1)
                .then()
                .statusCode(Matchers.equalTo(401));
    }
*/
}