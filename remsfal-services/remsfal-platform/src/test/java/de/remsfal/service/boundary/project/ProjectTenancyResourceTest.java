package de.remsfal.service.boundary.project;

import de.remsfal.service.boundary.AbstractResourceTest;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static io.restassured.RestAssured.given;

@QuarkusTest
class ProjectTenancyResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/projects/{projectId}/tenancies";
    static final String TENANCY_PATH = BASE_PATH + "/{tenancyId}";

    @Override
    @BeforeEach
    protected void setupTestProperties() {
        super.setupTestUsers();
        super.setupTestProjects();
        this.setupTestTenancies();
        super.setupTestProperties();
    }

    private void setupTestTenancies() {
        insertTenancy(TestData.TENANCY_ID, TestData.PROJECT_ID, java.time.LocalDate.parse("2021-01-01"), null);
    }

    private void insertTenancy(java.util.UUID id, java.util.UUID projectId, java.time.LocalDate start,
            java.time.LocalDate end) {
        runInTransaction(() -> entityManager
            .createNativeQuery(
                "INSERT INTO tenancies (id, project_id, start_of_rental, end_of_rental) VALUES (?,?,?,?)")
            .setParameter(1, id)
            .setParameter(2, projectId)
            .setParameter(3, start)
            .setParameter(4, end)
            .executeUpdate());
    }

    @Test
    void getTenancies_SUCCESS_oneTenancyReturned() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .get(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("tenancies.size()", Matchers.equalTo(1));
    }

    @Test
    void getTenancy_SUCCESS_tenancyReturned() {
        String tenancyId = given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .get(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .extract().path("tenancies[0].id");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .get(TENANCY_PATH, TestData.PROJECT_ID.toString(), tenancyId)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.equalTo(tenancyId));
    }

    @Test
    void createTenancy_SUCCESS_newTenancyReturned() {
        String json = "{" +
            "\"startOfRental\":\"2023-01-01\"," +
            "\"endOfRental\":\"2023-12-31\"" +
            "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .post(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.CREATED.getStatusCode());
    }

    @Test
    void createTenancy_SUCCESS_withoutTenants() {
        String json = "{" +
                "\"startOfRental\":\"2023-01-01\"," +
                "\"endOfRental\":\"2023-12-31\"" +
                "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .post(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.CREATED.getStatusCode());
    }

    @Test
    void updateTenancy_SUCCESS_withTenants() {
        String json = "{" +
                "\"tenants\": [{\"id\":\"" + TestData.USER_ID_1 + "\"}]" +
                "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            //.cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .patch(TENANCY_PATH, TestData.PROJECT_ID.toString(), TestData.TENANCY_ID.toString())
            .then()
            .log().ifValidationFails()
            .statusCode(Status.OK.getStatusCode())
            .body("tenants.size()", Matchers.equalTo(1));
    }

    @Test
    void updateTenancy_SUCCESS_withDates() {
        String json = "{" +
                "\"startOfRental\":\"2023-06-01\"," +
                "\"endOfRental\":\"2023-12-31\"" +
                "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .patch(TENANCY_PATH, TestData.PROJECT_ID.toString(), TestData.TENANCY_ID.toString())
            .then()
            .log().ifValidationFails()
            .statusCode(Status.OK.getStatusCode())
            .body("startOfRental", Matchers.equalTo("2023-06-01"))
            .body("endOfRental", Matchers.equalTo("2023-12-31"));
    }

    @Test
    void updateTenancy_SUCCESS_tenantsFieldMissing() {
        String json = "{ \"startOfRental\": \"2024-01-01\" }"; // no tenants field → null

        given()
           .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
           .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
           .contentType(MediaType.APPLICATION_JSON)
           .body(json)
           .patch(TENANCY_PATH, TestData.PROJECT_ID, TestData.TENANCY_ID)
           .then()
           .statusCode(Status.OK.getStatusCode())
           .body("tenants", Matchers.notNullValue()); // stays unchanged
    }

    @Test
    void updateTenancy_SUCCESS_withoutTenants() {
        String json = "{ \"tenants\": [] }";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .patch(TENANCY_PATH, TestData.PROJECT_ID.toString(), TestData.TENANCY_ID.toString())
            .then()
            .log().ifValidationFails()
            .statusCode(Status.OK.getStatusCode())
            .body("tenants.size()", Matchers.equalTo(0));
    }

    @Test
    void updateTenancy_SUCCESS_noTenantField_keepsOldTenants() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"tenants\": [{\"id\":\"" + TestData.USER_ID_1 + "\"}]}")
            .patch(TENANCY_PATH, TestData.PROJECT_ID.toString(), TestData.TENANCY_ID.toString())
            .then()
            .statusCode(Status.OK.getStatusCode());

        String json = "{ \"startOfRental\": \"2024-01-01\" }";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .patch(TENANCY_PATH, TestData.PROJECT_ID.toString(), TestData.TENANCY_ID.toString())
            .then()
            .log().ifValidationFails()
            .statusCode(Status.OK.getStatusCode())
            .body("tenants.size()", Matchers.equalTo(1))
            .body("startOfRental", Matchers.equalTo("2024-01-01"));
    }

    @Test
    void createTenancy_FAILURE_userNotFound() {
        String json = "{" +
            "\"startOfRental\":\"2023-01-01\"," +
            "\"endOfRental\":\"2023-12-31\"," +
            "\"tenants\": [{\"id\":\"" + java.util.UUID.randomUUID() + "\"}]" +
            "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .post(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void updateTenancy_FAILURE_userNotFound() {
        String json = "{" +
            "\"tenants\": [{\"id\":\"" + java.util.UUID.randomUUID() + "\"}]" +
            "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .patch(TENANCY_PATH, TestData.PROJECT_ID.toString(), TestData.TENANCY_ID.toString())
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void createTenancy_FAILURE_missingStartOfRental() {
        String json = "{" +
            "\"endOfRental\":\"2023-12-31\"" +
            "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .post(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

}
