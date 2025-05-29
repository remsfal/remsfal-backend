package de.remsfal.service.boundary.project;

import de.remsfal.core.json.project.ImmutableSiteJson;
import de.remsfal.service.TestData;
import de.remsfal.service.boundary.AbstractResourceTest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class SiteResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/projects/{projectId}/properties/{propertyId}/sites";

    @Override
    @BeforeEach
    protected void setupTestProperties() {
        super.setupTestUsers();
        super.setupTestProjects();
        super.setupTestProperties();
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = "{ \"title\":\"" + TestData.SITE_TITLE + "\","
        + "\"address\":{"
        + "\"street\":\"" + TestData.ADDRESS_STREET + "\","
        + "\"city\":\"" + TestData.ADDRESS_CITY + "\","
        + "\"province\":\"" + TestData.ADDRESS_PROVINCE + "\","
        + "\"zip\":\"" + TestData.ADDRESS_ZIP + "\","
        + "\"countryCode\":\"" + TestData.ADDRESS_COUNTRY + "\"}}")
    void createSite_SUCCESS_propertyIsCreated(final String json) {
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body(json)
            .post(BASE_PATH, TestData.PROJECT_ID, TestData.PROPERTY_ID)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .contentType(ContentType.JSON)
            .header("location", Matchers.containsString(BASE_PATH.replace("{projectId}", TestData.PROJECT_ID)
                .replace("{propertyId}", TestData.PROPERTY_ID) + "/"))
            .and().body("id", Matchers.notNullValue())
            .and().body("title", Matchers.equalTo(TestData.SITE_TITLE));

        long enties = entityManager
            .createQuery("SELECT count(site) FROM SiteEntity site where site.title = :title", Long.class)
            .setParameter("title", TestData.SITE_TITLE)
            .getSingleResult();
        assertEquals(1, enties);
    }

    @Test
    void createSite_FAILED_noTitle() {
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body("{ \"title\":\" \"}")
            .post(BASE_PATH, TestData.PROJECT_ID, TestData.PROPERTY_ID)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .body("parameterViolations.path", Matchers.hasItem("createSite.site.title"));
    }

    @Test
    void createSite_SUCCESS_noAddress() {
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body("{ \"title\":\"" + TestData.SITE_TITLE + "\"}")
            .post(BASE_PATH, TestData.PROJECT_ID, TestData.PROPERTY_ID)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .contentType(ContentType.JSON)
            .header("location", Matchers.containsString(BASE_PATH.replace("{projectId}", TestData.PROJECT_ID)
                .replace("{propertyId}", TestData.PROPERTY_ID) + "/"))
            .and().body("id", Matchers.notNullValue())
            .and().body("title", Matchers.equalTo(TestData.SITE_TITLE));
    }

    @Test
    void createSite_FAILED_idIsProvided() {
        final String json = "{ \"title\":\"" + TestData.SITE_TITLE + "\","
            + "\"id\":\"" + TestData.SITE_ID + "\"}";
        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body(json)
            .post(BASE_PATH, TestData.PROJECT_ID, TestData.PROPERTY_ID)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .body("parameterViolations.path", Matchers.hasItem("createSite.site.id"));
    }

    @Test
    void getSite_SUCCESS_siteCorrectlyReturned() {
        setupTestSites();

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH + "/{siteId}", TestData.PROJECT_ID, TestData.PROPERTY_ID, TestData.SITE_ID)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.equalTo(TestData.SITE_ID))
            .and().body("title", Matchers.equalTo(TestData.SITE_TITLE))
            .and().body("address.street", Matchers.equalTo(TestData.ADDRESS_STREET_5))
            .and().body("address.city", Matchers.equalTo(TestData.ADDRESS_CITY_5))
            .and().body("address.province", Matchers.equalTo(TestData.ADDRESS_PROVINCE_5))
            .and().body("address.zip", Matchers.equalTo(TestData.ADDRESS_ZIP_5))
            .and().body("address.countryCode", Matchers.equalTo("DE"))
            .and().body("address.country", Matchers.nullValue())
            .and().body("description", Matchers.equalTo(TestData.SITE_DESCRIPTION))
            .and().body("usableSpace", Matchers.equalTo(TestData.SITE_USABLE_SPACE));
    }

    @Test
    void deleteSite_SUCCESS_siteIsdeleted() {
        setupTestSites();

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .delete(BASE_PATH + "/{siteId}", TestData.PROJECT_ID, TestData.PROPERTY_ID, TestData.SITE_ID)
            .then()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH + "/{siteId}", TestData.PROJECT_ID, TestData.PROPERTY_ID, TestData.SITE_ID)
            .then()
            .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void updateSite_SUCCESS_siteCorrectlyUpdated() {
        setupTestSites();
        final ImmutableSiteJson json = ImmutableSiteJson
            .builder()
            .title(TestData.BUILDING_TITLE)
            .address(TestData.addressBuilder6().build())
            .build();

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .patch(BASE_PATH + "/{siteId}", TestData.PROJECT_ID, TestData.PROPERTY_ID, TestData.SITE_ID)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.equalTo(TestData.SITE_ID))
            .and().body("title", Matchers.equalTo(TestData.BUILDING_TITLE))
            .and().body("address.street", Matchers.equalTo(TestData.ADDRESS_STREET_6))
            .and().body("address.city", Matchers.equalTo(TestData.ADDRESS_CITY_6))
            .and().body("address.province", Matchers.equalTo(TestData.ADDRESS_PROVINCE_6))
            .and().body("address.zip", Matchers.equalTo(TestData.ADDRESS_ZIP_6))
            .and().body("address.countryCode", Matchers.equalTo("DE"))
            .and().body("address.country", Matchers.nullValue())
            .and().body("description", Matchers.equalTo(TestData.SITE_DESCRIPTION));

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH + "/{siteId}", TestData.PROJECT_ID, TestData.PROPERTY_ID, TestData.SITE_ID)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.equalTo(TestData.SITE_ID))
            .and().body("title", Matchers.equalTo(TestData.BUILDING_TITLE))
            .and().body("address.street", Matchers.equalTo(TestData.ADDRESS_STREET_6))
            .and().body("address.city", Matchers.equalTo(TestData.ADDRESS_CITY_6))
            .and().body("address.province", Matchers.equalTo(TestData.ADDRESS_PROVINCE_6))
            .and().body("address.zip", Matchers.equalTo(TestData.ADDRESS_ZIP_6))
            .and().body("address.countryCode", Matchers.equalTo("DE"))
            .and().body("address.country", Matchers.nullValue())
            .and().body("description", Matchers.equalTo(TestData.SITE_DESCRIPTION));
    }

    @ParameterizedTest(name = "{displayName} - {arguments}")
    @ValueSource(strings = "{ \"tenancy\":{"
        + "\"startOfRental\":\"" + TestData.TENANCY_START + "\","
        + "\"endOfRental\":\"" + TestData.TENANCY_END + "\","
        + "\"rent\":[{\"billingCycle\":\"MONTHLY\",\"firstPaymentDate\":\"" + TestData.TENANCY_START + "\","
        + "\"lastPaymentDate\":\"" + TestData.TENANCY_END + "\", \"basicRent\":523.89,"
        + "\"operatingCostsPrepayment\":123.66, \"heatingCostsPrepayment\":210.02}],"
        + "\"tenant\":{ \"firstName\":\"" + TestData.USER_FIRST_NAME_3 + "\","
        + "\"lastName\":\"" + TestData.USER_LAST_NAME_3 + "\","
        + "\"email\":\"" + TestData.USER_EMAIL_3 + "\","
        + "\"mobilePhoneNumber\":\"+491773289245\","
        + "\"businessPhoneNumber\":\"+49302278349\","
        + "\"privatePhoneNumber\":\"+4933012345611\"}},"
        + "\"usableSpace\":51.99}")
    void updateSite_SUCCESS_tenancyCorrectlyUpdated(final String json) {
        setupTestSites();

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .patch(BASE_PATH + "/{siteId}", TestData.PROJECT_ID, TestData.PROPERTY_ID, TestData.SITE_ID)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.equalTo(TestData.SITE_ID))
            .and().body("title", Matchers.equalTo(TestData.SITE_TITLE))
            .and().body("address.street", Matchers.equalTo(TestData.ADDRESS_STREET_5))
            .and().body("address.city", Matchers.equalTo(TestData.ADDRESS_CITY_5))
            .and().body("address.province", Matchers.equalTo(TestData.ADDRESS_PROVINCE_5))
            .and().body("address.zip", Matchers.equalTo(TestData.ADDRESS_ZIP_5))
            .and().body("address.countryCode", Matchers.equalTo("DE"))
            .and().body("address.country", Matchers.nullValue())
            .and().body("description", Matchers.equalTo(TestData.SITE_DESCRIPTION))
            // TODO: test tenancy
