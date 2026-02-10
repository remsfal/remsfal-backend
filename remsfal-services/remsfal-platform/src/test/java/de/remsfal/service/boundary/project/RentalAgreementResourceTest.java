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
import java.util.UUID;

import static io.restassured.RestAssured.given;

@QuarkusTest
class RentalAgreementResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/projects/{projectId}/rental-agreements";
    static final String AGREEMENT_PATH = BASE_PATH + "/{agreementId}";

    @BeforeEach
    protected void setupTests() {
        setupTestUsers();
        setupTestProjects();
        setupTestProperties();
        setupTestSites();
        setupTestBuildings();
        setupTestRentalAgreements();
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
    void getRentalAgreements_SUCCESS_containsAggregatedFields() {
        // Add some rents with different values
        insertApartmentRent(TestData.APARTMENT_ID, TestData.AGREEMENT_ID,
            java.time.LocalDate.parse("2021-01-01"), "MONTHLY", 500.00, 100.00, 50.00);
        insertPropertyRent(TestData.PROPERTY_ID, TestData.AGREEMENT_ID,
            java.time.LocalDate.parse("2021-01-01"), "MONTHLY", 300.00, 50.00, 25.00);

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .body("rentalAgreements.size()", Matchers.equalTo(1))
            .body("rentalAgreements[0].id", Matchers.notNullValue())
            .body("rentalAgreements[0].startOfRental", Matchers.notNullValue())
            .body("rentalAgreements[0].tenants", Matchers.notNullValue())
            .body("rentalAgreements[0].rentalUnits", Matchers.notNullValue())
            .body("rentalAgreements[0].rentalUnits.size()", Matchers.greaterThanOrEqualTo(1))
            .body("rentalAgreements[0].basicRent", Matchers.notNullValue())
            .body("rentalAgreements[0].operatingCostsPrepayment", Matchers.notNullValue())
            .body("rentalAgreements[0].heatingCostsPrepayment", Matchers.notNullValue());
    }

    @Test
    void getRentalAgreements_SUCCESS_calculatesRentSumsCorrectly() {
        // Add multiple active rents
        insertApartmentRent(TestData.APARTMENT_ID, TestData.AGREEMENT_ID,
            java.time.LocalDate.parse("2021-01-01"), "MONTHLY", 500.00, 100.00, 50.00);
        insertPropertyRent(TestData.PROPERTY_ID, TestData.AGREEMENT_ID,
            java.time.LocalDate.parse("2021-01-01"), "MONTHLY", 300.00, 50.00, 25.00);

        // Expected sums: basicRent = 800.00, operatingCosts = 150.00, heatingCosts = 75.00
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .body("rentalAgreements[0].basicRent", Matchers.equalTo(800.0f))
            .body("rentalAgreements[0].operatingCostsPrepayment", Matchers.equalTo(150.0f))
            .body("rentalAgreements[0].heatingCostsPrepayment", Matchers.equalTo(75.0f));
    }

    @Test
    void getRentalAgreements_SUCCESS_excludesInactiveRentsFromSum() {
        // Add active rent (no lastPaymentDate)
        insertApartmentRent(TestData.APARTMENT_ID, TestData.AGREEMENT_ID,
            java.time.LocalDate.parse("2021-01-01"), "MONTHLY", 500.00, 100.00, 50.00);

        // Add inactive rent (lastPaymentDate in the past)
        insertPropertyRent(TestData.PROPERTY_ID, TestData.AGREEMENT_ID,
            java.time.LocalDate.parse("2021-01-01"), "MONTHLY", 300.00, 50.00, 25.00,
            java.time.LocalDate.parse("2022-12-31"));

        // Only the active rent should be included in sums
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .body("rentalAgreements[0].basicRent", Matchers.equalTo(500.0f))
            .body("rentalAgreements[0].operatingCostsPrepayment", Matchers.equalTo(100.0f))
            .body("rentalAgreements[0].heatingCostsPrepayment", Matchers.equalTo(50.0f));
    }

    @Test
    void getRentalAgreements_SUCCESS_includesFutureRentsInSum() {
        // Add active rent with lastPaymentDate in the future
        insertApartmentRent(TestData.APARTMENT_ID, TestData.AGREEMENT_ID,
            java.time.LocalDate.parse("2021-01-01"), "MONTHLY", 500.00, 100.00, 50.00,
            java.time.LocalDate.now().plusMonths(6));

        // Should be included in sums since lastPaymentDate is in the future
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .body("rentalAgreements[0].basicRent", Matchers.equalTo(500.0f))
            .body("rentalAgreements[0].operatingCostsPrepayment", Matchers.equalTo(100.0f))
            .body("rentalAgreements[0].heatingCostsPrepayment", Matchers.equalTo(50.0f));
    }

    @Test
    void getRentalAgreements_SUCCESS_rentalUnitsListContainsCorrectUnits() {
        // Add rents for different unit types
        insertApartmentRent(TestData.APARTMENT_ID, TestData.AGREEMENT_ID,
            java.time.LocalDate.parse("2021-01-01"), "MONTHLY", 500.00, 100.00, 50.00);
        insertPropertyRent(TestData.PROPERTY_ID, TestData.AGREEMENT_ID,
            java.time.LocalDate.parse("2021-01-01"), "MONTHLY", 300.00, 50.00, 25.00);

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .body("rentalAgreements[0].rentalUnits.size()", Matchers.equalTo(2))
            .body("rentalAgreements[0].rentalUnits.id", Matchers.hasItems(
                TestData.APARTMENT_ID.toString(),
                TestData.PROPERTY_ID.toString()));
    }

    @Test
    void getRentalAgreements_SUCCESS_nullSumsWhenNoRents() {
        // No rents inserted, so sums should be null
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .body("rentalAgreements[0].basicRent", Matchers.nullValue())
            .body("rentalAgreements[0].operatingCostsPrepayment", Matchers.nullValue())
            .body("rentalAgreements[0].heatingCostsPrepayment", Matchers.nullValue())
            .body("rentalAgreements[0].rentalUnits.size()", Matchers.equalTo(0));
    }

    @Test
    void getRentalAgreements_SUCCESS_handlesPartialRentValues() {
        // Add rent with only basicRent set
        insertApartmentRent(TestData.APARTMENT_ID, TestData.AGREEMENT_ID,
            java.time.LocalDate.parse("2021-01-01"), "MONTHLY", 500.00, null, null);

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .body("rentalAgreements[0].basicRent", Matchers.equalTo(500.0f))
            .body("rentalAgreements[0].operatingCostsPrepayment", Matchers.nullValue())
            .body("rentalAgreements[0].heatingCostsPrepayment", Matchers.nullValue());
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
    void createRentalAgreement_SUCCESS_withTenantsAndUnits() {
        String json = "{" +
            "\"startOfRental\":\"2023-01-01\"," +
            "\"tenants\": [{\"firstName\":\"Max\", \"lastName\":\"Mustermann\"}]," +
            "\"apartmentRents\": [{\"unitId\":\"" + TestData.APARTMENT_ID + "\"}]" +
            "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .post(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .body("tenants.size()", Matchers.equalTo(1))
            .body("tenants[0].firstName", Matchers.equalTo("Max"))
            .body("tenants[0].lastName", Matchers.equalTo("Mustermann"));
    }

    @Test
    void createRentalAgreement_SUCCESS_tenantLinkedToExistingUser() {
        String json = "{" +
                "\"startOfRental\":\"2023-01-01\"," +
                "\"tenants\": [{\"firstName\":\"John\", \"lastName\":\"Doe\", \"email\":\"" + TestData.USER_EMAIL_1 + "\"}]," +
                "\"apartmentRents\": [{\"unitId\":\"" + TestData.APARTMENT_ID + "\"}]" +
                "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .post(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .body("tenants.size()", Matchers.equalTo(1))
            .body("tenants[0].email", Matchers.equalTo(TestData.USER_EMAIL_1));
    }

    @Test
    void createRentalAgreement_SUCCESS_tenantsWithoutEmail() {
        String json = "{" +
                "\"startOfRental\":\"2023-01-01\"," +
                "\"tenants\": [{\"firstName\":\"Jane\", \"lastName\":\"Smith\"}]," +
                "\"apartmentRents\": [{\"unitId\":\"" + TestData.APARTMENT_ID + "\"}]" +
                "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .post(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .body("tenants.size()", Matchers.equalTo(1))
            .body("tenants[0].firstName", Matchers.equalTo("Jane"))
            .body("tenants[0].email", Matchers.nullValue());
    }

    @Test
    void updateRentalAgreement_SUCCESS_replaceTenants() {
        String json = "{" +
                "\"tenants\": [{\"firstName\":\"Updated\", \"lastName\":\"Tenant\"}]" +
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
            .body("tenants.size()", Matchers.equalTo(1))
            .body("tenants[0].firstName", Matchers.equalTo("Updated"));
    }

    @Test
    void updateRentalAgreement_SUCCESS_addOptionalTenantFields() {
        String json = "{" +
                "\"tenants\": [{" +
                "\"firstName\":\"Max\"," +
                "\"lastName\":\"Mustermann\"," +
                "\"email\":\"max@example.com\"," +
                "\"mobilePhoneNumber\":\"+491234567890\"" +
                "}]" +
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
            .body("tenants[0].email", Matchers.equalTo("max@example.com"))
            .body("tenants[0].mobilePhoneNumber", Matchers.equalTo("+491234567890"));
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
            .body("{\"tenants\": [{\"firstName\":\"Keep\", \"lastName\":\"Me\"}]}")
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
    void createRentalAgreement_FAILURE_missingFirstName() {
        String json = "{" +
            "\"startOfRental\":\"2023-01-01\"," +
            "\"tenants\": [{\"lastName\":\"Doe\"}]," +
            "\"apartmentRents\": [{\"unitId\":\"" + TestData.APARTMENT_ID + "\"}]" +
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
    void createRentalAgreement_FAILURE_missingLastName() {
        String json = "{" +
            "\"startOfRental\":\"2023-01-01\"," +
            "\"tenants\": [{\"firstName\":\"John\"}]," +
            "\"apartmentRents\": [{\"unitId\":\"" + TestData.APARTMENT_ID + "\"}]" +
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
    void createRentalAgreement_FAILURE_missingTenants() {
        String json = "{" +
            "\"startOfRental\":\"2023-01-01\"," +
            "\"apartmentRents\": [{\"unitId\":\"" + TestData.APARTMENT_ID + "\"}]" +
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
    void createRentalAgreement_FAILURE_missingUnits() {
        String json = "{" +
            "\"startOfRental\":\"2023-01-01\"," +
            "\"tenants\": [{\"firstName\":\"Max\", \"lastName\":\"Mustermann\"}]" +
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
            .get(AGREEMENT_PATH, TestData.PROJECT_ID.toString(), UUID.randomUUID().toString())
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
            .patch(AGREEMENT_PATH, TestData.PROJECT_ID.toString(), UUID.randomUUID().toString())
            .then()
            .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void createRentalAgreement_FAILURE_projectNotFound() {
        String json = "{" +
            "\"startOfRental\":\"2023-01-01\"," +
            "\"tenants\": [{\"firstName\":\"Max\", \"lastName\":\"Mustermann\"}]," +
            "\"apartmentRents\": [{\"unitId\":\"" + TestData.APARTMENT_ID + "\"}]" +
            "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .post(BASE_PATH, UUID.randomUUID().toString())
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
