package de.remsfal.service.boundary;

import de.remsfal.service.TestData;
import de.remsfal.core.json.ImmutableProjectMemberJson;
import de.remsfal.core.model.ProjectMemberModel;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class ProjectResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/projects";

    @BeforeEach
    protected void setupTestUsers() {
        super.setupTestUsers();
    }

    @Test
    void getProjects_FAILED_noAuthentication() {
        given()
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void createProject_SUCCESS_projectIsCreated() {
        final String json = "{ \"title\":\"" + TestData.PROJECT_TITLE + "\"}";
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(ContentType.JSON)
                .body(json)
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .contentType(ContentType.JSON)
                .header("location", Matchers.containsString(BASE_PATH + "/"))
                .and().body("id", Matchers.notNullValue())
                .and().body("title", Matchers.equalTo(TestData.PROJECT_TITLE))
                .and().body("members.id", Matchers.hasItem(TestData.USER_ID))
                .and().body("members.role", Matchers.hasItem("MANAGER"));

        long enties = entityManager
                .createQuery("SELECT count(project) FROM ProjectEntity project where project.title = :title", Long.class)
                .setParameter("title", TestData.PROJECT_TITLE)
                .getSingleResult();
        assertEquals(1, enties);
    }

    @Test
    void createProject_FAILED_idIsProvided() {
        final String json = "{ \"title\":\"" + TestData.PROJECT_TITLE + "\","
                + "\"id\":\"anyId\"}";
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(ContentType.JSON)
                .body(json)
                .post(BASE_PATH)
                .then()
                .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void createProject_FAILED_noAuthentication() {
        given()
                .when()
                .contentType(ContentType.JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void createProject_FAILED_noTitle() {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(ContentType.JSON)
                .body("{ \"title\":\" \"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void getProject_SUCCESS_sameProjectIsReturned() {
        final String json = "{ \"title\":\"" + TestData.PROJECT_TITLE + "\"}";

        final Response res = given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .post(BASE_PATH)
                .thenReturn();

        final String projectId = res.then()
                .contentType(MediaType.APPLICATION_JSON)
                .extract().path("id");

        final String projectUrl = res.then()
                .statusCode(Status.CREATED.getStatusCode())
                .header("location", Matchers.startsWith("http://localhost:8081/api/v1/projects"))
                .header("location", Matchers.endsWith(projectId))
                .extract().header("location");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .get(projectUrl)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("id", Matchers.equalTo(projectId))
                .and().body("title", Matchers.equalTo(TestData.PROJECT_TITLE));
    }

    @Test
    void getProject_FAILED_noAuthentication() {
        given()
                .when()
                .get(BASE_PATH + "/anyId")
                .then()
                .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getProjects_FAILED_requestedNumberToHigh() {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .queryParam("limit", 120)
                .get(BASE_PATH)
                .then()
                .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void getProjects_FAILED_negativeOffset() {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .queryParam("offset", -3)
                .get(BASE_PATH)
                .then()
                .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void getProjects_FAILED_negativeLimit() {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .queryParam("limit", -12)
                .get(BASE_PATH)
                .then()
                .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void getProjects_SUCCESS_pagination() {
        for (int i = 0; i < 20; i++) {
            given()
                    .when()
                    .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{ \"title\":\"" + TestData.PROJECT_TITLE + i + "\"}")
                    .post(BASE_PATH)
                    .then()
                    .statusCode(Status.CREATED.getStatusCode());
        }

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .get(BASE_PATH)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("first", Matchers.is(0))
                .and().body("size", Matchers.is(10))
                .and().body("total", Matchers.is(20))
                .and().body("projects.size()", Matchers.is(10));

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .queryParam("offset", 6)
                .queryParam("limit", 12)
                .get(BASE_PATH)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("first", Matchers.is(6))
                .and().body("size", Matchers.is(12))
                .and().body("total", Matchers.is(20))
                .and().body("projects.size()", Matchers.is(12));
    }

    @Test
    void getProjects_SUCCESS_multiUser() {
        final String user1projectId1 = given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        final String user1projectId2 = given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_2 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        final String user2projectId3 = given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_3 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("first", Matchers.is(0))
                .and().body("size", Matchers.is(2))
                .and().body("total", Matchers.is(2))
                .and().body("projects.size()", Matchers.is(2))
                .and().body("projects.id", Matchers.hasItems(user1projectId1, user1projectId2))
                .and().body("projects.name", Matchers.hasItems(TestData.PROJECT_TITLE_1, TestData.PROJECT_TITLE_2))
                .and().body("projects.memberRole", Matchers.hasItems("MANAGER", "MANAGER"));

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
                .get(BASE_PATH)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("first", Matchers.is(0))
                .and().body("size", Matchers.is(1))
                .and().body("total", Matchers.is(1))
                .and().body("projects.size()", Matchers.is(1))
                .and().body("projects.id", Matchers.hasItems(user2projectId3))
                .and().body("projects.name", Matchers.hasItems(TestData.PROJECT_TITLE_3));
    }

    @Test
    void updateProject_SUCCESS_changedTitle() {
        final String projectId = given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_2 + "\"}")
                .patch(BASE_PATH + "/" + projectId)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("id", Matchers.equalTo(projectId))
                .and().body("title", Matchers.equalTo(TestData.PROJECT_TITLE_2));
    }

    @Test
    void deleteProject_SUCCESS_singleProject() {
        final String projectId = given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .delete(BASE_PATH + "/" + projectId)
                .then()
                .statusCode(Status.NO_CONTENT.getStatusCode());

        long enties = entityManager
                .createQuery("SELECT count(project) FROM ProjectEntity project where project.title = :title", Long.class)
                .setParameter("title", TestData.PROJECT_TITLE)
                .getSingleResult();
        assertEquals(0, enties);
    }

    @Test
    void addProjectMemeber_SUCCESS_multiUser() {
        final String user1projectId1 = given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"projectId\":\" " + TestData.PROJECT_ID_1 + "\",  \"id\":\"" + TestData.USER_ID_2 + "\", \"role\":\"LESSOR\", \"email\":\"" + TestData.USER_EMAIL_2 + "\"}")
                .post(BASE_PATH + "/" + user1projectId1 + "/members")
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/" + user1projectId1 + "/members")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("members.id", Matchers.hasItem(TestData.USER_ID_1));

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"projectId\":\" " + TestData.PROJECT_ID_1 + "\",  \"id\":\"" + TestData.USER_ID_2 + "\", \"role\":\"LESSOR\", \"email\":\"" + TestData.USER_EMAIL_2  + "\"}")
                .post(BASE_PATH + "/" + user1projectId1 + "/members")
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/" + user1projectId1 + "/members")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("members.id", Matchers.hasItem(TestData.USER_ID_1));

        final String user1projectId2 = given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_2 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"projectId\":\" " + TestData.PROJECT_ID_1 + "\",  \"id\":\"" + TestData.USER_ID_2 + "\", \"role\":\"LESSOR\", \"email\":\"" + TestData.USER_EMAIL_2 + "\"}")
                .post(BASE_PATH + "/" + user1projectId2 + "/members")
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/" + user1projectId2 + "/members")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("members.id", Matchers.hasItem(TestData.USER_ID_1));

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"projectId\":\" " + TestData.PROJECT_ID_1 + "\",  \"id\":\"" + TestData.USER_ID_2 + "\", \"role\":\"LESSOR\", \"email\":\"" + TestData.USER_EMAIL_2  + "\"}")
                .post(BASE_PATH + "/" + user1projectId2 + "/members")
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/" + user1projectId2 + "/members")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("members.id", Matchers.hasItem(TestData.USER_ID_1));

        final String user1projectId3 = given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_3 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"projectId\":\" " + TestData.PROJECT_ID_1 + "\",  \"id\":\"" + TestData.USER_ID_2 + "\", \"role\":\"LESSOR\", \"email\":\"" + TestData.USER_EMAIL_2 + "\"}")
                .post(BASE_PATH + "/" + user1projectId3 + "/members")
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/" + user1projectId3 + "/members")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("members.id", Matchers.hasItem(TestData.USER_ID_1));

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"projectId\":\" " + TestData.PROJECT_ID_1 + "\",  \"id\":\"" + TestData.USER_ID_2 + "\", \"role\":\"LESSOR\", \"email\":\"" + TestData.USER_EMAIL_2  + "\"}")
                .post(BASE_PATH + "/" + user1projectId3 + "/members")
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/" + user1projectId3 + "/members")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("members.id", Matchers.hasItem(TestData.USER_ID_1));

        final String user1projectId4 = given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_4 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"projectId\":\" " + TestData.PROJECT_ID_1 + "\",  \"id\":\"" + TestData.USER_ID_2 + "\", \"role\":\"LESSOR\", \"email\":\"" + TestData.USER_EMAIL_2 + "\"}")
                .post(BASE_PATH + "/" + user1projectId4 + "/members")
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/" + user1projectId4 + "/members")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("members.id", Matchers.hasItem(TestData.USER_ID_1));

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"projectId\":\" " + TestData.PROJECT_ID_1 + "\",  \"id\":\"" + TestData.USER_ID_2 + "\", \"role\":\"LESSOR\", \"email\":\"" + TestData.USER_EMAIL_2  + "\"}")
                .post(BASE_PATH + "/" + user1projectId4 + "/members")
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/" + user1projectId4 + "/members")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("members.id", Matchers.hasItem(TestData.USER_ID_1));

        final String user1projectId5 = given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_5 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"projectId\":\" " + TestData.PROJECT_ID_1 + "\",  \"id\":\"" + TestData.USER_ID_2 + "\", \"role\":\"LESSOR\", \"email\":\"" + TestData.USER_EMAIL_2 + "\"}")
                .post(BASE_PATH + "/" + user1projectId5 + "/members")
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/" + user1projectId5 + "/members")
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
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/" + user1projectId1 + "/members")
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
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/" + user1projectId1 + "/members")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .body("members.id", Matchers.hasItem(TestData.USER_ID_1));
    }

    @Test
    void updateProjectMember_SUCCESS() {
        final String user1projectId1 = given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
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
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"projectId\":\" " + user1projectId1 + "\",  \"id\":\"" + TestData.USER_ID_1 + "\", \"email\":\"max.mustermann@example.org\", \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .patch(BASE_PATH + "/" + user1projectId1 + "/members/" + TestData.USER_ID_1)
                .then()
                .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void updateProjectMember_noAuthentication() {
        final String user1projectId1 = given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/" + user1projectId1 + "/members")
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
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        final String user2projectId1 = given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"projectId\":\" " + TestData.PROJECT_ID_1 + "\",  \"id\":\"" + TestData.USER_ID_2 + "\", \"role\":\"LESSOR\", \"email\":\"" + TestData.USER_EMAIL_2 + "\"}")
                .post(BASE_PATH + "/" + user1projectId1 + "/members")
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/" + user1projectId1 + "/members")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON);

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .delete(BASE_PATH + "/" + user1projectId1 + "/members/" + TestData.USER_ID_2)
                .then()
                .statusCode(Matchers.equalTo(204));
    }

    @Test
    void deleteProjectMember_noFound() {
        final String user1projectId1 = given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/" + user1projectId1 + "/members")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON);

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .delete(BASE_PATH + "/" + user1projectId1 + "/members/" + TestData.USER_ID_1)
                .then()
                .statusCode(Matchers.equalTo(404));
    }

    @Test
    void deleteProjectMember_noAuthentication() {
        final String user1projectId1 = given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/" + user1projectId1 + "/members")
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

    @Test
    void updateProjectMemberTest() {
        final String user1projectId1 = given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
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
    void deleteProjectMemberTest(){
        final String user1projectId1 = given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        final String user2projectId1 = given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"projectId\":\" " + TestData.PROJECT_ID_1 + "\",  \"id\":\"" + TestData.USER_ID_2 + "\", \"role\":\"LESSOR\", \"email\":\"" + TestData.USER_EMAIL_2  + "\"}")
                .post(BASE_PATH + "/" + user1projectId1 + "/members")
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/" + user1projectId1 + "/members")
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON);

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .delete(BASE_PATH + "/" + user1projectId1 + "/members/" + TestData.USER_ID_2)
                .then()
                .statusCode(Matchers.equalTo(204));
    }
}