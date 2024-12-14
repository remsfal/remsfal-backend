package de.remsfal.service.boundary.project;

import de.remsfal.service.TestData;
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

@QuarkusTest
public class ApartmentResourceTest extends AbstractProjectResourceTest {

    static final String BASE_PATH = "/api/v1/projects/{projectId}/properties/{propertyId}/" +
            "buildings/{buildingId}/apartments";

    @Override
    @BeforeEach
    protected void setupTestProjects() {
        super.setupTestUsers();
        super.setupTestProjects();
        super.setupTestProperties();
    }

    protected void setupTestApartments() {
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
                .setParameter(3, TestData. PROJECT_ID)
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
                .get(BASE_PATH+"/{apartmentId}", TestData.PROJECT_ID, TestData.PROPERTY_ID,
                        TestData.BUILDING_ID, TestData.APARTMENT_ID)
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getApartment_SUCCESS_ApartmentCorrectlyReturned() {
        setupTestApartments();
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .get(BASE_PATH+"/{apartmentId}", TestData.PROJECT_ID, TestData.PROPERTY_ID,
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

//    @ParameterizedTest
//    @ValueSource(strings = "")
//    void createApartment_SUCCESS_propertyIsCreated() {
//        setupTestApartments();
//        given()
//                .when()
//                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
//                .post
//    }



}
