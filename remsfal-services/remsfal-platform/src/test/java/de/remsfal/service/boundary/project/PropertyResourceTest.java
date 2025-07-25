package de.remsfal.service.boundary.project;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.service.boundary.AbstractResourceTest;
import de.remsfal.test.TestData;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import org.hamcrest.Matchers;

@QuarkusTest
class PropertyResourceTest extends AbstractResourceTest {

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
        setupTestProperties();
        setupTestBuildings();

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH + "/{projectId}/properties", TestData.PROJECT_ID)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("properties.size()", Matchers.is(2))
            .and().body("properties[0].key", Matchers.equalTo(TestData.PROPERTY_ID_2))
            .and().body("properties[1].key", Matchers.equalTo(TestData.PROPERTY_ID_1))
            .and().body("properties[0].data.type", Matchers.equalTo("PROPERTY"))
            .and().body("properties[1].data.type", Matchers.equalTo("PROPERTY"))
            .and().body("properties[0].data.title", Matchers.equalTo(TestData.PROPERTY_TITLE_2))
            .and().body("properties[1].data.title", Matchers.equalTo(TestData.PROPERTY_TITLE_1))
            .and().body("properties[0].data.description", Matchers.equalTo(TestData.PROPERTY_DESCRIPTION_2))
            .and().body("properties[1].data.description", Matchers.equalTo(TestData.PROPERTY_DESCRIPTION_1))
            .and().body("properties[1].children[0].key", Matchers.equalTo(TestData.BUILDING_ID_1))
            .and().body("properties[1].children[0].data.type", Matchers.equalTo("BUILDING"))
            .and().body("properties[1].children[0].data.title", Matchers.equalTo(TestData.BUILDING_TITLE_1))
            .and().body("properties[1].children[0].data.description", Matchers.equalTo(TestData.BUILDING_DESCRIPTION_1))
            .and().body("properties[1].children[1].key", Matchers.equalTo(TestData.BUILDING_ID_2))
            .and().body("properties[1].children[1].data.type", Matchers.equalTo("BUILDING"))
            .and().body("properties[1].children[1].data.title", Matchers.equalTo(TestData.BUILDING_TITLE_2))
            .and().body("properties[1].children[1].data.description", Matchers.equalTo(TestData.BUILDING_DESCRIPTION_2))
            .and().body("properties[1].children[0].children[0].key", Matchers.equalTo(TestData.APARTMENT_ID_1))
            .and().body("properties[1].children[0].children[0].data.type", Matchers.equalTo("APARTMENT"))
            .and().body("properties[1].children[0].children[0].data.title", Matchers.equalTo(TestData.APARTMENT_TITLE_1))
            .and().body("properties[1].children[0].children[0].data.description", Matchers.equalTo(TestData.APARTMENT_DESCRIPTION_1))
            .and().body("properties[1].children[0].children[1].key", Matchers.equalTo(TestData.APARTMENT_ID_2))
            .and().body("properties[1].children[0].children[1].data.type", Matchers.equalTo("APARTMENT"))
            .and().body("properties[1].children[0].children[1].data.title", Matchers.equalTo(TestData.APARTMENT_TITLE_2))
            .and().body("properties[1].children[0].children[1].data.description", Matchers.equalTo(TestData.APARTMENT_DESCRIPTION_2))
            .and().body("properties[1].children[0].children[2].key", Matchers.equalTo(TestData.COMMERCIAL_ID_1))
            .and().body("properties[1].children[0].children[2].data.type", Matchers.equalTo("COMMERCIAL"))
            .and().body("properties[1].children[0].children[2].data.title", Matchers.equalTo(TestData.COMMERCIAL_TITLE_1))
            .and().body("properties[1].children[0].children[2].data.description", Matchers.equalTo(TestData.COMMERCIAL_DESCRIPTION_1))
            .and().body("properties[1].children[0].children[3].key", Matchers.equalTo(TestData.COMMERCIAL_ID_2))
            .and().body("properties[1].children[0].children[3].data.type", Matchers.equalTo("COMMERCIAL"))
            .and().body("properties[1].children[0].children[3].data.title", Matchers.equalTo(TestData.COMMERCIAL_TITLE_2))
            .and().body("properties[1].children[0].children[3].data.description", Matchers.equalTo(TestData.COMMERCIAL_DESCRIPTION_2))
            .and().body("properties[1].children[0].children[4].key", Matchers.equalTo(TestData.STORAGE_ID_1))
            .and().body("properties[1].children[0].children[4].data.type", Matchers.equalTo("STORAGE"))
            .and().body("properties[1].children[0].children[4].data.title", Matchers.equalTo(TestData.STORAGE_TITLE_1))
            .and().body("properties[1].children[0].children[4].data.description", Matchers.equalTo(TestData.STORAGE_DESCRIPTION_1))
            .and().body("properties[1].children[0].children[5].key", Matchers.equalTo(TestData.STORAGE_ID_2))
            .and().body("properties[1].children[0].children[5].data.type", Matchers.equalTo("STORAGE"))
            .and().body("properties[1].children[0].children[5].data.title", Matchers.equalTo(TestData.STORAGE_TITLE_2))
            .and().body("properties[1].children[0].children[5].data.description", Matchers.equalTo(TestData.STORAGE_DESCRIPTION_2))
            .log().body();
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
            + "\"landRegistry\":\"" + TestData.PROPERTY_LAND_REGISTRY_2 + "\","
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
            .and().body("landRegistry", Matchers.equalTo(TestData.PROPERTY_LAND_REGISTRY_2))
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
            .and().body("landRegistry", Matchers.equalTo(TestData.PROPERTY_LAND_REGISTRY_2))
            .and().body("description", Matchers.equalTo(TestData.PROPERTY_DESCRIPTION_2))
            .and().body("plotArea", Matchers.equalTo(TestData.PROPERTY_PLOT_AREA_2));
    }

    @Test
    void getProperty_SUCCESS_samePropertyIsReturned() {
        final String json = "{ \"title\":\"" + TestData.PROPERTY_TITLE + "\","
            + "\"landRegistry\":\"" + TestData.PROPERTY_LAND_REGISTRY_2 + "\","
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
            .and().body("landRegistry", Matchers.equalTo(TestData.PROPERTY_LAND_REGISTRY_2))
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
            .and().body("landRegistry", Matchers.equalTo(TestData.PROPERTY_LAND_REGISTRY_2))
            .and().body("description", Matchers.equalTo(TestData.PROPERTY_DESCRIPTION))
            .and().body("plotArea", Matchers.equalTo(TestData.PROPERTY_PLOT_AREA));
    }

}