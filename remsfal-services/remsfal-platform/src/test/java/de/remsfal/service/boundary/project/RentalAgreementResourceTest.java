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
class RentalAgreementResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/projects/{projectId}/rental-agreements";
    static final String AGREEMENT_PATH = BASE_PATH + "/{agreementId}";

    @Override
    @BeforeEach
    protected void setupTestProperties() {
        super.setupTestUsers();
        super.setupTestProjects();
        super.setupTestProperties();
        this.setupTestRentalAgreements();
    }

    private void setupTestRentalAgreements() {
        insertRentalAgreement(TestData.AGREEMENT_ID, TestData.PROJECT_ID);
    }

    @Test
    void getRentalAgreements_SUCCESS_oneRentalAgreementReturned() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("rentalAgreements.size()", Matchers.equalTo(1));
    }

    @Test
    void getRentalAgreement_SUCCESS_agreementReturned() {
        String agreementId = given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .extract().path("rentalAgreements[0].id");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(AGREEMENT_PATH, TestData.PROJECT_ID.toString(), agreementId)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.equalTo(agreementId));
    }

    @Test
    void createRentalAgreement_SUCCESS_newAgreementReturned() {
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
    void createRentalAgreement_SUCCESS_withoutTenants() {
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
    void updateRentalAgreement_SUCCESS_withTenants() {
        String json = "{" +
                "\"tenants\": [{\"id\":\"" + TestData.USER_ID_1 + "\"}]" +
                "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .patch(AGREEMENT_PATH, TestData.PROJECT_ID.toString(), TestData.AGREEMENT_ID.toString())
            .then()
            .log().ifValidationFails()
            .statusCode(Status.OK.getStatusCode())
            .body("tenants.size()", Matchers.equalTo(1));
    }

    @Test
    void updateRentalAgreement_SUCCESS_withDates() {
        String json = "{" +
                "\"startOfRental\":\"2023-06-01\"," +
                "\"endOfRental\":\"2023-12-31\"" +
                "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .patch(AGREEMENT_PATH, TestData.PROJECT_ID.toString(), TestData.AGREEMENT_ID.toString())
            .then()
            .log().ifValidationFails()
            .statusCode(Status.OK.getStatusCode())
            .body("startOfRental", Matchers.equalTo("2023-06-01"))
            .body("endOfRental", Matchers.equalTo("2023-12-31"));
    }

    @Test
    void updateRentalAgreement_SUCCESS_tenantsFieldMissing() {
        String json = "{ \"startOfRental\": \"2024-01-01\" }"; // no tenants field → null

        given()
           .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
           .contentType(MediaType.APPLICATION_JSON)
           .body(json)
           .patch(AGREEMENT_PATH, TestData.PROJECT_ID, TestData.AGREEMENT_ID)
           .then()
           .statusCode(Status.OK.getStatusCode())
           .body("tenants", Matchers.notNullValue()); // stays unchanged
    }

    @Test
    void updateRentalAgreement_SUCCESS_withoutTenants() {
        String json = "{ \"tenants\": [] }";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .patch(AGREEMENT_PATH, TestData.PROJECT_ID.toString(), TestData.AGREEMENT_ID.toString())
            .then()
            .log().ifValidationFails()
            .statusCode(Status.OK.getStatusCode())
            .body("tenants.size()", Matchers.equalTo(0));
    }

    @Test
    void updateRentalAgreement_SUCCESS_noTenantField_keepsOldTenants() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"tenants\": [{\"id\":\"" + TestData.USER_ID_1 + "\"}]}")
            .patch(AGREEMENT_PATH, TestData.PROJECT_ID.toString(), TestData.AGREEMENT_ID.toString())
            .then()
            .statusCode(Status.OK.getStatusCode());

        String json = "{ \"startOfRental\": \"2024-01-01\" }";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .patch(AGREEMENT_PATH, TestData.PROJECT_ID.toString(), TestData.AGREEMENT_ID.toString())
            .then()
            .log().ifValidationFails()
            .statusCode(Status.OK.getStatusCode())
            .body("tenants.size()", Matchers.equalTo(1))
            .body("startOfRental", Matchers.equalTo("2024-01-01"));
    }

    @Test
    void createRentalAgreement_FAILURE_userNotFound() {
        String json = "{" +
            "\"startOfRental\":\"2023-01-01\"," +
            "\"endOfRental\":\"2023-12-31\"," +
            "\"tenants\": [{\"id\":\"" + java.util.UUID.randomUUID() + "\"}]" +
            "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .post(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void updateRentalAgreement_FAILURE_userNotFound() {
        String json = "{" +
            "\"tenants\": [{\"id\":\"" + java.util.UUID.randomUUID() + "\"}]" +
            "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .patch(AGREEMENT_PATH, TestData.PROJECT_ID.toString(), TestData.AGREEMENT_ID.toString())
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void createRentalAgreement_FAILURE_missingStartOfRental() {
        String json = "{" +
            "\"endOfRental\":\"2023-12-31\"" +
            "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .post(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }


    @Test
    void getRentalAgreement_FAILURE_tenancyNotFound() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(AGREEMENT_PATH, TestData.PROJECT_ID.toString(), java.util.UUID.randomUUID().toString())
            .then()
            .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void updateRentalAgreement_FAILURE_tenancyNotFound() {
        String json = "{ \"startOfRental\": \"2024-01-01\" }";
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .patch(AGREEMENT_PATH, TestData.PROJECT_ID.toString(), java.util.UUID.randomUUID().toString())
            .then()
            .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void createRentalAgreement_FAILURE_projectNotFound() {
        String json = "{" +
            "\"startOfRental\":\"2023-01-01\"," +
            "\"endOfRental\":\"2023-12-31\"" +
            "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .post(BASE_PATH, java.util.UUID.randomUUID().toString())
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void getRentalAgreements_FAILURE_unauthorized() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .get(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void getRentalAgreement_FAILURE_unauthorized() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .get(AGREEMENT_PATH, TestData.PROJECT_ID.toString(), TestData.AGREEMENT_ID.toString())
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

}
