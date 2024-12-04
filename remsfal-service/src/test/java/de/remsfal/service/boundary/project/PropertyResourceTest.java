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
    void getProperties_SUCCESS_propertiesCorrectlyReturned() {
        // Insert test data
        insertProperty(TestData.PROPERTY_ID_1, TestData.PROJECT_ID, TestData.PROPERTY_TITLE_1, TestData.PROPERTY_REG_ENTRY_1, TestData.PROPERTY_DESCRIPTION_1, TestData.PROPERTY_PLOT_AREA_1);
        insertProperty(TestData.PROPERTY_ID_2, TestData.PROJECT_ID, TestData.PROPERTY_TITLE_2, TestData.PROPERTY_REG_ENTRY_2, TestData.PROPERTY_DESCRIPTION_2, TestData.PROPERTY_PLOT_AREA_2);

        insertBuilding(TestData.BUILDING_ID_1, TestData.PROJECT_ID, TestData.PROPERTY_ID_1, TestData.BUILDING_TITLE_1, TestData.BUILDING_DESCRIPTION_1, TestData.BUILDING_LIVING_SPACE_1, TestData.BUILDING_COMMERCIAL_SPACE_1, TestData.BUILDING_USABLE_SPACE_1, TestData.BUILDING_HEATING_SPACE_1, TestData.ADDRESS_ID_1);
        insertBuilding(TestData.BUILDING_ID_2, TestData.PROJECT_ID, TestData.PROPERTY_ID_1, TestData.BUILDING_TITLE_2, TestData.BUILDING_DESCRIPTION_2, TestData.BUILDING_LIVING_SPACE_2, TestData.BUILDING_COMMERCIAL_SPACE_2, TestData.BUILDING_USABLE_SPACE_2, TestData.BUILDING_HEATING_SPACE_2, TestData.ADDRESS_ID_2);

        insertApartment(TestData.APARTMENT_ID_1, TestData.PROJECT_ID, TestData.BUILDING_ID_1, TestData.APARTMENT_TITLE_1, TestData.APARTMENT_LOCATION_1, TestData.APARTMENT_DESCRIPTION_1, TestData.APARTMENT_LIVING_SPACE_1, TestData.APARTMENT_USABLE_SPACE_1, TestData.APARTMENT_HEATING_SPACE_1);
        insertApartment(TestData.APARTMENT_ID_2, TestData.PROJECT_ID, TestData.BUILDING_ID_1, TestData.APARTMENT_TITLE_2, TestData.APARTMENT_LOCATION_2, TestData.APARTMENT_DESCRIPTION_2, TestData.APARTMENT_LIVING_SPACE_2, TestData.APARTMENT_USABLE_SPACE_2, TestData.APARTMENT_HEATING_SPACE_2);

        insertGarage(TestData.GARAGE_ID_1, TestData.PROJECT_ID, TestData.BUILDING_ID_1, TestData.GARAGE_TITLE_1, TestData.GARAGE_LOCATION_1, TestData.GARAGE_DESCRIPTION_1, TestData.GARAGE_USABLE_SPACE_1);
        insertGarage(TestData.GARAGE_ID_2, TestData.PROJECT_ID, TestData.BUILDING_ID_1, TestData.GARAGE_TITLE_2, TestData.GARAGE_LOCATION_2, TestData.GARAGE_DESCRIPTION_2, TestData.GARAGE_USABLE_SPACE_2);


        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .queryParam("limit", 10)
            .queryParam("offset", 0)
            .queryParam("projectId", TestData.PROJECT_ID)
            .get(BASE_PATH + "/{projectId}/properties", TestData.PROJECT_ID)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("first", Matchers.equalTo(0))
            .and().body("size", Matchers.equalTo(2))
            .and().body("total", Matchers.equalTo(2))
            .and().body("nodes.size()", Matchers.is(2))
            .and().body("nodes[0].key", Matchers.equalTo(TestData.PROPERTY_ID_2))
            .and().body("nodes[1].key", Matchers.equalTo(TestData.PROPERTY_ID_1))
            .and().body("nodes[0].type", Matchers.equalTo("Property"))
            .and().body("nodes[1].type", Matchers.equalTo("Property"))
            .and().body("nodes[0].title", Matchers.equalTo(TestData.PROPERTY_TITLE_2))
            .and().body("nodes[1].title", Matchers.equalTo(TestData.PROPERTY_TITLE_1))
            .and().body("nodes[0].description", Matchers.equalTo(TestData.PROPERTY_DESCRIPTION_2))
            .and().body("nodes[1].description", Matchers.equalTo(TestData.PROPERTY_DESCRIPTION_1))
            .and().body("nodes[1].children[0].key", Matchers.equalTo(TestData.BUILDING_ID_1))
            .and().body("nodes[1].children[0].type", Matchers.equalTo("Building"))
            .and().body("nodes[1].children[0].title", Matchers.equalTo(TestData.BUILDING_TITLE_1))
            .and().body("nodes[1].children[0].description", Matchers.equalTo(TestData.BUILDING_DESCRIPTION_1))
            .and().body("nodes[1].children[1].key", Matchers.equalTo(TestData.BUILDING_ID_2))
            .and().body("nodes[1].children[1].type", Matchers.equalTo("Building"))
            .and().body("nodes[1].children[1].title", Matchers.equalTo(TestData.BUILDING_TITLE_2))
            .and().body("nodes[1].children[1].description", Matchers.equalTo(TestData.BUILDING_DESCRIPTION_2))
            .and().body("nodes[1].children[0].children[0].key", Matchers.equalTo(TestData.APARTMENT_ID_1))
            .and().body("nodes[1].children[0].children[0].type", Matchers.equalTo("Apartment"))
            .and().body("nodes[1].children[0].children[0].title", Matchers.equalTo(TestData.APARTMENT_TITLE_1))
            .and().body("nodes[1].children[0].children[0].description", Matchers.equalTo(TestData.APARTMENT_DESCRIPTION_1))
            .and().body("nodes[1].children[0].children[1].key", Matchers.equalTo(TestData.APARTMENT_ID_2))
            .and().body("nodes[1].children[0].children[1].type", Matchers.equalTo("Apartment"))
            .and().body("nodes[1].children[0].children[1].title", Matchers.equalTo(TestData.APARTMENT_TITLE_2))
            .and().body("nodes[1].children[0].children[1].description", Matchers.equalTo(TestData.APARTMENT_DESCRIPTION_2))
            .and().body("nodes[1].children[0].children[2].key", Matchers.equalTo(TestData.GARAGE_ID_1))
            .and().body("nodes[1].children[0].children[2].type", Matchers.equalTo("Garage"))
            .and().body("nodes[1].children[0].children[2].title", Matchers.equalTo(TestData.GARAGE_TITLE_1))
            .and().body("nodes[1].children[0].children[2].description", Matchers.equalTo(TestData.GARAGE_DESCRIPTION_1))
            .and().body("nodes[1].children[0].children[3].key", Matchers.equalTo(TestData.GARAGE_ID_2))
            .and().body("nodes[1].children[0].children[3].type", Matchers.equalTo("Garage"))
            .and().body("nodes[1].children[0].children[3].title", Matchers.equalTo(TestData.GARAGE_TITLE_2))
            .and().body("nodes[1].children[0].children[3].description", Matchers.equalTo(TestData.GARAGE_DESCRIPTION_2))
            .log().body();
    }

    private void insertProperty(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO PROPERTY (ID, PROJECT_ID, TITLE, LAND_REGISTER_ENTRY, DESCRIPTION, PLOT_AREA) VALUES (?,?,?,?,?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .setParameter(3, params[2])
                .setParameter(4, params[3])
                .setParameter(5, params[4])
                .setParameter(6, params[5])
                .executeUpdate());
    }

    private void insertBuilding(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO BUILDING (ID, PROJECT_ID, PROPERTY_ID, TITLE, DESCRIPTION, LIVING_SPACE, COMMERCIAL_SPACE, USABLE_SPACE, HEATING_SPACE, ADDRESS_ID) VALUES (?,?,?,?,?,?,?,?,?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .setParameter(3, params[2])
                .setParameter(4, params[3])
                .setParameter(5, params[4])
                .setParameter(6, params[5])
                .setParameter(7, params[6])
                .setParameter(8, params[7])
                .setParameter(9, params[8])
                .setParameter(10, params[9])
                .executeUpdate());
    }

    private void insertApartment(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO APARTMENT (ID, PROJECT_ID, BUILDING_ID,TITLE, LOCATION, DESCRIPTION, LIVING_SPACE, USABLE_SPACE, HEATING_SPACE) VALUES (?,?,?,?,?,?,?,?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .setParameter(3, params[2])
                .setParameter(4, params[3])
                .setParameter(5, params[4])
                .setParameter(6, params[5])
                .setParameter(7, params[6])
                .setParameter(8, params[7])
                .setParameter(9, params[8])
                .executeUpdate());
    }

    private void insertGarage(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO GARAGE (ID, PROJECT_ID, BUILDING_ID, TITLE, LOCATION, DESCRIPTION, USABLE_SPACE) VALUES (?,?,?,?,?,?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .setParameter(3, params[2])
                .setParameter(4, params[3])
                .setParameter(5, params[4])
                .setParameter(6, params[5])
                .setParameter(7, params[6])
                .executeUpdate());
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
            .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .queryParam("offset", "10")
            .get(BASE_PATH + "/{projectId}/properties", TestData.PROJECT_ID)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("first", Matchers.equalTo(10))
            .and().body("size", Matchers.equalTo(0))
            .and().body("total", Matchers.equalTo(1))
            .and().body("nodes.size()", Matchers.is(0));
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
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .delete(BASE_PATH + "/{projectId}/properties/{propertyId}", TestData.PROJECT_ID, TestData.PROPERTY_ID_1)
            .then()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
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
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
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
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
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