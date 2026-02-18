package de.remsfal.service.boundary.tenancy;

import de.remsfal.service.boundary.AbstractResourceTest;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static io.restassured.RestAssured.given;

@QuarkusTest
class TenancyResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/tenancies";

    @BeforeEach
    protected void setupTests() {
        super.setupAllTestData();
        this.setupTestRentalAgreements();
    }

    protected void setupTestRentalAgreements() {
        insertRentalAgreement(TestData.AGREEMENT_ID_1, TestData.PROJECT_ID,
            TestData.AGREEMENT_START_1, TestData.AGREEMENT_END_1);
        insertTenant(TestData.TENANT_ID_1, TestData.AGREEMENT_ID_1, TestData.PROJECT_ID, TestData.USER_ID_3,
            TestData.USER_FIRST_NAME_3, TestData.USER_LAST_NAME_3, TestData.USER_EMAIL_3);
        insertApartmentRent(TestData.AGREEMENT_ID_1, TestData.APARTMENT_ID,
            TestData.AGREEMENT_START_1, TestData.AGREEMENT_END_1, 670f, 75f, 120f);
        insertStorageRent(TestData.AGREEMENT_ID_1, TestData.STORAGE_ID,
            TestData.AGREEMENT_START_1, TestData.AGREEMENT_END_1, 30f, null, null);
        insertCommercialRent(TestData.AGREEMENT_ID_1, TestData.COMMERCIAL_ID,
            TestData.AGREEMENT_START_1, TestData.AGREEMENT_END_1, 1030f, 230f, 320f);
        insertRentalAgreement(TestData.AGREEMENT_ID_2, TestData.PROJECT_ID,
            TestData.AGREEMENT_START_2, TestData.AGREEMENT_END_2);
        insertTenant(TestData.TENANT_ID_2, TestData.AGREEMENT_ID_2, TestData.PROJECT_ID, TestData.USER_ID_4,
            TestData.USER_FIRST_NAME_4, TestData.USER_LAST_NAME_4, TestData.USER_EMAIL_4);
        insertSiteRent(TestData.AGREEMENT_ID_2, TestData.SITE_ID_1,
            TestData.AGREEMENT_START_2, TestData.AGREEMENT_END_2, 40f, null, null);
        insertBuildingRent(TestData.AGREEMENT_ID_2, TestData.BUILDING_ID_2,
            TestData.AGREEMENT_START_2, TestData.AGREEMENT_END_2, 899f, 150f, 240f);
        insertRentalAgreement(TestData.AGREEMENT_ID_3, TestData.PROJECT_ID,
            TestData.AGREEMENT_START_3, TestData.AGREEMENT_END_3);
        insertTenant(TestData.TENANT_ID_3, TestData.AGREEMENT_ID_3, TestData.PROJECT_ID, TestData.USER_ID_3,
            TestData.USER_FIRST_NAME_3, TestData.USER_LAST_NAME_3, TestData.USER_EMAIL_3);
        insertTenant(TestData.TENANT_ID_4, TestData.AGREEMENT_ID_3, TestData.PROJECT_ID, TestData.USER_ID_4,
            TestData.USER_FIRST_NAME_4, TestData.USER_LAST_NAME_4, TestData.USER_EMAIL_4);
        insertPropertyRent(TestData.AGREEMENT_ID_3, TestData.PROPERTY_ID_2,
            TestData.AGREEMENT_START_3, TestData.AGREEMENT_END_3, 100f, null, null);
    }

    protected void insertRentalAgreement(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO rental_agreements (id, project_id, start_of_rental, end_of_rental) VALUES (?,?,?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .setParameter(3, params[2])
                .setParameter(4, params[3])
                .executeUpdate());
    }

    protected void insertPropertyRent(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO property_rents (agreement_id, property_id, first_payment, last_payment,"
                    + "BASIC_RENT, OPERATING_COSTS_PREPAYMENT, HEATING_COSTS_PREPAYMENT) VALUES (?,?,?,?,?,?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .setParameter(3, params[2])
                .setParameter(4, params[3])
                .setParameter(5, params[4])
                .setParameter(6, params[5])
                .setParameter(7, params[6])
                .executeUpdate());
    }

    protected void insertSiteRent(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO site_rents (agreement_id, site_id, first_payment, last_payment,"
                    + "BASIC_RENT, OPERATING_COSTS_PREPAYMENT, HEATING_COSTS_PREPAYMENT) VALUES (?,?,?,?,?,?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .setParameter(3, params[2])
                .setParameter(4, params[3])
                .setParameter(5, params[4])
                .setParameter(6, params[5])
                .setParameter(7, params[6])
                .executeUpdate());
    }

    protected void insertBuildingRent(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO building_rents (agreement_id, building_id, first_payment, last_payment,"
                    + "BASIC_RENT, OPERATING_COSTS_PREPAYMENT, HEATING_COSTS_PREPAYMENT) VALUES (?,?,?,?,?,?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .setParameter(3, params[2])
                .setParameter(4, params[3])
                .setParameter(5, params[4])
                .setParameter(6, params[5])
                .setParameter(7, params[6])
                .executeUpdate());
    }

    protected void insertApartmentRent(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO apartment_rents (agreement_id, apartment_id, first_payment, last_payment,"
                    + "BASIC_RENT, OPERATING_COSTS_PREPAYMENT, HEATING_COSTS_PREPAYMENT) VALUES (?,?,?,?,?,?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .setParameter(3, params[2])
                .setParameter(4, params[3])
                .setParameter(5, params[4])
                .setParameter(6, params[5])
                .setParameter(7, params[6])
                .executeUpdate());
    }

    protected void insertStorageRent(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO storage_rents (agreement_id, storage_id, first_payment, last_payment,"
                    + "BASIC_RENT, OPERATING_COSTS_PREPAYMENT, HEATING_COSTS_PREPAYMENT) VALUES (?,?,?,?,?,?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .setParameter(3, params[2])
                .setParameter(4, params[3])
                .setParameter(5, params[4])
                .setParameter(6, params[5])
                .setParameter(7, params[6])
                .executeUpdate());
    }

    protected void insertCommercialRent(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO commercial_rents (agreement_id, commercial_id, first_payment, last_payment,"
                    + "BASIC_RENT, OPERATING_COSTS_PREPAYMENT, HEATING_COSTS_PREPAYMENT) VALUES (?,?,?,?,?,?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .setParameter(3, params[2])
                .setParameter(4, params[3])
                .setParameter(5, params[4])
                .setParameter(6, params[5])
                .setParameter(7, params[6])
                .executeUpdate());
    }

    @Test
    void getRentalAgreements_SUCCESS_noAgreementsAreReturned() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(BASE_PATH)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .and().body("agreements", Matchers.notNullValue())
            .and().body("agreements.size()", Matchers.is(0));
    }

    @Test
    void getRentalAgreements_SUCCESS_agreementsAreReturned() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_4, TestData.USER_EMAIL_4, Duration.ofMinutes(10)))
            .get(BASE_PATH)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .and().body("agreements.size()", Matchers.is(2))
            .and().body("agreements[0].agreementId", Matchers.equalTo(TestData.AGREEMENT_ID_2.toString()))
            .and().body("agreements[0].rentalUnits.size()", Matchers.is(2))
            .and().body("agreements[0].basicRent", Matchers.equalTo(939f))
            .and().body("agreements[0].operatingCostsPrepayment", Matchers.equalTo(150f))
            .and().body("agreements[0].heatingCostsPrepayment", Matchers.equalTo(240f))
            .and().body("agreements[1].agreementId", Matchers.equalTo(TestData.AGREEMENT_ID_3.toString()))
            .and().body("agreements[1].rentalUnits.size()", Matchers.is(1))
            .and().body("agreements[1].basicRent", Matchers.nullValue())
            .and().body("agreements[1].operatingCostsPrepayment", Matchers.nullValue())
            .and().body("agreements[1].heatingCostsPrepayment", Matchers.nullValue());
    }

    @Test
    void getUser_SUCCESS_userHasCorrectRole() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(100)))
            .get("/api/v1/user")
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
                .and().body("id", Matchers.equalTo(TestData.USER_ID_3.toString()))
                .and().body("email", Matchers.equalTo(TestData.USER_EMAIL_3))
                .and().body("userContexts.size()", Matchers.equalTo(1))
                .and().body("userContexts[0]", Matchers.equalTo("TENANT"));
    }

}