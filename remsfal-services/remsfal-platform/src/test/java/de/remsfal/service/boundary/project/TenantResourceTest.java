package de.remsfal.service.boundary.project;

import de.remsfal.service.boundary.AbstractResourceTest;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.wildfly.common.Assert.assertNotNull;

@QuarkusTest
public class TenantResourceTest extends AbstractResourceTest {
    static final String BASE_PATH = "/api/v1/projects/{projectId}/tenants";

    private static final String NEW_TENANT_JSON =
            "{ \"firstName\":\"New\", \"lastName\":\"Tenant\", " +
                    "\"email\":\"new.tenant@example.com\", \"mobilePhoneNumber\":\"+491511234567\" }";

    // private static final String EXISTING_USER_AS_TENANT_ID = TestData.USER_ID_4.toString();

    @Override
    @BeforeEach
    protected void setupTestProjects() {
        super.setupTestUsers();
        super.setupTestProjects();
        insertTestTenancy(TestData.TENANCY_ID_1, TestData.PROJECT_ID_1);
        insertTenant(TestData.TENANCY_ID_1, TestData.USER_ID_3);
    }

    @Test
    void createTenant_SUCCESS_tenantIsCreatedAndAssociated() {
        // ACT & ASSERT: Sende Request zum Erstellen des Mieters
        String tenantId = given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(NEW_TENANT_JSON)
                .post(BASE_PATH, TestData.PROJECT_ID_1.toString())
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .header("location", Matchers.containsString(BASE_PATH.replace("{projectId}", TestData.PROJECT_ID_1.toString()) + "/"))
                .and().body("id", Matchers.notNullValue())
                .and().body("firstName", Matchers.equalTo("New"))
                .and().body("email", Matchers.equalTo("new.tenant@example.com"))
                .extract().path("id");

        // Optional: Zusätzlicher GET-Check zur Verifizierung der Persistenz
        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/{tenantId}", TestData.PROJECT_ID_1.toString(), tenantId)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        assertNotNull(tenantId);
    }

    @Test
    void createTenant_FAILED_DuplicateEmailInProject() {
        // Der Benutzer TestData.USER_EMAIL_3 ist im Setup bereits als Mieter verknüpft.
        final String DUPLICATE_EMAIL_JSON =
                "{ \"firstName\":\"X\", \"lastName\":\"Y\", \"email\":\"" + TestData.USER_EMAIL_3 + "\" }";

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(DUPLICATE_EMAIL_JSON)
                .post(BASE_PATH, TestData.PROJECT_ID_1.toString())
                .then()
                // Erwartet: 400 Bad Request aufgrund der Duplikatsprüfung in der createTenant Logik
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body(Matchers.containsString("A tenant with this email already exists in the project"));
    }

    @Test
    void getTenant_SUCCESS_TenantCorrectlyReturned() {
        final String TENANT_ID = TestData.USER_ID_3.toString();

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/{tenantId}", TestData.PROJECT_ID_1.toString(), TENANT_ID)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .and().body("id", Matchers.equalTo(TENANT_ID))
                .and().body("firstName", Matchers.equalTo(TestData.USER_FIRST_NAME_3))
                .and().body("email", Matchers.equalTo(TestData.USER_EMAIL_3));
    }

    @Test
    void getTenant_FAILED_tenantNotPartOfProject() {
        // TestData.USER_ID_2 ist nicht mit PROJECT_ID_1 verknüpft
        final String NON_PROJECT_TENANT_ID = TestData.USER_ID_2.toString();

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/{tenantId}", TestData.PROJECT_ID_1.toString(), NON_PROJECT_TENANT_ID)
                .then()
                // Erwartet: 404, da die findTenantByProjectId fehlschlagen sollte
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void deleteTenant_SUCCESS_tenantIsRemovedFromTenancy() {
        // TestData.USER_ID_3 wurde im Setup mit Tenancy verknüpft.
        final String TENANT_ID = TestData.USER_ID_3.toString();

        // 1. ACT: Lösche die Verknüpfung
        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .delete(BASE_PATH + "/{tenantId}", TestData.PROJECT_ID_1.toString(), TENANT_ID)
                .then()
                // Erwartet: 204 No Content
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        // 2. ASSERT: Prüfe, ob der Mieter nicht mehr abrufbar ist
        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/{tenantId}", TestData.PROJECT_ID_1.toString(), TENANT_ID)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }


}

