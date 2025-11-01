package de.remsfal.service.boundary.project;

import static io.restassured.RestAssured.given;

import java.time.Duration;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.service.boundary.AbstractResourceTest;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@QuarkusTest
class BuildingResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/projects/{projectId}/properties/{propertyId}/buildings";

    @Override
    @BeforeEach
    protected void setupTestProperties() {
        super.setupTestUsers();
        super.setupTestProjects();
        super.setupTestProperties();
    }

    @Test
    void getBuilding_FAILED_buildingDoesNotExist() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"description\":\"" + TestData.BUILDING_DESCRIPTION_1 + "\"," +
                " \"livingSpace\":\"" + TestData.APARTMENT_LIVING_SPACE_1 + "\"," +
                " \"title\":\"" + TestData.BUILDING_TITLE_1 + "\"," +
                " \"usableSpace\":\"" + TestData.BUILDING_USABLE_SPACE_1 + "\"," +
                " \"heatingSpace\":\"" + TestData.APARTMENT_HEATING_SPACE_1 + "\"," +
                " \"address\": {" +
                "     \"street\": \"" + TestData.ADDRESS_STREET_1 + "\"," +
                "     \"city\": \"" + TestData.ADDRESS_CITY_1 + "\"," +
                "     \"province\": \"" + TestData.ADDRESS_PROVINCE_1 + "\"," +
                "     \"zip\": \"" + TestData.ADDRESS_ZIP_1 + "\"," +
                "     \"countryCode\": \"" + TestData.ADDRESS_COUNTRY_1 + "\"" +
                " } }")
            .post(BASE_PATH, TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID)
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .extract().path("id");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .get(BASE_PATH + "/" + TestData.BUILDING_ID_1, TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID)
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void getBuilding_SUCCESS() {
        final String user1building1 = given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"description\":\"" + TestData.BUILDING_DESCRIPTION_1 + "\"," +
                " \"livingSpace\":\"" + TestData.APARTMENT_LIVING_SPACE_1 + "\"," +
                " \"title\":\"" + TestData.BUILDING_TITLE_1 + "\"," +
                " \"usableSpace\":\"" + TestData.BUILDING_USABLE_SPACE_1 + "\"," +
                " \"heatingSpace\":\"" + TestData.APARTMENT_HEATING_SPACE_1 + "\"," +
                " \"address\": {" +
                "     \"street\": \"" + TestData.ADDRESS_STREET_1 + "\"," +
                "     \"city\": \"" + TestData.ADDRESS_CITY_1 + "\"," +
                "     \"province\": \"" + TestData.ADDRESS_PROVINCE_1 + "\"," +
                "     \"zip\": \"" + TestData.ADDRESS_ZIP_1 + "\"," +
                "     \"countryCode\": \"" + TestData.ADDRESS_COUNTRY_1 + "\"" +
                " } }")
            .post(BASE_PATH, TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID)
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .extract().path("id");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .get(BASE_PATH + "/" + user1building1, TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID)
            .then()
            .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    void createBuilding_SUCCESS() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(TestData.buildingBuilder2().id(null).build())
            .post(BASE_PATH, TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID)
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .body("id", Matchers.notNullValue())
            .body("type", Matchers.equalTo("BUILDING"))
            .body("title", Matchers.equalTo(TestData.BUILDING_TITLE_2))
            .body("location", Matchers.nullValue())
            .body("description", Matchers.equalTo(TestData.BUILDING_DESCRIPTION_2))
            .body("space", Matchers.equalTo(430.5f))
            .body("grossFloorArea", Matchers.equalTo(430.5f))
            .body("netFloorArea", Matchers.equalTo(400.0f))
            .body("constructionFloorArea", Matchers.equalTo(70.5f))
            .body("livingSpace", Matchers.nullValue())
            .body("usableSpace", Matchers.nullValue())
            .body("heatingSpace", Matchers.equalTo(420.75f))
            .body("address", Matchers.nullValue());
    }

    @Test
    void createBuilding_FAILED_wrongPropertyId() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"description\":\"" + TestData.BUILDING_DESCRIPTION_1 + "\"," +
                " \"livingSpace\":\"" + TestData.APARTMENT_LIVING_SPACE_1 + "\"," +
                " \"title\":\"" + TestData.BUILDING_TITLE_1 + "\"," +
                " \"usableSpace\":\"" + TestData.BUILDING_USABLE_SPACE_1 + "\"," +
                " \"heatingSpace\":\"" + TestData.APARTMENT_HEATING_SPACE_1 + "\"," +
                " \"address\": {" +
                "     \"street\": \"" + TestData.ADDRESS_STREET_1 + "\"," +
                "     \"city\": \"" + TestData.ADDRESS_CITY_1 + "\"," +
                "     \"province\": \"" + TestData.ADDRESS_PROVINCE_1 + "\"," +
                "     \"zip\": \"" + TestData.ADDRESS_ZIP_1 + "\"," +
                "     \"countryCode\": \"" + TestData.ADDRESS_COUNTRY_1 + "\"" +
                " } }")
            .post(BASE_PATH, TestData.PROJECT_ID.toString().replace("0", "1"), TestData.PROPERTY_ID.toString())
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void updateBuilding_SUCCESS() {
        final String user1building1 = given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"description\":\"" + TestData.BUILDING_DESCRIPTION_1 + "\"," +
                " \"livingSpace\":\"" + TestData.APARTMENT_LIVING_SPACE_1 + "\"," +
                " \"title\":\"" + TestData.BUILDING_TITLE_1 + "\"," +
                " \"usableSpace\":\"" + TestData.BUILDING_USABLE_SPACE_1 + "\"," +
                " \"heatingSpace\":\"" + TestData.APARTMENT_HEATING_SPACE_1 + "\"," +
                " \"address\": {" +
                "     \"street\": \"" + TestData.ADDRESS_STREET_1 + "\"," +
                "     \"city\": \"" + TestData.ADDRESS_CITY_1 + "\"," +
                "     \"province\": \"" + TestData.ADDRESS_PROVINCE_1 + "\"," +
                "     \"zip\": \"" + TestData.ADDRESS_ZIP_1 + "\"," +
                "     \"countryCode\": \"" + TestData.ADDRESS_COUNTRY_1 + "\"" +
                " } }")
            .post(BASE_PATH, TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID)
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .extract().path("id");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"description\":\"" + TestData.BUILDING_DESCRIPTION_1 + "\"," +
                " \"livingSpace\":\"" + TestData.APARTMENT_LIVING_SPACE_1 + "\"," +
                " \"title\":\"" + TestData.BUILDING_TITLE_1 + "\"," +
                " \"usableSpace\":\"" + TestData.BUILDING_USABLE_SPACE_1 + "\"," +
                " \"heatingSpace\":\"" + TestData.APARTMENT_HEATING_SPACE_1 + "\"," +
                " \"address\": {" +
                "     \"street\": \"Lavochkina Street\"," +
                "     \"city\": \"" + TestData.ADDRESS_CITY_1 + "\"," +
                "     \"province\": \"" + TestData.ADDRESS_PROVINCE_1 + "\"," +
                "     \"zip\": \"" + TestData.ADDRESS_ZIP_1 + "\"," +
                "     \"countryCode\": \"" + TestData.ADDRESS_COUNTRY_1 + "\"" +
                " } }")
            .patch(BASE_PATH + "/" + user1building1, TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID)
            .then()
            .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    void updateBuilding_FAILED_noAuthentication() {
        final String user1building1 = given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"description\":\"" + TestData.BUILDING_DESCRIPTION_1 + "\"," +
                " \"livingSpace\":\"" + TestData.APARTMENT_LIVING_SPACE_1 + "\"," +
                " \"title\":\"" + TestData.BUILDING_TITLE_1 + "\"," +
                " \"usableSpace\":\"" + TestData.BUILDING_USABLE_SPACE_1 + "\"," +
                " \"heatingSpace\":\"" + TestData.APARTMENT_HEATING_SPACE_1 + "\"," +
                " \"address\": {" +
                "     \"street\": \"" + TestData.ADDRESS_STREET_1 + "\"," +
                "     \"city\": \"" + TestData.ADDRESS_CITY_1 + "\"," +
                "     \"province\": \"" + TestData.ADDRESS_PROVINCE_1 + "\"," +
                "     \"zip\": \"" + TestData.ADDRESS_ZIP_1 + "\"," +
                "     \"countryCode\": \"" + TestData.ADDRESS_COUNTRY_1 + "\"" +
                " } }")
            .post(BASE_PATH, TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID)
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .extract().path("id");

        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"description\":\"" + TestData.BUILDING_DESCRIPTION_1 + "\"," +
                " \"livingSpace\":\"" + TestData.APARTMENT_LIVING_SPACE_1 + "\"," +
                " \"title\":\"" + TestData.BUILDING_TITLE_1 + "\"," +
                " \"usableSpace\":\"" + TestData.BUILDING_USABLE_SPACE_1 + "\"," +
                " \"heatingSpace\":\"" + TestData.APARTMENT_HEATING_SPACE_1 + "\"," +
                " \"address\": {" +
                "     \"street\": \"Lavochkina Street\"," +
                "     \"city\": \"" + TestData.ADDRESS_CITY_1 + "\"," +
                "     \"province\": \"" + TestData.ADDRESS_PROVINCE_1 + "\"," +
                "     \"zip\": \"" + TestData.ADDRESS_ZIP_1 + "\"," +
                "     \"countryCode\": \"" + TestData.ADDRESS_COUNTRY_1 + "\"" +
                " } }")
            .patch(BASE_PATH + "/" + user1building1, TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID)
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void updateBuilding_FAILED_notExist() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"description\":\"" + TestData.BUILDING_DESCRIPTION_1 + "\"," +
                " \"livingSpace\":\"" + TestData.APARTMENT_LIVING_SPACE_1 + "\"," +
                " \"title\":\"" + TestData.BUILDING_TITLE_1 + "\"," +
                " \"usableSpace\":\"" + TestData.BUILDING_USABLE_SPACE_1 + "\"," +
                " \"heatingSpace\":\"" + TestData.APARTMENT_HEATING_SPACE_1 + "\"," +
                " \"address\": {" +
                "     \"street\": \"Lavochkina Street\"," +
                "     \"city\": \"" + TestData.ADDRESS_CITY_1 + "\"," +
                "     \"province\": \"" + TestData.ADDRESS_PROVINCE_1 + "\"," +
                "     \"zip\": \"" + TestData.ADDRESS_ZIP_1 + "\"," +
                "     \"countryCode\": \"" + TestData.ADDRESS_COUNTRY_1 + "\"" +
                " } }")
            .patch(BASE_PATH + "/" + java.util.UUID.randomUUID(), TestData.PROJECT_ID, TestData.PROPERTY_ID)
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void deleteBuilding_SUCCESS() {
        final String user1building1 = given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"description\":\"" + TestData.BUILDING_DESCRIPTION_1 + "\"," +
                " \"livingSpace\":\"" + TestData.APARTMENT_LIVING_SPACE_1 + "\"," +
                " \"title\":\"" + TestData.BUILDING_TITLE_1 + "\"," +
                " \"usableSpace\":\"" + TestData.BUILDING_USABLE_SPACE_1 + "\"," +
                " \"heatingSpace\":\"" + TestData.APARTMENT_HEATING_SPACE_1 + "\"," +
                " \"address\": {" +
                "     \"street\": \"" + TestData.ADDRESS_STREET_1 + "\"," +
                "     \"city\": \"" + TestData.ADDRESS_CITY_1 + "\"," +
                "     \"province\": \"" + TestData.ADDRESS_PROVINCE_1 + "\"," +
                "     \"zip\": \"" + TestData.ADDRESS_ZIP_1 + "\"," +
                "     \"countryCode\": \"" + TestData.ADDRESS_COUNTRY_1 + "\"" +
                " } }")
            .post(BASE_PATH, TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID)
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .extract().path("id");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .delete(BASE_PATH + "/" + user1building1, TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID)
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void createAndGetBuilding_SUCCESS_shortcut() {
        final String user1building1 = given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"description\":\"" + TestData.BUILDING_DESCRIPTION_1 + "\"," +
                " \"livingSpace\":\"" + TestData.APARTMENT_LIVING_SPACE_1 + "\"," +
                " \"title\":\"" + TestData.BUILDING_TITLE_1 + "\"," +
                " \"usableSpace\":\"" + TestData.BUILDING_USABLE_SPACE_1 + "\"," +
                " \"heatingSpace\":\"" + TestData.APARTMENT_HEATING_SPACE_1 + "\"," +
                " \"address\": {" +
                "     \"street\": \"" + TestData.ADDRESS_STREET_1 + "\"," +
                "     \"city\": \"" + TestData.ADDRESS_CITY_1 + "\"," +
                "     \"province\": \"" + TestData.ADDRESS_PROVINCE_1 + "\"," +
                "     \"zip\": \"" + TestData.ADDRESS_ZIP_1 + "\"," +
                "     \"countryCode\": \"" + TestData.ADDRESS_COUNTRY_1 + "\"" +
                " } }")
            .post(BASE_PATH, TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID)
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .extract().path("id");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .get("/api/v1/projects/{projectId}/buildings/" + user1building1, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Response.Status.OK.getStatusCode());

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"usableSpace\":\"56.7\"}")
            .patch("/api/v1/projects/{projectId}/buildings/" + user1building1, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Response.Status.OK.getStatusCode());

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .delete("/api/v1/projects/{projectId}/buildings/" + user1building1, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

}