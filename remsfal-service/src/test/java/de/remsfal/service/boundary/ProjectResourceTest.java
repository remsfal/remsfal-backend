package de.remsfal.service.boundary;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.service.TestData;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import org.hamcrest.Matchers;

@QuarkusTest
class ProjectResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/projects";

    @BeforeEach
    void setupTestUsers() {
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
            .and().body("projects.size()", Matchers.is(2))
            .and().body("projects.id", Matchers.hasItems(user1projectId1, user1projectId2))
            .and().body("projects.title", Matchers.hasItems(TestData.PROJECT_TITLE_1, TestData.PROJECT_TITLE_2));

        given()
        .when()
        .cookie(buildCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
        .get(BASE_PATH)
        .then()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
        .and().body("projects.size()", Matchers.is(1))
        .and().body("projects.id", Matchers.hasItems(user2projectId3))
        .and().body("projects.title", Matchers.hasItems(TestData.PROJECT_TITLE_3));
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

        final String user1projectId2 = given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"title\":\"" + TestData.PROJECT_TITLE_2 + "\"}")
            .post(BASE_PATH)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");

        final String user1projectId3 = given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"title\":\"" + TestData.PROJECT_TITLE_3 + "\"}")
            .post(BASE_PATH)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");

        final String user1projectId4 = given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"title\":\"" + TestData.PROJECT_TITLE_4 + "\"}")
            .post(BASE_PATH)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");

        final String user1projectId5 = given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"title\":\"" + TestData.PROJECT_TITLE_5 + "\"}")
            .post(BASE_PATH)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");

        // TODO
    }

}