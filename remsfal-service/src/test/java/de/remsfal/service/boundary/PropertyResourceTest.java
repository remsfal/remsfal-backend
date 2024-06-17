package de.remsfal.service.boundary;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
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
class PropertyResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/projects";

    @BeforeEach
    void setupTestUsers() {
        super.setupTestUsers();
    }

    @Test
    void getProperty_FAILED_noAuthentication() {
        given()
            .when()
            .get(BASE_PATH + "/anyId/properties/anyId")
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void createProperty_SUCCESS_propertyIsCreated() {
        final String projectId = given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"title\":\"" + TestData.PROJECT_TITLE + "\"}")
                .post(BASE_PATH)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().path("id");

        final String json = "{ \"title\":\"" + TestData.PROPERTY_TITLE + "\"}";
        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body(json)
            .post(BASE_PATH + "/{projectId}/properties", projectId)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .contentType(ContentType.JSON)
            .header("location", Matchers.containsString(BASE_PATH + "/"));

        long enties = entityManager
            .createQuery("SELECT count(property) FROM PropertyEntity property where property.title = :title", Long.class)
            .setParameter("title", TestData.PROPERTY_TITLE)
            .getSingleResult();
        assertEquals(1, enties);
    }

}