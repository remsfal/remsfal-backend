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

@QuarkusTest
class TenantResourceTest extends AbstractResourceTest {
    static final String BASE_PATH = "/api/v1/projects/{projectId}/tenants";

    private static final String NEW_TENANT_JSON =
            "{ \"firstName\":\"New\", \"lastName\":\"Tenant\", " +
                    "\"email\":\"new.tenant@example.com\", \"mobilePhoneNumber\":\"+491511234567\" }";

    @Override
    @BeforeEach
    protected void setupTestProjects() {
        super.setupTestUsers();
        super.setupTestProjects();
        insertRentalAgreement(TestData.AGREEMENT_ID_1, TestData.PROJECT_ID_1);
        // Insert test tenant 1 (linked to USER_1 via email)
        insertTenant(TestData.TENANT_ID_1, TestData.AGREEMENT_ID_1,
            TestData.TENANT_FIRST_NAME_1, TestData.TENANT_LAST_NAME_1, TestData.TENANT_EMAIL_1);
    }

    @Test
    void createTenant_SUCCESS_tenantIsCreatedAndAssociated() {
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

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/{tenantId}", TestData.PROJECT_ID_1.toString(), tenantId)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    void createTenant_FAILED_DuplicateEmailInProject() {
        final String DUPLICATE_EMAIL_JSON =
                "{ \"firstName\":\"firstName\", \"lastName\":\"lastName\", \"email\":\""
                        + TestData.TENANT_EMAIL_1 + "\" }";

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(DUPLICATE_EMAIL_JSON)
                .post(BASE_PATH, TestData.PROJECT_ID_1.toString())
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void getTenant_SUCCESS_TenantCorrectlyReturned() {
        final String TENANT_ID = TestData.TENANT_ID_1.toString();

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/{tenantId}", TestData.PROJECT_ID_1.toString(), TENANT_ID)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .and().body("id", Matchers.equalTo(TENANT_ID))
                .and().body("firstName", Matchers.equalTo(TestData.TENANT_FIRST_NAME_1))
                .and().body("email", Matchers.equalTo(TestData.TENANT_EMAIL_1));
    }

    @Test
    void getTenant_FAILED_tenantNotPartOfProject() {
        final String NON_PROJECT_TENANT_ID = TestData.USER_ID_2.toString();

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/{tenantId}", TestData.PROJECT_ID_1.toString(), NON_PROJECT_TENANT_ID)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void getTenants_FAILED_NoAuthentication() {
        given()
                .when()
                .get(BASE_PATH, TestData.PROJECT_ID_1.toString())
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void updateTenant_SUCCESS_tenantIsUpdated() {
        final String TENANT_ID = TestData.TENANT_ID_1.toString();
        final String NEW_FIRST_NAME = TestData.TENANT_FIRST_NAME_4;
        final String NEW_LAST_NAME = TestData.TENANT_LAST_NAME_4;
        final String NEW_MOBILE_PHONE = "+491701112233";

        final String EMAIL_TO_KEEP = TestData.TENANT_EMAIL_1;

        final String UPDATED_TENANT_JSON =
                "{ \"firstName\":\"" + NEW_FIRST_NAME + "\", \"lastName\":\"" + NEW_LAST_NAME + "\", " +
                        "\"email\":\"" + EMAIL_TO_KEEP + "\", \"mobilePhoneNumber\": \"" + NEW_MOBILE_PHONE + "\" }";

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(UPDATED_TENANT_JSON)
                .patch(BASE_PATH + "/{tenantId}", TestData.PROJECT_ID_1.toString(), TENANT_ID)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .and().body("id", Matchers.equalTo(TENANT_ID))
                .and().body("firstName", Matchers.equalTo(NEW_FIRST_NAME))
                .and().body("lastName", Matchers.equalTo(NEW_LAST_NAME))
                .and().body("mobilePhoneNumber", Matchers.equalTo(NEW_MOBILE_PHONE));

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/{tenantId}", TestData.PROJECT_ID_1.toString(), TENANT_ID)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("firstName", Matchers.equalTo(NEW_FIRST_NAME));
    }

    @Test
    void deleteTenant_SUCCESS_tenantIsRemovedFromTenancy() {
        final String TENANT_ID = TestData.TENANT_ID_1.toString();

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .delete(BASE_PATH + "/{tenantId}", TestData.PROJECT_ID_1.toString(), TENANT_ID)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/{tenantId}", TestData.PROJECT_ID_1.toString(), TENANT_ID)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}

