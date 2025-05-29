package de.remsfal.service.boundary.project;

import de.remsfal.service.TestData;
import de.remsfal.service.boundary.AbstractResourceTest;
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

    protected void setupTestApartment() {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO BUILDING (ID, PROPERTY_ID, PROJECT_ID, ADDRESS_ID, TITLE)" +
                " VALUES (?,?,?,?,?)")
            .setParameter(1, TestData.BUILDING_ID)
            .setParameter(2, TestData.PROPERTY_ID)
            .setParameter(3, TestData.PROJECT_ID)
            .setParameter(4, TestData.ADDRESS_ID)
            .setParameter(5, TestData.APARTMENT_TITLE)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO APARTMENT (ID, BUILDING_ID, PROJECT_ID, " +
                "LOCATION, LIVING_SPACE, HEATING_SPACE, TITLE, DESCRIPTION, USABLE_SPACE) " +
                "VALUES (?,?,?,?,?,?,?,?,?)")
            .setParameter(1, TestData.APARTMENT_ID)
            .setParameter(2, TestData.BUILDING_ID)
            .setParameter(3, TestData.PROJECT_ID)
            .setParameter(4, TestData.APARTMENT_LOCATION)
            .setParameter(5, TestData.APARTMENT_LIVING_SPACE)
            .setParameter(6, TestData.APARTMENT_HEATING_SPACE)
            .setParameter(7, TestData.APARTMENT_TITLE)
            .setParameter(8, TestData.APARTMENT_DESCRIPTION)
            .setParameter(9, TestData.APARTMENT_USABLE_SPACE)
            .executeUpdate()
        );
    }

    @Test
    void getApartment_FAILED_noAuthentication() {
        given()
            .when()
            .get(BASE_PATH + "/{apartmentId}", TestData.PROJECT_ID, TestData.PROPERTY_ID,
                TestData.BUILDING_ID, TestData.APARTMENT_ID)
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getApartment_SUCCESS_ApartmentCorrectlyReturned() {
        setupTestApartment();
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH + "/{apartmentId}", TestData.PROJECT_ID, TestData.PROPERTY_ID,
                TestData.BUILDING_ID, TestData.APARTMENT_ID)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .and().body("id", Matchers.equalTo(TestData.APARTMENT_ID))
            .and().body("title", Matchers.equalTo(TestData.APARTMENT_TITLE))
            .and().body("description", Matchers.equalTo(TestData.APARTMENT_DESCRIPTION))
            .and().body("livingSpace", Matchers.equalTo(TestData.APARTMENT_LIVING_SPACE))
            .and().body("usableSpace", Matchers.equalTo(TestData.APARTMENT_USABLE_SPACE))
            .and().body("heatingSpace", Matchers.equalTo(TestData.APARTMENT_HEATING_SPACE))
            .and().body("location", Matchers.equalTo(TestData.APARTMENT_LOCATION));
    }

    @ParameterizedTest
    @ValueSource(strings = "{ \"title\":\"" + TestData.APARTMENT_TITLE_2 + "\"}")
    void createApartment_SUCCESS_propertyIsCreated(String json) {
        setupTestApartment();
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .post(BASE_PATH, TestData.PROJECT_ID, TestData.PROPERTY_ID, TestData.BUILDING_ID)
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .header("location", Matchers.containsString(BASE_PATH.replace("{projectId}", TestData.PROJECT_ID)
                .replace("{propertyId}", TestData.PROPERTY_ID)
                .replace("{buildingId}", TestData.BUILDING_ID) + "/"))
            .and().body("id", Matchers.notNullValue())
            .and().body("title", Matchers.equalTo(TestData.APARTMENT_TITLE_2));

        long entities = entityManager
            .createQuery("SELECT count(apartment) FROM ApartmentEntity apartment where apartment.title = :title",
                long.class)
            .setParameter("title", TestData.APARTMENT_TITLE_2)
            .getSingleResult();
        assertEquals(1, entities);
    }

    @Test
    void deleteApartment_SUCCESS_apartmentIsDeleted() {
        setupTestApartment();

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .delete(BASE_PATH + "/{apartmentId}", TestData.PROJECT_ID, TestData.PROPERTY_ID,
                TestData.BUILDING_ID, TestData.APARTMENT_ID)
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH + "/{apartmentId}", TestData.PROJECT_ID, TestData.PROPERTY_ID,
                TestData.BUILDING_ID, TestData.APARTMENT_ID)
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = "{ \"title\":\"" + TestData.APARTMENT_TITLE_2 + "\"}")
    void updateApartment_SUCCESS_apartmentIsUpdated(final String json) {
        setupTestApartment();

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .patch(BASE_PATH + "/{apartmentId}", TestData.PROJECT_ID, TestData.PROPERTY_ID,
                TestData.BUILDING_ID, TestData.APARTMENT_ID)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .and().body("id", Matchers.equalTo(TestData.APARTMENT_ID))
            .and().body("title", Matchers.equalTo(TestData.APARTMENT_TITLE_2))
            .and().body("description", Matchers.equalTo(TestData.APARTMENT_DESCRIPTION))
            .and().body("livingSpace", Matchers.equalTo(TestData.APARTMENT_LIVING_SPACE))
            .and().body("heatingSpace", Matchers.equalTo(TestData.APARTMENT_HEATING_SPACE))
            .and().body("location", Matchers.equalTo(TestData.APARTMENT_LOCATION))
            .and().body("usableSpace", Matchers.equalTo(TestData.APARTMENT_USABLE_SPACE));

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .get(BASE_PATH + "/{apartmentId}", TestData.PROJECT_ID, TestData.PROPERTY_ID,
                TestData.BUILDING_ID, TestData.APARTMENT_ID)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .and().body("id", Matchers.equalTo(TestData.APARTMENT_ID))
            .and().body("title", Matchers.equalTo(TestData.APARTMENT_TITLE_2));

    }


}
