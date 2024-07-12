package de.remsfal.service.boundary.project;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.core.json.project.SiteListJson;
import de.remsfal.service.TestData;
import de.remsfal.service.entity.dto.SiteEntity;
import static io.restassured.RestAssured.given;

import java.time.Duration;
import java.util.List;

import jakarta.ws.rs.core.Response.Status;

@QuarkusTest
class SiteResourceTest extends AbstractProjectResourceTest {

    static final String BASE_PATH = "/api/v1/projects/{projectId}/properties/{propertyId}/sites";

    @Override
    @BeforeEach
    protected void setupTestProjects() {
        super.setupTestProjects();
    }

    @Test
    void getSite_FAILED_notImplemented() {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROPERTY (ID, PROJECT_ID, TITLE, LAND_REGISTER_ENTRY, DESCRIPTION, PLOT_AREA) VALUES (?,?,?,?,?,?)")
            .setParameter(1, TestData.PROPERTY_ID_1)
            .setParameter(2, TestData.PROJECT_ID)
            .setParameter(3, TestData.PROPERTY_TITLE_1)
            .setParameter(4, TestData.PROPERTY_REG_ENTRY_1)
            .setParameter(5, TestData.PROPERTY_DESCRIPTION_1)
            .setParameter(6, TestData.PROPERTY_PLOT_AREA_1)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROPERTY (ID, PROJECT_ID, TITLE, LAND_REGISTER_ENTRY, DESCRIPTION, PLOT_AREA) VALUES (?,?,?,?,?,?)")
            .setParameter(1, TestData.PROPERTY_ID_2)
            .setParameter(2, TestData.PROJECT_ID)
            .setParameter(3, TestData.PROPERTY_TITLE_2)
            .setParameter(4, TestData.PROPERTY_REG_ENTRY_2)
            .setParameter(5, TestData.PROPERTY_DESCRIPTION_2)
            .setParameter(6, TestData.PROPERTY_PLOT_AREA_2)
            .executeUpdate());

        SiteEntity entity = new SiteEntity();
        entity.generateId();
        entity.setTitle(TestData.SITE_TITLE);
        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .body(SiteListJson.valueOf(List.of(entity)))
            .post(BASE_PATH, TestData.PROJECT_ID, TestData.PROPERTY_ID_1)
            .then()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

}