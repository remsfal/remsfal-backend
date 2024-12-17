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

    @Override
    @BeforeEach
    protected void setupTestProjects() {
        super.setupTestUsers();
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
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
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
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
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
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body("{ \"title\":\" \"}")
            .post(BASE_PATH + "/{projectId}/properties", TestData.PROJECT_ID)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void getProperties_SUCCESS_propertiesCorrectlyReturned() {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROPERTY (ID, PROJECT_ID, TITLE, LAND_REGISTER_ENTRY, DESCRIPTION, PLOT_AREA) VALUES (?,?,?,?,?,?)")
            .setParameter(1, TestData.PROPERTY_ID_1)
            .setParameter(2, TestData.PROJECT_ID)
            .setParameter(3, TestData.PROPERTY_TITLE_1)
            .setParameter(4, TestData.PROPERTY_REG_ENTRY_1)
            .setParameter(5, TestData.PROPERTY_DESCRIPTION_1)
            .setParameter(6, TestData.PROPERTY_PLOT_AREA_1)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROPERTY (ID, PROJECT_ID, TITLE, LAND_REGISTER_ENTRY, DESCRIPTION, PLOT_AREA) VALUES (?,?,?,?,?,?)")
            .setParameter(1, TestData.PROPERTY_ID_2)
            .setParameter(2, TestData.PROJECT_ID)
            .setParameter(3, TestData.PROPERTY_TITLE_2)
            .setParameter(4, TestData.PROPERTY_REG_ENTRY_2)
            .setParameter(5, TestData.PROPERTY_DESCRIPTION_2)
            .setParameter(6, TestData.PROPERTY_PLOT_AREA_2)
            .executeUpdate());

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(BASE_PATH + "/{projectId}/properties", TestData.PROJECT_ID)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("first", Matchers.equalTo(0))
            .and().body("size", Matchers.equalTo(2))
            .and().body("total", Matchers.equalTo(2))
            .and().body("properties.size()", Matchers.is(2))
            .and().body("properties.id", Matchers.hasItems(TestData.PROPERTY_ID_1, TestData.PROPERTY_ID_2))
            .and().body("properties.title", Matchers.hasItems(TestData.PROPERTY_TITLE_1, TestData.PROPERTY_TITLE_2))
            .and().body("properties.landRegisterEntry", Matchers.hasItems(TestData.PROPERTY_REG_ENTRY_1, TestData.PROPERTY_REG_ENTRY_2))
            .and().body("properties.description", Matchers.hasItems(TestData.PROPERTY_DESCRIPTION_1, TestData.PROPERTY_DESCRIPTION_2))
            .and().body("properties.plotArea", Matchers.hasItems(TestData.PROPERTY_PLOT_AREA_1, TestData.PROPERTY_PLOT_AREA_2));
    }

    @Test
    void getProperties_SUCCESS_noPropertiesForOffsetTooHigh() {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROPERTY (ID, PROJECT_ID, TITLE, LAND_REGISTER_ENTRY, DESCRIPTION, PLOT_AREA) VALUES (?,?,?,?,?,?)")
            .setParameter(1, TestData.PROPERTY_ID_1)
            .setParameter(2, TestData.PROJECT_ID)
            .setParameter(3, TestData.PROPERTY_TITLE_1)
            .setParameter(4, TestData.PROPERTY_REG_ENTRY_1)
            .setParameter(5, TestData.PROPERTY_DESCRIPTION_1)
            .setParameter(6, TestData.PROPERTY_PLOT_AREA_1)
            .executeUpdate());

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .queryParam("offset", "10")
            .get(BASE_PATH + "/{projectId}/properties", TestData.PROJECT_ID)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("first", Matchers.equalTo(10))
            .and().body("size", Matchers.equalTo(0))
            .and().body("total", Matchers.equalTo(1))
            .and().body("properties.size()", Matchers.is(0));
    }

    @Test
    void deleteProperty_SUCCESS_propertyIsdeleted() {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROPERTY (ID, PROJECT_ID, TITLE) VALUES (?,?,?)")
            .setParameter(1, TestData.PROPERTY_ID_1)
            .setParameter(2, TestData.PROJECT_ID)
            .setParameter(3, TestData.PROPERTY_TITLE_1)
            .executeUpdate());

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .delete(BASE_PATH + "/{projectId}/properties/{propertyId}", TestData.PROJECT_ID, TestData.PROPERTY_ID_1)
            .then()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH + "/{projectId}/properties/{propertyId}", TestData.PROJECT_ID, TestData.PROPERTY_ID_1)
            .then()
            .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void updateProperty_SUCCESS_propertyCorrectlyUpdated() {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROPERTY (ID, PROJECT_ID, TITLE) VALUES (?,?,?)")
            .setParameter(1, TestData.PROPERTY_ID_1)
            .setParameter(2, TestData.PROJECT_ID)
            .setParameter(3, TestData.PROPERTY_TITLE_1)
            .executeUpdate());
        final String json = "{ \"title\":\"" + TestData.PROPERTY_TITLE_2 + "\","
            + "\"landRegisterEntry\":\"" + TestData.PROPERTY_REG_ENTRY_2 + "\","
            + "\"description\":\"" + TestData.PROPERTY_DESCRIPTION_2 + "\","
            + "\"plotArea\":\"" + TestData.PROPERTY_PLOT_AREA_2 + "\"}";
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .patch(BASE_PATH + "/{projectId}/properties/{propertyId}", TestData.PROJECT_ID, TestData.PROPERTY_ID_1)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.equalTo(TestData.PROPERTY_ID_1))
            .and().body("title", Matchers.equalTo(TestData.PROPERTY_TITLE_2))
            .and().body("landRegisterEntry", Matchers.equalTo(TestData.PROPERTY_REG_ENTRY_2))
            .and().body("description", Matchers.equalTo(TestData.PROPERTY_DESCRIPTION_2))
            .and().body("plotArea", Matchers.equalTo(TestData.PROPERTY_PLOT_AREA_2));

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH + "/{projectId}/properties/{propertyId}", TestData.PROJECT_ID, TestData.PROPERTY_ID_1)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.equalTo(TestData.PROPERTY_ID_1))
            .and().body("title", Matchers.equalTo(TestData.PROPERTY_TITLE_2))
            .and().body("landRegisterEntry", Matchers.equalTo(TestData.PROPERTY_REG_ENTRY_2))
            .and().body("description", Matchers.equalTo(TestData.PROPERTY_DESCRIPTION_2))
            .and().body("plotArea", Matchers.equalTo(TestData.PROPERTY_PLOT_AREA_2));
    }

    @Test
    void getProperty_SUCCESS_samePropertyIsReturned() {
        final String json = "{ \"title\":\"" + TestData.PROPERTY_TITLE + "\","
            + "\"landRegisterEntry\":\"" + TestData.PROPERTY_REG_ENTRY + "\","
            + "\"description\":\"" + TestData.PROPERTY_DESCRIPTION + "\","
            + "\"plotArea\":\"" + TestData.PROPERTY_PLOT_AREA + "\"}";

        final Response res = given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
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
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
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