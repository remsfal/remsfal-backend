package de.remsfal.service.boundary.project;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.core.json.project.BuildingListJson;
import de.remsfal.service.TestData;
import de.remsfal.service.entity.dto.BuildingEntity;

import static io.restassured.RestAssured.given;

import java.time.Duration;
import java.util.List;

import jakarta.ws.rs.core.Response.Status;

@QuarkusTest
class BuildingResourceTest extends AbstractProjectResourceTest {

    static final String BASE_PATH = "/api/v1/projects/{projectId}/properties/{propertyId}/buildings";

    @Override
    @BeforeEach
    protected void setupTestProperties() {
        super.setupTestUsers();
        super.setupTestProjects();
        super.setupTestProperties();
    }

    @Test
    void getBuilding_FAILED_notImplemented() {
        BuildingEntity entity = new BuildingEntity();
        entity.generateId();
        entity.setTitle(TestData.BUILDING_TITLE);
        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .body(BuildingListJson.valueOf(List.of(entity)))
            .post(BASE_PATH, TestData.PROJECT_ID, TestData.PROPERTY_ID_1)
            .then()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

}