//            .and().body("tenancy.id", Matchers.notNullValue())
//            .and().body("tenancy.startOfRental", Matchers.equalTo(TestData.TENANCY_START))
//            .and().body("tenancy.endOfRental", Matchers.equalTo(TestData.TENANCY_END))
//            .and().body("tenancy.rent.billingCycle", Matchers.hasItems("MONTHLY"))
//            .and().body("tenancy.rent.firstPaymentDate", Matchers.hasItems(TestData.TENANCY_START))
//            .and().body("tenancy.rent.lastPaymentDate", Matchers.hasItems(TestData.TENANCY_END))
//            .and().body("tenancy.rent.basicRent", Matchers.hasItems(523.89f))
//            .and().body("tenancy.rent.operatingCostsPrepayment", Matchers.hasItems(123.66f))
//            .and().body("tenancy.rent.heatingCostsPrepayment", Matchers.hasItems(210.02f))
//            .and().body("tenancy.tenant.firstName", Matchers.equalTo(TestData.USER_FIRST_NAME_3))
//            .and().body("tenancy.tenant.lastName", Matchers.equalTo(TestData.USER_LAST_NAME_3))
//            .and().body("tenancy.tenant.email", Matchers.equalTo(TestData.USER_EMAIL_3))
//            .and().body("tenancy.tenant.mobilePhoneNumber", Matchers.equalTo("+491773289245"))
//            .and().body("tenancy.tenant.businessPhoneNumber", Matchers.equalTo("+49302278349"))
//            .and().body("tenancy.tenant.privatePhoneNumber", Matchers.equalTo("+4933012345611"))
            .and().body("usableSpace", Matchers.equalTo(51.99f));

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH + "/{siteId}", TestData.PROJECT_ID, TestData.PROPERTY_ID, TestData.SITE_ID)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.equalTo(TestData.SITE_ID))
            // TODO
