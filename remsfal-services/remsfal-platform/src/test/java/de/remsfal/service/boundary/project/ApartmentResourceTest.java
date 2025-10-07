package de.remsfal.service.boundary.project;

import de.remsfal.service.boundary.AbstractResourceTest;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class ApartmentResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/projects/{projectId}/properties/{propertyId}/" +
        "buildings/{buildingId}/apartments";

    @Override
    @BeforeEach
    protected void setupTestProjects() {
        super.setupTestUsers();
        super.setupTestProjects();
        super.setupTestProperties();
    }

    @Test
    void getApartment_FAILED_noAuthentication() {
        given()
            .when()
            .get(BASE_PATH + "/{apartmentId}", TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID,
                TestData.BUILDING_ID, TestData.APARTMENT_ID)
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getApartment_SUCCESS_ApartmentCorrectlyReturned() {
        setupTestBuildings();
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH + "/{apartmentId}", TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID,
                TestData.BUILDING_ID, TestData.APARTMENT_ID)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .and().body("id", Matchers.equalTo(TestData.APARTMENT_ID_1.toString()))
            .and().body("title", Matchers.equalTo(TestData.APARTMENT_TITLE_1))
            .and().body("description", Matchers.equalTo(TestData.APARTMENT_DESCRIPTION_1))
            .and().body("livingSpace", Matchers.equalTo(TestData.APARTMENT_LIVING_SPACE_1))
            .and().body("usableSpace", Matchers.equalTo(TestData.APARTMENT_USABLE_SPACE_1))
            .and().body("heatingSpace", Matchers.equalTo(TestData.APARTMENT_HEATING_SPACE_1))
            .and().body("location", Matchers.equalTo(TestData.APARTMENT_LOCATION_1));
    }

    @ParameterizedTest
    @ValueSource(strings = "{ \"title\":\"" + TestData.APARTMENT_TITLE_2 + "\"}")
    void createApartment_SUCCESS_propertyIsCreated(String json) {
        setupTestBuildings();
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .post(BASE_PATH, TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID, TestData.BUILDING_ID_2)
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .header("location", Matchers.containsString(BASE_PATH.replace("{projectId}", TestData.PROJECT_ID.toString())
                .replace("{propertyId}", TestData.PROPERTY_ID.toString())
                .replace("{buildingId}", TestData.BUILDING_ID_2.toString()) + "/"))
            .and().body("id", Matchers.notNullValue())
            .and().body("title", Matchers.equalTo(TestData.APARTMENT_TITLE_2));

        long entities = entityManager
            .createQuery("SELECT count(apartment) FROM ApartmentEntity apartment where apartment.buildingId = :buildingId",
                long.class)
            .setParameter("buildingId", TestData.BUILDING_ID_2.toString())
            .getSingleResult();
        assertEquals(1, entities);
    }

    @Test
    void deleteApartment_SUCCESS_apartmentIsDeleted() {
        setupTestBuildings();

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .delete(BASE_PATH + "/{apartmentId}", TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID,
                TestData.BUILDING_ID, TestData.APARTMENT_ID)
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH + "/{apartmentId}", TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID,
                TestData.BUILDING_ID, TestData.APARTMENT_ID)
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = "{ \"title\":\"" + TestData.APARTMENT_TITLE_2 + "\"}")
    void updateApartment_SUCCESS_apartmentIsUpdated(final String json) {
        setupTestBuildings();

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .patch(BASE_PATH + "/{apartmentId}", TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID,
                TestData.BUILDING_ID, TestData.APARTMENT_ID)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .and().body("id", Matchers.equalTo(TestData.APARTMENT_ID.toString()))
            .and().body("title", Matchers.equalTo(TestData.APARTMENT_TITLE_2))
            .and().body("description", Matchers.equalTo(TestData.APARTMENT_DESCRIPTION_1))
            .and().body("livingSpace", Matchers.equalTo(TestData.APARTMENT_LIVING_SPACE_1))
            .and().body("heatingSpace", Matchers.equalTo(TestData.APARTMENT_HEATING_SPACE_1))
            .and().body("location", Matchers.equalTo(TestData.APARTMENT_LOCATION_1))
            .and().body("usableSpace", Matchers.equalTo(TestData.APARTMENT_USABLE_SPACE_1));

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .get(BASE_PATH + "/{apartmentId}", TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID,
                TestData.BUILDING_ID, TestData.APARTMENT_ID)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .and().body("id", Matchers.equalTo(TestData.APARTMENT_ID.toString()))
            .and().body("title", Matchers.equalTo(TestData.APARTMENT_TITLE_2));

    }

}
