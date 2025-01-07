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
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class CommercialResourceTest extends AbstractProjectResourceTest {

    static final String BASE_PATH = "/api/v1/projects/{projectId}/properties/{propertyId}/" +
            "buildings/{buildingId}/commercials";

    @Override
    @BeforeEach
    protected void setupTestProjects() {
        super.setupTestUsers();
        super.setupTestProjects();
        super.setupTestProperties();
    }

    protected void setupTestCommercial() {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO BUILDING (ID, PROPERTY_ID, PROJECT_ID, ADDRESS_ID, TITLE)" +
                        " VALUES (?,?,?,?,?)")
                .setParameter(1, TestData.BUILDING_ID)
                .setParameter(2, TestData.PROPERTY_ID)
                .setParameter(3, TestData.PROJECT_ID)
                .setParameter(4, TestData.ADDRESS_ID)
                .setParameter(5, TestData.COMMERCIAL_TITLE)
                .executeUpdate());
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO COMMERCIAL (ID, BUILDING_ID, PROJECT_ID, " +
                        "LOCATION, COMMERCIAL_SPACE,HEATING_SPACE, TITLE, DESCRIPTION, USABLE_SPACE) VALUES (?,?,?,?,?,?,?,?,?)")
                .setParameter(1, TestData.COMMERCIAL_ID)
                .setParameter(2, TestData.BUILDING_ID)
                .setParameter(3, TestData.PROJECT_ID)
                .setParameter(4, TestData.COMMERCIAL_LOCATION)
                .setParameter(5, TestData.COMMERCIAL_COMMERCIAL_SPACE)
                .setParameter(6, TestData.COMMERCIAL_HEATING_SPACE)
                .setParameter(7, TestData.COMMERCIAL_TITLE)
                .setParameter(8, TestData.COMMERCIAL_DESCRIPTION)
                .setParameter(9, TestData.COMMERCIAL_USABLE_SPACE)
                .executeUpdate());
    }

    @Test
    void getCommercial_FAILED_noAuthentication() {
        given()
                .when()
                .get(BASE_PATH + "/{commercialId}", TestData.PROJECT_ID, TestData.PROPERTY_ID,
                        TestData.BUILDING_ID, TestData.COMMERCIAL_ID)
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getCommercialSuccessfully() {
        setupTestCommercial();
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/{commercialId}", TestData.PROJECT_ID, TestData.PROPERTY_ID,
                        TestData.BUILDING_ID, TestData.COMMERCIAL_ID)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .and().body("id", Matchers.equalTo(TestData.COMMERCIAL_ID))
                .and().body("title", Matchers.equalTo(TestData.COMMERCIAL_TITLE))
                .and().body("description", Matchers.equalTo(TestData.COMMERCIAL_DESCRIPTION))
                .and().body("commercialSpace", Matchers.equalTo(TestData.COMMERCIAL_COMMERCIAL_SPACE))
                .and().body("usableSpace", Matchers.equalTo(TestData.COMMERCIAL_USABLE_SPACE))
                .and().body("heatingSpace", Matchers.equalTo(TestData.COMMERCIAL_HEATING_SPACE))
                .and().body("location", Matchers.equalTo(TestData.COMMERCIAL_LOCATION));
    }

    @ParameterizedTest
    @ValueSource(strings = "{ \"title\":\"" + TestData.COMMERCIAL_TITLE_2 + "\"}")
    void createCommercialSuccessfully(String json) {
        setupTestCommercial();
        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
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
                .and().body("title", Matchers.equalTo(TestData.COMMERCIAL_TITLE_2));

        long entities = entityManager
                .createQuery("SELECT count(commercial) FROM CommercialEntity commercial where commercial.title = :title", long.class)
                .setParameter("title", TestData.COMMERCIAL_TITLE_2)
                .getSingleResult();
        assertEquals(1, entities);
    }

    @ParameterizedTest
    @ValueSource(strings = "{ \"title\":\"" + TestData.COMMERCIAL_TITLE_2 + "\"}")
    void updateCommercialSuccessfully(final String json) {
        setupTestCommercial();

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .patch(BASE_PATH + "/{commercialId}", TestData.PROJECT_ID, TestData.PROPERTY_ID,
                        TestData.BUILDING_ID, TestData.COMMERCIAL_ID)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .and().body("id", Matchers.equalTo(TestData.COMMERCIAL_ID))
                .and().body("title", Matchers.equalTo(TestData.COMMERCIAL_TITLE_2))
                .and().body("description", Matchers.equalTo(TestData.COMMERCIAL_DESCRIPTION))
                .and().body("commercialSpace", Matchers.equalTo(TestData.COMMERCIAL_COMMERCIAL_SPACE))
                .and().body("heatingSpace", Matchers.equalTo(TestData.COMMERCIAL_HEATING_SPACE))
                .and().body("location", Matchers.equalTo(TestData.COMMERCIAL_LOCATION))
                .and().body("usableSpace", Matchers.equalTo(TestData.COMMERCIAL_USABLE_SPACE));

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .get(BASE_PATH + "/{commercialId}", TestData.PROJECT_ID, TestData.PROPERTY_ID,
                        TestData.BUILDING_ID, TestData.COMMERCIAL_ID)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .and().body("id", Matchers.equalTo(TestData.COMMERCIAL_ID))
                .and().body("title", Matchers.equalTo(TestData.COMMERCIAL_TITLE_2));
    }

    @Test
    void deleteCommercialSuccessfully() {
        setupTestCommercial();

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .delete(BASE_PATH + "/{commercialId}", TestData.PROJECT_ID, TestData.PROPERTY_ID,
                        TestData.BUILDING_ID, TestData.COMMERCIAL_ID)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        given()
                .when()
                .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/{commercialId}", TestData.PROJECT_ID, TestData.PROPERTY_ID,
                        TestData.BUILDING_ID, TestData.COMMERCIAL_ID)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}