//            .and().body("tenancy.id", Matchers.notNullValue())
//            .and().body("tenancy.startOfRental", Matchers.equalTo(TestData.TENANCY_START))
//            .and().body("tenancy.endOfRental", Matchers.equalTo(TestData.TENANCY_END))
//            .and().body("tenancy.rent.billingCycle", Matchers.hasItems("MONTHLY"))
//            .and().body("tenancy.rent.firstPaymentDate", Matchers.hasItems(TestData.TENANCY_START))
//            .and().body("tenancy.rent.lastPaymentDate", Matchers.hasItems(TestData.TENANCY_END))
//            .and().body("tenancy.rent.basicRent", Matchers.hasItems(523.89f))
//            .and().body("tenancy.rent.operatingCostsPrepayment", Matchers.hasItems(123.66f))
//            .and().body("tenancy.rent.heatingCostsPrepayment", Matchers.hasItems(210.02f))
//            .and().body("tenancy.tenant.firstName", Matchers.equalTo(TestData.USER_FIRST_NAME_3))
//            .and().body("tenancy.tenant.lastName", Matchers.equalTo(TestData.USER_LAST_NAME_3))
//            .and().body("tenancy.tenant.email", Matchers.equalTo(TestData.USER_EMAIL_3))
//            .and().body("tenancy.tenant.mobilePhoneNumber", Matchers.equalTo("+491773289245"))
//            .and().body("tenancy.tenant.businessPhoneNumber", Matchers.equalTo("+49302278349"))
//            .and().body("tenancy.tenant.privatePhoneNumber", Matchers.equalTo("+4933012345611"))
            .and().body("usableSpace", Matchers.equalTo(51.99f));
    }

    @Test
    void getAndDeleteSite_SUCCESS_shortcut() {
        setupTestSites();

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get("/api/v1/projects/{projectId}/sites/{siteId}", TestData.PROJECT_ID, TestData.SITE_ID)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .and().body("id", Matchers.equalTo(TestData.SITE_ID));

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"usableSpace\":\"56.7\"}")
            .patch("/api/v1/projects/{projectId}/sites/{siteId}", TestData.PROJECT_ID, TestData.SITE_ID)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .and().body("usableSpace", Matchers.equalTo(56.7f));

        given()
            .when()
            .cookies(buildCookies(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .delete("/api/v1/projects/{projectId}/sites/{siteId}", TestData.PROJECT_ID, TestData.SITE_ID)
            .then()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

}