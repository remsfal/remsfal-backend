package de.remsfal.service.boundary;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.test.TestData;

import static org.hamcrest.Matchers.containsString;

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
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body(json)
            .post(BASE_PATH)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .contentType(ContentType.JSON)
            .header("location", Matchers.containsString(BASE_PATH + "/"))
            .and().body("id", Matchers.notNullValue())
            .and().body("title", Matchers.equalTo(TestData.PROJECT_TITLE))
            .and().body("members.id", Matchers.hasItem(TestData.USER_ID.toString()))
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
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
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
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
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
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
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
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(projectUrl)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.equalTo(projectId))
            .and().body("title", Matchers.equalTo(TestData.PROJECT_TITLE));
    }

    @Test
    void getProjects_FAILED_requestedNumberToHigh() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .queryParam("limit", 120)
            .get(BASE_PATH)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void getProjects_FAILED_negativeOffset() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .queryParam("offset", -3)
            .get(BASE_PATH)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void getProjects_FAILED_negativeLimit() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
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
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE + i + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode());
        }

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
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
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
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
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
            .post(BASE_PATH)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");

        final String user1projectId2 = given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"title\":\"" + TestData.PROJECT_TITLE_2 + "\"}")
            .post(BASE_PATH)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");

        final String user2projectId3 = given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"title\":\"" + TestData.PROJECT_TITLE_3 + "\"}")
            .post(BASE_PATH)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100)))
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
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
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
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"title\":\"" + TestData.PROJECT_TITLE_1 + "\"}")
            .post(BASE_PATH)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
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
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"title\":\"" + TestData.PROJECT_TITLE + "\"}")
            .post(BASE_PATH)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
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
    void metricGenerated_afterGetProjectsList() {
        given()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .when().get(BASE_PATH)
            .then().statusCode(Status.OK.getStatusCode());
        given()
            .when().get("/q/metrics")
            .then().statusCode(200)
            .body(containsString("GetProjectsListTimer"));
    }

    @Test
    void metricGenerated_afterCreateProject() {
        String json = "{ \"title\":\"" + TestData.PROJECT_TITLE + "\"}";
        given()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body(json)
            .when().post(BASE_PATH)
            .then().statusCode(Status.CREATED.getStatusCode());
        given()
            .when().get("/q/metrics")
            .then().statusCode(200)
            .body(containsString("CreateProjectTimer"));
    }

    @Test
    void metricGenerated_afterGetSingleProject() {
        String projectId = given()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body("{ \"title\":\"" + TestData.PROJECT_TITLE + "\"}")
            .when().post(BASE_PATH)
            .then().statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");
        given()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .when().get(BASE_PATH + "/" + projectId)
            .then().statusCode(Status.OK.getStatusCode());
        given()
            .when().get("/q/metrics")
            .then().statusCode(200)
            .body(containsString("GetSingleProjectTimer"));
    }

    @Test
    void metricGenerated_afterUpdateProject() {
        String projectId = given()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body("{ \"title\":\"" + TestData.PROJECT_TITLE + "\"}")
            .when().post(BASE_PATH)
            .then().statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");
        given()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body("{ \"title\":\"updated\"}")
            .when().patch(BASE_PATH + "/" + projectId)
            .then().statusCode(Status.OK.getStatusCode());
        given()
            .when().get("/q/metrics")
            .then().statusCode(200)
            .body(containsString("UpdateProjectTimer"));
    }

    @Test
    void metricGenerated_afterDeleteProject() {
        String projectId = given()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body("{ \"title\":\"" + TestData.PROJECT_TITLE + "\"}")
            .when().post(BASE_PATH)
            .then().statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");
        given()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .when().delete(BASE_PATH + "/" + projectId)
            .then().statusCode(Status.NO_CONTENT.getStatusCode());
        given()
            .when().get("/q/metrics")
            .then().statusCode(200)
            .body(containsString("deleteProjectTimer"));
    }
    @Test
    void getSubResources_SUCCESS() {
        // 1️⃣ Projekt anlegen, um eine gültige ID zu haben
        String projectId = given()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"Test Project For Subresources\" }")
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        // 2️⃣ Alle Sub-Resource-Pfadnamen in einer Liste
        List<String> subPaths = List.of("apartments", "commercial", "storage");

        // 3️⃣ Für jeden Pfad eine GET-Anfrage ausführen
        for (String subPath : subPaths) {
            given()
                    .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                    .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100)))
                    .when()
                    .get(BASE_PATH + "/" + projectId + "/" + subPath)
                    .then()
                    .statusCode(Status.OK.getStatusCode());
        }
    }

}