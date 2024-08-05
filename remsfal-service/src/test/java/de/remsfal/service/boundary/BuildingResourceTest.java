package de.remsfal.service.boundary;

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
public class BuildingResourceTest extends AbstractResourceTest{

    static final String BASE_PATH_GET = "/api/v1/projects/b9440c43-b5c0-4951-9c28-000000000001/properties/b9440c43-b5c0-4951-9c25-000000000001/buildings/b9440c43-b5c0-4951-9c25-000000000001";

    static final String BASE_PATH_POST = "/api/v1/projects/b9440c43-b5c0-4951-9c28-000000000001/properties/b9440c43-b5c0-4951-9c25-000000000001/buildings/";

    @BeforeEach
    protected void setupTestUsers() {
        super.setupTestUsers();
    }

    @Test
    void getBuilding_FAILED_noAuthentication() {
        given()
                .when()
                .get(BASE_PATH_GET)
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getBuilding_FAILED_buildingDoesNotExist() {
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .get(BASE_PATH_GET)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void getBuilding_SUCCESS() {

        final String user1building1 = given()
                .when()
                .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"description\":\"" + TestData.BUILDING_DESCRIPTION_1 + "\"," +
                        " \"livingSpace\":\"" + TestData.APARTMENT_LIVING_SPACE_1 + "\"," +
                        " \"title\":\"" + TestData.BUILDING_TITLE_1 + "\"," +
                        " \"commercialSpace\":\"" + TestData.COMMERCIAL_COMMERCIAL_SPACE_1 + "\"," +
                        " \"usableSpace\":\"" + TestData.BUILDING_USABLE_SPACE_1+ "\"," +
                        " \"heatingSpace\":\"" + TestData.APARTMENT_HEATING_SPACE_1 + "\"," +
                        " \"rent\":\"" + TestData.BUILDING_RENT_1 + "\"," +
                        " \"address\": {" +
                        "     \"street\": \"" + TestData.ADDRESS_STREET_1 + "\"," +
                        "     \"city\": \"" + TestData.ADDRESS_CITY_1 + "\"," +
                        "     \"province\": \"" + TestData.ADDRESS_PROVINCE_1 + "\"," +
                        "     \"zip\": \"" + TestData.ADDRESS_ZIP_1 + "\"," +
                        "     \"country\": \"" + TestData.ADDRESS_COUNTRY_1 + "\"" +
                        " } }")
                .post(BASE_PATH_POST)
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .extract().path("id");


     /*   given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .get(BASE_PATH_GET)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());*/
    }

}
