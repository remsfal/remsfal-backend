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
import static org.junit.jupiter.api.Assertions.assertEquals;

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
            .and().body("id", Matchers.equalTo(TestData.COMMERCIAL_ID))
            .and().body("title", Matchers.equalTo(TestData.COMMERCIAL_TITLE))
            .and().body("description", Matchers.equalTo(TestData.COMMERCIAL_DESCRIPTION))
            .and().body("commercialSpace", Matchers.equalTo(TestData.COMMERCIAL_COMMERCIAL_SPACE))
            .and().body("usableSpace", Matchers.equalTo(TestData.COMMERCIAL_USABLE_SPACE))
            .and().body("heatingSpace", Matchers.equalTo(TestData.COMMERCIAL_HEATING_SPACE))
            .and().body("location", Matchers.equalTo(TestData.COMMERCIAL_LOCATION));
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
            .and().body("id", Matchers.equalTo(TestData.COMMERCIAL_ID))
            .and().body("title", Matchers.equalTo(TestData.COMMERCIAL_TITLE))
            .and().body("description", Matchers.equalTo(TestData.COMMERCIAL_DESCRIPTION))
            .and().body("commercialSpace", Matchers.equalTo(TestData.COMMERCIAL_COMMERCIAL_SPACE))
            .and().body("usableSpace", Matchers.equalTo(TestData.COMMERCIAL_USABLE_SPACE))
            .and().body("heatingSpace", Matchers.equalTo(TestData.COMMERCIAL_HEATING_SPACE))
            .and().body("location", Matchers.equalTo(TestData.COMMERCIAL_LOCATION));
    }

    void getTenancy_SUCCESS_tenancyIsReturned() {
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .get(BASE_PATH + "/{tenancyId}", TestData.TENANCY_ID_1)
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .header("location", Matchers.containsString(BASE_PATH.replace("{projectId}", TestData.PROJECT_ID)
                .replace("{propertyId}", TestData.PROPERTY_ID)
                .replace("{buildingId}", TestData.BUILDING_ID) + "/"))
            .and().body("id", Matchers.notNullValue())
            .and().body("title", Matchers.equalTo(TestData.COMMERCIAL_TITLE_2));

        long entities = entityManager
            .createQuery("SELECT count(commercial) FROM CommercialEntity commercial where commercial.title = :title",
                long.class)
            .setParameter("title", TestData.COMMERCIAL_TITLE_2)
            .getSingleResult();
        assertEquals(1, entities);
    }

}
