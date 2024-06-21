package de.remsfal.service.boundary.project;

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
class PropertyResourceTest extends AbstractProjectResourceTest {

    static final String BASE_PATH = "/api/v1/projects";

    @BeforeEach
    protected void setupTestProjects() {
        super.setupTestProjects();
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
        final String json = "{ \"title\":\"" + TestData.PROPERTY_TITLE + "\"}";
        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body(json)
            .post(BASE_PATH + "/{projectId}/properties", TestData.PROJECT_ID)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .contentType(ContentType.JSON)
            .header("location", Matchers.containsString(BASE_PATH + "/"))
            .and().body("id", Matchers.notNullValue())
            .and().body("title", Matchers.equalTo(TestData.PROPERTY_TITLE));

        long enties = entityManager
            .createQuery("SELECT count(property) FROM PropertyEntity property where property.title = :title", Long.class)
            .setParameter("title", TestData.PROPERTY_TITLE)
            .getSingleResult();
        assertEquals(1, enties);
    }

    @Test
    void createProperty_FAILED_idIsProvided() {
        final String json = "{ \"title\":\"" + TestData.PROPERTY_TITLE + "\","
            + "\"id\":\"anyId\"}";
        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body(json)
            .post(BASE_PATH + "/{projectId}/properties", TestData.PROJECT_ID)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void createProperty_FAILED_noTitle() {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(ContentType.JSON)
                .body("{ \"title\":\" \"}")
                .post(BASE_PATH + "/{projectId}/properties", TestData.PROJECT_ID)
                .then()
                .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void getProperty_SUCCESS_samePropertyIsReturned() {
        final String json = "{ \"title\":\"" + TestData.PROPERTY_TITLE + "\","
        	+ "\"landRegisterEntry\":\"" + TestData.PROPERTY_REG_ENTRY + "\","
        	+ "\"description\":\"" + TestData.PROPERTY_DESCRIPTION + "\","
        	+ "\"plotArea\":\"" + TestData.PROPERTY_PLOT_AREA + "\"}";

        final Response res = given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .post(BASE_PATH + "/{projectId}/properties", TestData.PROJECT_ID)
            .thenReturn();

        final String propertyId = res.then()
            .contentType(MediaType.APPLICATION_JSON)
            .extract().path("id");

        final String propertyUrl = res.then()
            .statusCode(Status.CREATED.getStatusCode())
            .and().body("id", Matchers.equalTo(propertyId))
            .and().body("title", Matchers.equalTo(TestData.PROPERTY_TITLE))
            .and().body("landRegisterEntry", Matchers.equalTo(TestData.PROPERTY_REG_ENTRY))
            .and().body("description", Matchers.equalTo(TestData.PROPERTY_DESCRIPTION))
            .and().body("plotArea", Matchers.equalTo(TestData.PROPERTY_PLOT_AREA))
            .header("location", Matchers.startsWith("http://localhost:8081/api/v1/projects"))
            .header("location", Matchers.endsWith(propertyId))
            .extract().header("location");

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(propertyUrl)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.equalTo(propertyId))
            .and().body("title", Matchers.equalTo(TestData.PROPERTY_TITLE))
            .and().body("landRegisterEntry", Matchers.equalTo(TestData.PROPERTY_REG_ENTRY))
            .and().body("description", Matchers.equalTo(TestData.PROPERTY_DESCRIPTION))
            .and().body("plotArea", Matchers.equalTo(TestData.PROPERTY_PLOT_AREA));
    }

}