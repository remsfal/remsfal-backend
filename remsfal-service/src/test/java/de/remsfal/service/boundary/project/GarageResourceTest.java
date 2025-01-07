package de.remsfal.service.boundary.project;

import de.remsfal.service.TestData;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static io.restassured.RestAssured.given;

@QuarkusTest
class GarageResourceTest extends AbstractProjectResourceTest {

    static final String BASE_PATH = "/api/v1/projects";

    private static final String PROJECT_JSON = "{ \"title\": \"" + TestData.PROJECT_TITLE + "\" }";

    private static final String PROPERTY_JSON = "{ \"title\": \"" + TestData.PROPERTY_TITLE + "\" }";

    private static final String BUILDING_JSON = "{ \"description\":\"" + TestData.BUILDING_DESCRIPTION + "\"," +
        " \"livingSpace\":\"" + TestData.APARTMENT_LIVING_SPACE + "\"," +
        " \"title\":\"" + TestData.BUILDING_TITLE + "\"," +
        " \"commercialSpace\":\"" + TestData.COMMERCIAL_COMMERCIAL_SPACE + "\"," +
        " \"usableSpace\":\"" + TestData.BUILDING_USABLE_SPACE + "\"," +
        " \"heatingSpace\":\"" + TestData.APARTMENT_HEATING_SPACE + "\"," +
        " \"address\": {" +
        "     \"street\": \"" + TestData.ADDRESS_STREET + "\"," +
        "     \"city\": \"" + TestData.ADDRESS_CITY + "\"," +
        "     \"province\": \"" + TestData.ADDRESS_PROVINCE + "\"," +
        "     \"zip\": \"" + TestData.ADDRESS_ZIP + "\"," +
        "     \"country\": \"" + TestData.ADDRESS_COUNTRY + "\"" +
        " } }";

    private static final String GARAGE_JSON = "{ \"title\": \"" + TestData.GARAGE_TITLE + "\"," +
        " \"description\": \"" + TestData.GARAGE_DESCRIPTION + "\"," +
        " \"usableSpace\": " + 12.8f + "," +
        " \"location\": \"" + TestData.GARAGE_LOCATION + "\" }";

    @BeforeEach
    protected void setupTestUsers() {
        super.setupTestUsers();
    }

    private String createProject() {
        return given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body(PROJECT_JSON)
            .post(BASE_PATH)
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .extract().path("id");
    }

    private String createProperty(String projectId) {
        return given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body(PROPERTY_JSON)
            .post(BASE_PATH + "/" + projectId + "/properties")
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .extract().path("id");
    }

    private String createBuilding(String projectId, String propertyId) {
        return given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(BUILDING_JSON)
            .post(BASE_PATH + "/" + projectId + "/properties/" + propertyId + "/buildings")
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .extract().path("id");
    }

    @Test
    void getGarage_FAILED_garageDoesNotExist() {
        final String projectId = createProject();
        final String propertyId = createProperty(projectId);

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .get(BASE_PATH + "/" + projectId + "/properties/" + propertyId +
                "/buildings/building1/garages/nonExistingGarage")
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void getGarage_SUCCESS() {
        final String projectId = createProject();
        final String propertyId = createProperty(projectId);
        final String buildingId = createBuilding(projectId, propertyId);

        final String garageId = given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(GARAGE_JSON)
            .post(BASE_PATH + "/" + projectId + "/properties/" + propertyId + "/buildings/" + buildingId + "/garages")
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .extract().path("id");

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .get(BASE_PATH + "/" + projectId + "/properties/" + propertyId + "/buildings/" + buildingId + "/garages/" +
                garageId)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("title", org.hamcrest.Matchers.equalTo(TestData.GARAGE_TITLE));
    }

    @Test
    void createGarage_SUCCESS() {
        final String projectId = createProject();
        final String propertyId = createProperty(projectId);
        final String buildingId = createBuilding(projectId, propertyId);

        final String garageId = given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(GARAGE_JSON)
            .post(BASE_PATH + "/" + projectId + "/properties/" + propertyId + "/buildings/" + buildingId + "/garages")
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .extract().path("id");

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .get(BASE_PATH + "/" + projectId + "/properties/" + propertyId + "/buildings/" + buildingId + "/garages/" +
                garageId)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("title", org.hamcrest.Matchers.equalTo(TestData.GARAGE_TITLE));
    }

    @Test
    void updateGarage_SUCCESS() {
        final String projectId = createProject();
        final String propertyId = createProperty(projectId);
        final String buildingId = createBuilding(projectId, propertyId);

        final String garageId = given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(GARAGE_JSON)
            .post(BASE_PATH + "/" + projectId + "/properties/" + propertyId + "/buildings/" + buildingId + "/garages")
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .extract().path("id");

        final String updatedGarageJson = "{ \"title\": \"" + TestData.GARAGE_TITLE_2 + "\"," +
            " \"description\": \"" + TestData.GARAGE_DESCRIPTION_2 + "\"," +
            " \"usableSpace\": " + 15.5f + "," +
            " \"location\": \"" + TestData.GARAGE_LOCATION_2 + "\" }";

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(updatedGarageJson)
            .patch(
                BASE_PATH + "/" + projectId + "/properties/" + propertyId + "/buildings/" + buildingId + "/garages/" +
                    garageId)
            .then()
            .statusCode(Response.Status.OK.getStatusCode());

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .get(BASE_PATH + "/" + projectId + "/properties/" + propertyId + "/buildings/" + buildingId + "/garages/" +
                garageId)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("title", org.hamcrest.Matchers.equalTo(TestData.GARAGE_TITLE_2))
            .body("description", org.hamcrest.Matchers.equalTo(TestData.GARAGE_DESCRIPTION_2))
            .body("usableSpace", org.hamcrest.Matchers.equalTo(15.5f))
            .body("location", org.hamcrest.Matchers.equalTo(TestData.GARAGE_LOCATION_2));
    }

    @Test
    void deleteGarage_SUCCESS() {
        final String projectId = createProject();
        final String propertyId = createProperty(projectId);
        final String buildingId = createBuilding(projectId, propertyId);

        final String garageId = given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(GARAGE_JSON)
            .post(BASE_PATH + "/" + projectId + "/properties/" + propertyId + "/buildings/" + buildingId + "/garages")
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .extract().path("id");

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .delete(
                BASE_PATH + "/" + projectId + "/properties/" + propertyId + "/buildings/" + buildingId + "/garages/" +
                    garageId)
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .get(BASE_PATH + "/" + projectId + "/properties/" + propertyId + "/buildings/" + buildingId + "/garages/" +
                garageId)
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

}
