package de.remsfal.service.boundary.project;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.remsfal.service.TestData;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import org.hamcrest.Matchers;

@QuarkusTest
class TaskResourceTest extends AbstractProjectResourceTest {

    static final String BASE_PATH = "/api/v1/projects/{projectId}";
    static final String TASK_PATH = BASE_PATH + "/tasks";
    static final String DEFECT_PATH = BASE_PATH + "/defects";

    @BeforeEach
    protected void setupTestProjects() {
        super.setupTestProjects();
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { TASK_PATH, DEFECT_PATH })
    void getTasks_FAILED_noAuthentication(String path) {
        given()
            .when()
            .get(path, TestData.PROJECT_ID)
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { TASK_PATH, DEFECT_PATH })
    void createTask_SUCCESS_taskIsCreated(String path) {
        final String json = "{ \"title\":\"" + TestData.TASK_TITLE + "\"}";
        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body(json)
            .post(path, TestData.PROJECT_ID)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .contentType(ContentType.JSON)
            .header("location", Matchers.containsString(path.replace("{projectId}", TestData.PROJECT_ID)))
            .and().body("id", Matchers.notNullValue())
            .and().body("title", Matchers.equalTo(TestData.TASK_TITLE));

        long enties = entityManager
            .createQuery("SELECT count(task) FROM TaskEntity task where task.title = :title", Long.class)
            .setParameter("title", TestData.TASK_TITLE)
            .getSingleResult();
        assertEquals(1, enties);
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { TASK_PATH, DEFECT_PATH })
    void createTask_FAILED_idIsProvided(String path) {
        final String json = "{ \"title\":\"" + TestData.TASK_TITLE + "\","
            + "\"id\":\"anyId\"}";
        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body(json)
            .post(path, TestData.PROJECT_ID)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { TASK_PATH, DEFECT_PATH })
    void createTask_FAILED_noTitle(String path) {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(ContentType.JSON)
                .body("{ \"title\":\" \"}")
                .post(path, TestData.PROJECT_ID)
                .then()
                .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { TASK_PATH, DEFECT_PATH })
    void getTask_SUCCESS_sameTaskIsReturned(String path) {
        final String json = "{ \"title\":\"" + TestData.TASK_TITLE + "\","
        	+ "\"description\":\"" + TestData.TASK_DESCRIPTION + "\"}";

        final Response res = given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .post(path, TestData.PROJECT_ID)
            .thenReturn();

        final String taskId = res.then()
            .contentType(MediaType.APPLICATION_JSON)
            .extract().path("id");

        final String taskUrl = res.then()
            .statusCode(Status.CREATED.getStatusCode())
            .and().body("id", Matchers.equalTo(taskId))
            .and().body("title", Matchers.equalTo(TestData.TASK_TITLE))
            .and().body("description", Matchers.equalTo(TestData.TASK_DESCRIPTION.replace("\\n", "\n")))
            .header("location", Matchers.startsWith("http://localhost:8081/api/v1/projects"))
            .header("location", Matchers.endsWith(taskId))
            .extract().header("location");

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(taskUrl)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.equalTo(taskId))
            .and().body("title", Matchers.equalTo(TestData.TASK_TITLE))
            .and().body("description", Matchers.equalTo(TestData.TASK_DESCRIPTION.replace("\\n", "\n")));
    }

}