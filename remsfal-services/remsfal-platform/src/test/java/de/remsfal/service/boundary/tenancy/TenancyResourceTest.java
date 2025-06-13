package de.remsfal.service.boundary.tenancy;

import de.remsfal.service.TestData;
import de.remsfal.service.boundary.AbstractResourceTest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
        this.setupTestTenancies();
    }

    protected void setupTestTenancies() {
        insertTenancy(TestData.TENANCY_ID_1, TestData.PROJECT_ID,
            TestData.TENANCY_START_1, TestData.TENANCY_END_1);
        insertTenant(TestData.TENANCY_ID_1, TestData.USER_ID_3);
        insertApartmentRent(TestData.TENANCY_ID_1, TestData.APARTMENT_ID,
            TestData.TENANCY_START_1, TestData.TENANCY_END_1, 670f, 75f, 120f);
        insertStorageRent(TestData.TENANCY_ID_1, TestData.STORAGE_ID,
            TestData.TENANCY_START_1, TestData.TENANCY_END_1, 30f, null, null);
        insertCommercialRent(TestData.TENANCY_ID_1, TestData.COMMERCIAL_ID,
            TestData.TENANCY_START_1, TestData.TENANCY_END_1, 1030f, 230f, 320f);
        insertTenancy(TestData.TENANCY_ID_2, TestData.PROJECT_ID,
            TestData.TENANCY_START_2, TestData.TENANCY_END_2);
        insertTenant(TestData.TENANCY_ID_2, TestData.USER_ID_4);
        insertSiteRent(TestData.TENANCY_ID_2, TestData.SITE_ID_1,
            TestData.TENANCY_START_2, TestData.TENANCY_END_2, 40f, null, null);
        insertBuildingRent(TestData.TENANCY_ID_2, TestData.BUILDING_ID_2,
            TestData.TENANCY_START_2, TestData.TENANCY_END_2, 899f, 150f, 240f);
        insertTenancy(TestData.TENANCY_ID_3, TestData.PROJECT_ID,
            TestData.TENANCY_START_3, TestData.TENANCY_END_3);
        insertTenant(TestData.TENANCY_ID_3, TestData.USER_ID_3);
        insertTenant(TestData.TENANCY_ID_3, TestData.USER_ID_4);
        insertPropertyRent(TestData.TENANCY_ID_3, TestData.PROPERTY_ID_2,
            TestData.TENANCY_START_3, TestData.TENANCY_END_3, 100f, null, null);
    }

    protected void insertTenancy(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO TENANCY (ID, PROJECT_ID, START_OF_RENTAL, END_OF_RENTAL) VALUES (?,?,?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .setParameter(3, params[2])
                .setParameter(4, params[3])
                .executeUpdate());
    }

    protected void insertTenant(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO TENANT (TENANCY_ID, USER_ID) VALUES (?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .executeUpdate());
    }

    protected void insertPropertyRent(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO PROPERTY_RENT (TENANCY_ID, PROPERTY_ID, FIRST_PAYMENT, LAST_PAYMENT,"
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
                .createNativeQuery("INSERT INTO SITE_RENT (TENANCY_ID, SITE_ID, FIRST_PAYMENT, LAST_PAYMENT,"
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
                .createNativeQuery("INSERT INTO BUILDING_RENT (TENANCY_ID, BUILDING_ID, FIRST_PAYMENT, LAST_PAYMENT,"
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
                .createNativeQuery("INSERT INTO APARTMENT_RENT (TENANCY_ID, APARTMENT_ID, FIRST_PAYMENT, LAST_PAYMENT,"
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
                .createNativeQuery("INSERT INTO STORAGE_RENT (TENANCY_ID, STORAGE_ID, FIRST_PAYMENT, LAST_PAYMENT,"
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
                .createNativeQuery("INSERT INTO COMMERCIAL_RENT (TENANCY_ID, COMMERCIAL_ID, FIRST_PAYMENT, LAST_PAYMENT,"
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
    void getTenancies_SUCCESS_noTenanciesAreReturned() {
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(BASE_PATH)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .and().body("tenancies", Matchers.notNullValue())
            .and().body("tenancies.size()", Matchers.is(0));
    }

    @Test
    void getTenancies_SUCCESS_tenanciesAreReturned() {
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID_4, TestData.USER_EMAIL_4, Duration.ofMinutes(10)))
            .get(BASE_PATH)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .and().body("tenancies.size()", Matchers.is(3))
            .and().body("tenancies[0].id", Matchers.equalTo(TestData.TENANCY_ID_2 + "/sites/" + TestData.SITE_ID_1))
            .and().body("tenancies[0].name", Matchers.equalTo(TestData.SITE_TITLE_1))
            .and().body("tenancies[0].rentalType", Matchers.equalTo("SITE"))
            .and().body("tenancies[0].rentalTitle", Matchers.equalTo(TestData.SITE_TITLE_1))
            .and().body("tenancies[0].isActive", Matchers.equalTo(Boolean.TRUE))
            .and().body("tenancies[1].id", Matchers.equalTo(TestData.TENANCY_ID_2 + "/buildings/" + TestData.BUILDING_ID_2))
            .and().body("tenancies[1].name", Matchers.equalTo(TestData.BUILDING_TITLE_2))
            .and().body("tenancies[1].rentalType", Matchers.equalTo("BUILDING"))
            .and().body("tenancies[1].rentalTitle", Matchers.equalTo(TestData.BUILDING_TITLE_2))
            .and().body("tenancies[1].isActive", Matchers.equalTo(Boolean.TRUE))
            .and().body("tenancies[2].id", Matchers.equalTo(TestData.TENANCY_ID_3 + "/properties/" + TestData.PROPERTY_ID_2))
            .and().body("tenancies[2].name", Matchers.equalTo(TestData.PROPERTY_TITLE_2))
            .and().body("tenancies[2].rentalType", Matchers.equalTo("PROPERTY"))
            .and().body("tenancies[2].rentalTitle", Matchers.equalTo(TestData.PROPERTY_TITLE_2))
            .and().body("tenancies[2].isActive", Matchers.equalTo(Boolean.FALSE));
    }

    @Test
    void getTenancy_SUCCESS_tenancyIsReturned() {
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID_4, TestData.USER_EMAIL_4, Duration.ofMinutes(10)))
            .get(BASE_PATH + "/{tenancyId}/sites/{rentalId}", TestData.TENANCY_ID_2, TestData.SITE_ID_1)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .and().body("id", Matchers.equalTo(TestData.TENANCY_ID_2 + "/sites/" + TestData.SITE_ID_1))
            .and().body("rentalType", Matchers.equalTo("SITE"))
            .and().body("rentalTitle", Matchers.equalTo(TestData.SITE_TITLE_1))
            .and().body("startOfRental", Matchers.equalTo(TestData.TENANCY_START_2))
            .and().body("endOfRental", Matchers.equalTo(TestData.TENANCY_END_2))
            .and().body("billingCycle", Matchers.equalTo("MONTHLY"))
            .and().body("basicRent", Matchers.equalTo(40f))
            .and().body("operatingCostsPrepayment", Matchers.nullValue())
            .and().body("heatingCostsPrepayment", Matchers.nullValue());
    }

}
