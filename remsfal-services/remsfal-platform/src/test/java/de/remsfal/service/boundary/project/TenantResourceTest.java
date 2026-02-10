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

    @Override
    @BeforeEach
    protected void setupTestProjects() {
        super.setupTestUsers();
        super.setupTestProjects();
        super.setupTestProperties();
        super.setupTestSites();
        super.setupTestBuildings();
        insertRentalAgreement(TestData.AGREEMENT_ID_1, TestData.PROJECT_ID_1);
        // Insert test tenant 1 (linked to USER_1 via email)
        insertTenant(TestData.TENANT_ID_1, TestData.AGREEMENT_ID_1,
            TestData.TENANT_FIRST_NAME_1, TestData.TENANT_LAST_NAME_1, TestData.TENANT_EMAIL_1);
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
    void getTenants_SUCCESS_containsAllFields() {
        // Add a rent for the tenant's agreement
        insertApartmentRent(TestData.APARTMENT_ID, TestData.AGREEMENT_ID_1,
            java.time.LocalDate.parse("2021-01-01"), "MONTHLY", 500.00, 100.00, 50.00);

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(BASE_PATH, TestData.PROJECT_ID_1.toString())
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .body("tenants.size()", Matchers.equalTo(1))
            .body("tenants[0].id", Matchers.equalTo(TestData.TENANT_ID_1.toString()))
            .body("tenants[0].firstName", Matchers.equalTo(TestData.TENANT_FIRST_NAME_1))
            .body("tenants[0].lastName", Matchers.equalTo(TestData.TENANT_LAST_NAME_1))
            .body("tenants[0].email", Matchers.equalTo(TestData.TENANT_EMAIL_1))
            .body("tenants[0].rentalUnits", Matchers.notNullValue())
            .body("tenants[0].active", Matchers.notNullValue());
    }

    @Test
    void getTenants_SUCCESS_tenantIsActive() {
        // Add a rent for an active agreement (no end date)
        insertApartmentRent(TestData.APARTMENT_ID, TestData.AGREEMENT_ID_1,
            java.time.LocalDate.parse("2021-01-01"), "MONTHLY", 500.00, 100.00, 50.00);

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(BASE_PATH, TestData.PROJECT_ID_1.toString())
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("tenants[0].active", Matchers.equalTo(true));
    }

    @Test
    void getTenants_SUCCESS_tenantIsInactive() {
        // Update the agreement to have an end date in the past
        runInTransaction(() -> entityManager
            .createNativeQuery("UPDATE rental_agreements SET end_of_rental = ? WHERE id = ?")
            .setParameter(1, java.time.LocalDate.parse("2020-12-31"))
            .setParameter(2, TestData.AGREEMENT_ID_1)
            .executeUpdate());

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(BASE_PATH, TestData.PROJECT_ID_1.toString())
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("tenants[0].active", Matchers.equalTo(false));
    }

    @Test
    void getTenants_SUCCESS_tenantIsActiveWithFutureEndDate() {
        // Update the agreement to have an end date in the future
        runInTransaction(() -> entityManager
            .createNativeQuery("UPDATE rental_agreements SET end_of_rental = ? WHERE id = ?")
            .setParameter(1, java.time.LocalDate.now().plusMonths(6))
            .setParameter(2, TestData.AGREEMENT_ID_1)
            .executeUpdate());

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(BASE_PATH, TestData.PROJECT_ID_1.toString())
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("tenants[0].active", Matchers.equalTo(true));
    }

    @Test
    void getTenants_SUCCESS_rentalUnitsListContainsRentedUnits() {
        // Add multiple rents for different unit types
        insertApartmentRent(TestData.APARTMENT_ID, TestData.AGREEMENT_ID_1,
            java.time.LocalDate.parse("2021-01-01"), "MONTHLY", 500.00, 100.00, 50.00);
        insertPropertyRent(TestData.PROPERTY_ID, TestData.AGREEMENT_ID_1,
            java.time.LocalDate.parse("2021-01-01"), "MONTHLY", 300.00, 50.00, 25.00);

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(BASE_PATH, TestData.PROJECT_ID_1.toString())
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("tenants[0].rentalUnits.size()", Matchers.equalTo(2))
            .body("tenants[0].rentalUnits.id", Matchers.hasItems(
                TestData.APARTMENT_ID.toString(),
                TestData.PROPERTY_ID.toString()));
    }

    @Test
    void getTenants_SUCCESS_rentalUnitsListIncludesHistoricalUnits() {
        // Create a first agreement with apartment rent
        insertApartmentRent(TestData.APARTMENT_ID, TestData.AGREEMENT_ID_1,
            java.time.LocalDate.parse("2020-01-01"), "MONTHLY", 500.00, 100.00, 50.00);

        // Create a second agreement for the same tenant with a different unit
        insertRentalAgreement(TestData.AGREEMENT_ID_2, TestData.PROJECT_ID_1);
        insertTenant(TestData.TENANT_ID_2, TestData.AGREEMENT_ID_2,
            TestData.TENANT_FIRST_NAME_1, TestData.TENANT_LAST_NAME_1, TestData.TENANT_EMAIL_1);
        insertPropertyRent(TestData.PROPERTY_ID, TestData.AGREEMENT_ID_2,
            java.time.LocalDate.parse("2021-01-01"), "MONTHLY", 600.00, 120.00, 60.00);

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(BASE_PATH, TestData.PROJECT_ID_1.toString())
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("tenants.size()", Matchers.equalTo(2))
            // Both tenants should have units listed (even though they're the same person with same name/email)
            .body("tenants[0].rentalUnits.size()", Matchers.greaterThanOrEqualTo(1))
            .body("tenants[1].rentalUnits.size()", Matchers.greaterThanOrEqualTo(1));
    }

    @Test
    void getTenants_SUCCESS_emptyRentalUnitsWhenNoRents() {
        // No rents inserted for this agreement
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(BASE_PATH, TestData.PROJECT_ID_1.toString())
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("tenants[0].rentalUnits.size()", Matchers.equalTo(0))
            .body("tenants[0].active", Matchers.equalTo(true)); // Still active (no end date)
    }

    @Test
    void getTenants_SUCCESS_multipleTenantsInSameAgreement() {
        // Add a second tenant to the same agreement
        insertTenant(TestData.TENANT_ID_2, TestData.AGREEMENT_ID_1,
            TestData.TENANT_FIRST_NAME_2, TestData.TENANT_LAST_NAME_2, TestData.TENANT_EMAIL_2);

        // Add a rent
        insertApartmentRent(TestData.APARTMENT_ID, TestData.AGREEMENT_ID_1,
            java.time.LocalDate.parse("2021-01-01"), "MONTHLY", 500.00, 100.00, 50.00);

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(BASE_PATH, TestData.PROJECT_ID_1.toString())
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("tenants.size()", Matchers.equalTo(2))
            // Both tenants should have the same rental unit
            .body("tenants[0].rentalUnits.id", Matchers.hasItem(TestData.APARTMENT_ID.toString()))
            .body("tenants[1].rentalUnits.id", Matchers.hasItem(TestData.APARTMENT_ID.toString()))
            // Both should have the same active status
            .body("tenants[0].active", Matchers.equalTo(true))
            .body("tenants[1].active", Matchers.equalTo(true));
    }

    @Test
    void getTenants_SUCCESS_phoneNumbersAreIncluded() {
        // Update tenant with phone numbers
        runInTransaction(() -> entityManager
            .createNativeQuery("UPDATE tenants SET mobile_phone_number = ?, business_phone_number = ?, private_phone_number = ? WHERE id = ?")
            .setParameter(1, "+491701112233")
            .setParameter(2, "+493012345678")
            .setParameter(3, "+491709876543")
            .setParameter(4, TestData.TENANT_ID_1)
            .executeUpdate());

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(BASE_PATH, TestData.PROJECT_ID_1.toString())
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("tenants[0].mobilePhoneNumber", Matchers.equalTo("+491701112233"))
            .body("tenants[0].businessPhoneNumber", Matchers.equalTo("+493012345678"))
            .body("tenants[0].privatePhoneNumber", Matchers.equalTo("+491709876543"));
    }

    @Test
    void getTenants_SUCCESS_noDuplicateUnitsInList() {
        // Add the same unit twice in different rent entries (edge case)
        insertApartmentRent(TestData.APARTMENT_ID, TestData.AGREEMENT_ID_1,
            java.time.LocalDate.parse("2020-01-01"), "MONTHLY", 500.00, 100.00, 50.00,
            java.time.LocalDate.parse("2020-12-31")); // Ended rent

        insertApartmentRent(TestData.APARTMENT_ID, TestData.AGREEMENT_ID_1,
            java.time.LocalDate.parse("2021-01-01"), "MONTHLY", 550.00, 110.00, 55.00); // New rent for same unit

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(BASE_PATH, TestData.PROJECT_ID_1.toString())
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("tenants[0].rentalUnits.size()", Matchers.equalTo(1)) // Should only appear once
            .body("tenants[0].rentalUnits[0].id", Matchers.equalTo(TestData.APARTMENT_ID.toString()));
    }
}

