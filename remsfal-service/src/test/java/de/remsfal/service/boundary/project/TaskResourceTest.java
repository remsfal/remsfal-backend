package de.remsfal.service.boundary.project;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.remsfal.service.TestData;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

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
        super.setupTestUsers();
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
    void createTask_FAILED_userIsNotMember(String path) {
        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"title\":\"" + TestData.TASK_TITLE + "\"}")
            .post(path, TestData.PROJECT_ID)
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
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

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { TASK_PATH, DEFECT_PATH })
    void getTask_SUCCESS_sameTaskIsReturned_USERID_isNULL(String path) {
        final String json = "{ \"title\":\"" + TestData.TASK_TITLE + "\","
                + "\"description\":\"" + TestData.TASK_DESCRIPTION + "\"}";

        final Response res = given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .post(path, TestData.PROJECT_ID)
                .thenReturn();

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .get(path,TestData.PROJECT_ID)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("id", Matchers.equalTo(res.then().extract().path("id")))
                .and().body("title", Matchers.equalTo(TestData.TASK_TITLE))
                .and().body("description", Matchers.equalTo(TestData.TASK_DESCRIPTION.replace("\\n", "\n")));
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { TASK_PATH, DEFECT_PATH })
    void getTask_FAILED_userIsNotMember(String path) {
        final String taskId = given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"title\":\"" + TestData.TASK_TITLE + "\"}")
            .post(path, TestData.PROJECT_ID)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(path + "/{taskId}", TestData.PROJECT_ID, taskId)
            .then()
            .statusCode(Status.OK.getStatusCode());

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .get(path + "/{taskId}", TestData.PROJECT_ID, taskId)
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { TASK_PATH, DEFECT_PATH })
    void getTask_FAILED_userIsNotPrivileged(String path) {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, USER_ROLE) VALUES (?,?,?)")
            .setParameter(1, TestData.PROJECT_ID)
            .setParameter(2, TestData.USER_ID_2)
            .setParameter(3, "CARETAKER")
            .executeUpdate());

        final String taskId = given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"title\":\"" + TestData.TASK_TITLE + "\"}")
            .post(path, TestData.PROJECT_ID)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .get(path + "/{taskId}", TestData.PROJECT_ID, taskId)
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { TASK_PATH, DEFECT_PATH })
    void updateTask_SUCCESS_descriptionIsUpdated(String path) {
        final String taskId = given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"title\":\"" + TestData.TASK_TITLE + "\"}")
            .post(path, TestData.PROJECT_ID)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"description\":\"" + TestData.TASK_DESCRIPTION + "\"}")
            .patch(path + "/{taskId}", TestData.PROJECT_ID, taskId)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.equalTo(taskId))
            .and().body("title", Matchers.equalTo(TestData.TASK_TITLE))
            .and().body("description", Matchers.equalTo(TestData.TASK_DESCRIPTION.replace("\\n", "\n")));

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(path + "/{taskId}", TestData.PROJECT_ID, taskId)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .and().body("description", Matchers.equalTo(TestData.TASK_DESCRIPTION.replace("\\n", "\n")));
    }
    
    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { TASK_PATH, DEFECT_PATH })
    void updateTask_FAILED_userIsNotMember(String path) {
        final String taskId = given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"title\":\"" + TestData.TASK_TITLE + "\"}")
            .post(path, TestData.PROJECT_ID)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"description\":\"" + TestData.TASK_DESCRIPTION + "\"}")
            .patch(path + "/{taskId}", TestData.PROJECT_ID, taskId)
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { TASK_PATH, DEFECT_PATH })
    void deleteTask_SUCCESS_taskIsdeleted(String path) {
        final String taskId = given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"title\":\"" + TestData.TASK_TITLE + "\"}")
            .post(path, TestData.PROJECT_ID)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .delete(path + "/{taskId}", TestData.PROJECT_ID, taskId)
            .then()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(path + "/{taskId}", TestData.PROJECT_ID, taskId)
            .then()
            .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { TASK_PATH, DEFECT_PATH })
    void deleteTask_FAILED_userIsNotMember(String path) {
        final String taskId = given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"title\":\"" + TestData.TASK_TITLE + "\"}")
            .post(path, TestData.PROJECT_ID)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .delete(path + "/{taskId}", TestData.PROJECT_ID, taskId)
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @CsvSource({ TASK_PATH + ",TASK", DEFECT_PATH + ",DEFECT" })
    void getTasks_SUCCESS_myTasksAreReturned(String path, String type) {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO TASK (ID, TYPE, PROJECT_ID, TITLE, STATUS, OWNER_ID, CREATED_BY) VALUES (?,?,?,?,?,?,?)")
            .setParameter(1, UUID.randomUUID().toString())
            .setParameter(2, type)
            .setParameter(3, TestData.PROJECT_ID)
            .setParameter(4, TestData.TASK_TITLE_1)
            .setParameter(5, "OPEN")
            .setParameter(6, TestData.USER_ID_1)
            .setParameter(7, TestData.USER_ID_1)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO TASK (ID, TYPE, PROJECT_ID, TITLE, STATUS, OWNER_ID, CREATED_BY) VALUES (?,?,?,?,?,?,?)")
            .setParameter(1, UUID.randomUUID().toString())
            .setParameter(2, type)
            .setParameter(3, TestData.PROJECT_ID)
            .setParameter(4, TestData.TASK_TITLE_2)
            .setParameter(5, "OPEN")
            .setParameter(6, TestData.USER_ID_2)
            .setParameter(7, TestData.USER_ID_1)
            .executeUpdate());

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .queryParam("owner", TestData.USER_ID_1)
            .get(path, TestData.PROJECT_ID)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("tasks.size()", Matchers.is(1))
            .and().body("tasks.title", Matchers.hasItems(TestData.TASK_TITLE_1))
            .and().body("tasks.status", Matchers.hasItems("OPEN"))
            .and().body("tasks.owner", Matchers.hasItems(TestData.USER_ID_1));
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @CsvSource({ TASK_PATH + ",TASK", DEFECT_PATH + ",DEFECT" })
    void getTasks_SUCCESS_openTasksAreReturned(String path, String type) {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO TASK (ID, TYPE, PROJECT_ID, TITLE, STATUS, OWNER_ID, CREATED_BY) VALUES (?,?,?,?,?,?,?)")
            .setParameter(1, UUID.randomUUID().toString())
            .setParameter(2, type)
            .setParameter(3, TestData.PROJECT_ID)
            .setParameter(4, TestData.TASK_TITLE_1)
            .setParameter(5, "CLOSED")
            .setParameter(6, TestData.USER_ID_1)
            .setParameter(7, TestData.USER_ID_1)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO TASK (ID, TYPE, PROJECT_ID, TITLE, STATUS, OWNER_ID, CREATED_BY) VALUES (?,?,?,?,?,?,?)")
            .setParameter(1, UUID.randomUUID().toString())
            .setParameter(2, type)
            .setParameter(3, TestData.PROJECT_ID)
            .setParameter(4, TestData.TASK_TITLE_2)
            .setParameter(5, "OPEN")
            .setParameter(6, TestData.USER_ID_2)
            .setParameter(7, TestData.USER_ID_1)
            .executeUpdate());

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .queryParam("status", "OPEN")
            .get(path, TestData.PROJECT_ID)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("tasks.size()", Matchers.is(1))
            .and().body("tasks.title", Matchers.hasItems(TestData.TASK_TITLE_2))
            .and().body("tasks.status", Matchers.hasItems("OPEN"))
            .and().body("tasks.owner", Matchers.hasItems(TestData.USER_ID_2));
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @CsvSource({ TASK_PATH + ",TASK", DEFECT_PATH + ",DEFECT" })
    void getTasks_SUCCESS_myOpenTasksAreReturned(String path, String type) {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO TASK (ID, TYPE, PROJECT_ID, TITLE, STATUS, OWNER_ID, CREATED_BY) VALUES (?,?,?,?,?,?,?)")
            .setParameter(1, UUID.randomUUID().toString())
            .setParameter(2, type)
            .setParameter(3, TestData.PROJECT_ID)
            .setParameter(4, TestData.TASK_TITLE_1)
            .setParameter(5, "OPEN")
            .setParameter(6, TestData.USER_ID_1)
            .setParameter(7, TestData.USER_ID_1)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO TASK (ID, TYPE, PROJECT_ID, TITLE, STATUS, OWNER_ID, CREATED_BY) VALUES (?,?,?,?,?,?,?)")
            .setParameter(1, UUID.randomUUID().toString())
            .setParameter(2, type)
            .setParameter(3, TestData.PROJECT_ID)
            .setParameter(4, TestData.TASK_TITLE_2)
            .setParameter(5, "OPEN")
            .setParameter(6, TestData.USER_ID_2)
            .setParameter(7, TestData.USER_ID_1)
            .executeUpdate());

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .queryParam("status", "OPEN")
            .queryParam("owner", TestData.USER_ID_1)
            .get(path, TestData.PROJECT_ID)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("tasks.size()", Matchers.is(1))
            .and().body("tasks.title", Matchers.hasItems(TestData.TASK_TITLE_1))
            .and().body("tasks.status", Matchers.hasItems("OPEN"))
            .and().body("tasks.owner", Matchers.hasItems(TestData.USER_ID_1));
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @CsvSource({ TASK_PATH + ",TASK", DEFECT_PATH + ",DEFECT" })
    void getTasks_SUCCESS_allTasksAreReturned(String path, String type) {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO TASK (ID, TYPE, PROJECT_ID, TITLE, STATUS, OWNER_ID, CREATED_BY) VALUES (?,?,?,?,?,?,?)")
            .setParameter(1, UUID.randomUUID().toString())
            .setParameter(2, type)
            .setParameter(3, TestData.PROJECT_ID)
            .setParameter(4, TestData.TASK_TITLE_1)
            .setParameter(5, "CLOSED")
            .setParameter(6, TestData.USER_ID_1)
            .setParameter(7, TestData.USER_ID_1)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO TASK (ID, TYPE, PROJECT_ID, TITLE, STATUS, OWNER_ID, CREATED_BY) VALUES (?,?,?,?,?,?,?)")
            .setParameter(1, UUID.randomUUID().toString())
            .setParameter(2, type)
            .setParameter(3, TestData.PROJECT_ID)
            .setParameter(4, TestData.TASK_TITLE_2)
            .setParameter(5, "OPEN")
            .setParameter(6, TestData.USER_ID_2)
            .setParameter(7, TestData.USER_ID_1)
            .executeUpdate());

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(path, TestData.PROJECT_ID)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("tasks.size()", Matchers.is(2))
            .and().body("tasks.title", Matchers.hasItems(TestData.TASK_TITLE_1, TestData.TASK_TITLE_2))
            .and().body("tasks.status", Matchers.hasItems("CLOSED", "OPEN"))
            .and().body("tasks.owner", Matchers.hasItems(TestData.USER_ID_1, TestData.USER_ID_2));
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = { TASK_PATH, DEFECT_PATH })
    void getTasks_FAILED_userIsNotMember(String path) {
        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"title\":\"" + TestData.TASK_TITLE + "\"}")
            .post(path, TestData.PROJECT_ID)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .get(path, TestData.PROJECT_ID)
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

}