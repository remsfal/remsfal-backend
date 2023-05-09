package de.remsfal.service.boundary;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.service.TestData;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.contains;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.hamcrest.Matchers;

@QuarkusTest
class ProjectResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/projects";

    @BeforeEach
    void setupTestUsers() {
        super.setupTestUsers();
    }

    @Test
    void createProject_SUCCESS_projectIsCreated() {
        final String json = "{ \"title\":\"" + TestData.PROJECT_TITLE + "\"}";
        given()
            .when()
            .log().all()
            .header("Authorization", "Bearer " + TestData.USER_TOKEN)
            .contentType(ContentType.JSON)
            .body(json)
            .post(BASE_PATH)
            .then()
            .log().all()
            .statusCode(Status.CREATED.getStatusCode())
            .contentType(ContentType.JSON)
            .header("location", Matchers.contains(BASE_PATH + "/"))
            .and().body("id", Matchers.notNullValue())
            .and().body("title", Matchers.equalTo(TestData.PROJECT_TITLE));

        long enties = entityManager
            .createQuery("SELECT count(project) FROM ProjectEntity project where project.title = :title", Long.class)
            .setParameter("title", TestData.PROJECT_TITLE)
            .getSingleResult();
        assertEquals(1, enties);
    }

    @Test
    void getProject_SUCCESS_sameProjectIsReturned() {
        final String json = "{ \"title\":\"" + TestData.PROJECT_TITLE + "\"}";
        
        final Response res = given()
            .when()
            .header("Authorization", "Bearer " + TestData.USER_TOKEN)
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
            .when().get(projectUrl)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.equalTo(projectId))
            .and().body("title", Matchers.equalTo(TestData.PROJECT_TITLE));
    }

}