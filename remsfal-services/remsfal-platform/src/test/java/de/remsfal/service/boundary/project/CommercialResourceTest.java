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

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class CommercialResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/projects/{projectId}/properties/{propertyId}/" +
        "buildings/{buildingId}/commercials";

    @Override
    @BeforeEach
    protected void setupTestProjects() {
        super.setupTestUsers();
        super.setupTestProjects();
        super.setupTestProperties();
    }

    @Test
    void getCommercial_FAILED_noAuthentication() {
        given()
            .when()
            .get(BASE_PATH + "/{commercialId}", TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID,
                TestData.BUILDING_ID, TestData.COMMERCIAL_ID)
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getCommercialSuccessfully() {
        setupTestBuildings();
        given()
            .when()
            .cookie(buildManagerCookie())
            .get(BASE_PATH + "/{commercialId}", TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID,
                TestData.BUILDING_ID, TestData.COMMERCIAL_ID)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .and().body("id", Matchers.equalTo(TestData.COMMERCIAL_ID_1.toString()))
            .and().body("title", Matchers.equalTo(TestData.COMMERCIAL_TITLE_1))
            .and().body("description", Matchers.equalTo(TestData.COMMERCIAL_DESCRIPTION_1))
            .and().body("netFloorArea", Matchers.equalTo(TestData.COMMERCIAL_NET_FLOOR_AREA_1))
            .and().body("heatingSpace", Matchers.equalTo(TestData.COMMERCIAL_HEATING_SPACE_1))
            .and().body("location", Matchers.equalTo(TestData.COMMERCIAL_LOCATION_1));
    }

    @ParameterizedTest
    @ValueSource(strings = "{ \"title\":\"" + TestData.COMMERCIAL_TITLE_2 + "\"}")
    void createCommercialSuccessfully(String json) {
        setupTestBuildings();
        given()
            .when()
            .cookie(buildManagerCookie())
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
            .and().body("title", Matchers.equalTo(TestData.COMMERCIAL_TITLE_2));

        long entities = entityManager
            .createQuery("SELECT count(commercial) FROM CommercialEntity commercial where commercial.buildingId = :buildingId",
                long.class)
            .setParameter("buildingId", TestData.BUILDING_ID_2)
            .getSingleResult();
        assertEquals(1, entities);
    }

    @ParameterizedTest
    @ValueSource(strings = "{ \"title\":\"" + TestData.COMMERCIAL_TITLE_2 + "\"}")
    void updateCommercialSuccessfully(final String json) {
        setupTestBuildings();

        given()
            .when()
            .cookie(buildManagerCookie())
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .patch(BASE_PATH + "/{commercialId}", TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID,
                TestData.BUILDING_ID, TestData.COMMERCIAL_ID)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .and().body("id", Matchers.equalTo(TestData.COMMERCIAL_ID.toString()))
            .and().body("title", Matchers.equalTo(TestData.COMMERCIAL_TITLE_2))
            .and().body("description", Matchers.equalTo(TestData.COMMERCIAL_DESCRIPTION_1))
            .and().body("netFloorArea", Matchers.equalTo(TestData.COMMERCIAL_NET_FLOOR_AREA_1))
            .and().body("heatingSpace", Matchers.equalTo(TestData.COMMERCIAL_HEATING_SPACE_1))
            .and().body("location", Matchers.equalTo(TestData.COMMERCIAL_LOCATION_1));

        given()
            .when()
            .cookie(buildManagerCookie())
            .contentType(MediaType.APPLICATION_JSON)
            .get(BASE_PATH + "/{commercialId}", TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID,
                TestData.BUILDING_ID, TestData.COMMERCIAL_ID)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .and().body("id", Matchers.equalTo(TestData.COMMERCIAL_ID.toString()))
            .and().body("title", Matchers.equalTo(TestData.COMMERCIAL_TITLE_2));
    }

    @Test
    void deleteCommercialSuccessfully() {
        setupTestBuildings();

        given()
            .when()
            .cookie(buildManagerCookie())
            .delete(BASE_PATH + "/{commercialId}", TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID,
                TestData.BUILDING_ID, TestData.COMMERCIAL_ID)
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        given()
            .when()
            .cookie(buildManagerCookie())
            .get(BASE_PATH + "/{commercialId}", TestData.PROJECT_ID.toString(), TestData.PROPERTY_ID,
                TestData.BUILDING_ID, TestData.COMMERCIAL_ID)
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}
