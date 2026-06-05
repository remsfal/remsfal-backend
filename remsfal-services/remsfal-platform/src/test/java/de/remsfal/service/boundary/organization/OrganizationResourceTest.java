package de.remsfal.service.boundary.organization;

import de.remsfal.service.boundary.AbstractResourceTest;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class OrganizationResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/organizations";

    @BeforeEach
    protected void setupTestData() {
        super.setupTestUsers();
        super.setupTestOrganizations();
    }

    @Test
    void getOrganization_FAILED_notFound() {
        given()
            .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .get(BASE_PATH + "/" + UUID.randomUUID())
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void getOrganizations_FAILED_noAuthentication() {
        given()
            .when()
            .get(BASE_PATH)
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void createOrganization_SUCCESS_organizationIsCreated() {
        final String json = "{\"name\": \"" + TestData.ORGANIZATION_NAME + "\",\n" +
                "  \"phone\": \"" + TestData.ORGANIZATION_PHONE + "\",\n" +
                "  \"email\": \"" + TestData.ORGANIZATION_EMAIL + "\",\n" +
                "  \"vatIdentificationNumber\": \"" + TestData.ORGANIZATION_VAT_IDENTIFICATION_NUMBER + "\",\n" +
                "  \"trade\": \"" + TestData.ORGANIZATION_TRADE + "\",\n" +
                " \"address\": { " +
                    "\"street\":\"" + TestData.ADDRESS_STREET + "\",\n" +
                    "\"city\":\"" + TestData.ADDRESS_CITY + "\",\n" +
                    "\"province\":\"" + TestData.ADDRESS_PROVINCE + "\",\n" +
                    "\"zip\":\"" + TestData.ADDRESS_ZIP + "\",\n" +
                    "\"countryCode\":\"" + TestData.ADDRESS_COUNTRY + "\"}" +
                "}";
        final String organizationId = given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body(json)
            .post(BASE_PATH)
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .contentType(ContentType.JSON)
            .header("location", Matchers.containsString(BASE_PATH + "/"))
            .and().body("id", Matchers.notNullValue())
            .and().body("name", Matchers.equalTo(TestData.ORGANIZATION_NAME))
            .and().body("phone", Matchers.equalTo(TestData.ORGANIZATION_PHONE))
            .and().body("email", Matchers.equalTo(TestData.ORGANIZATION_EMAIL))
            .and().body("vatIdentificationNumber", Matchers.equalTo(TestData.ORGANIZATION_VAT_IDENTIFICATION_NUMBER))
            .and().body("trade", Matchers.equalTo(TestData.ORGANIZATION_TRADE))
            .statusCode(Response.Status.CREATED.getStatusCode())
            .extract().path("id");

        long entities = entityManager
                .createQuery("SELECT count(organization) FROM OrganizationEntity organization WHERE organization.id = :organizationId", Long.class)
                .setParameter("organizationId", UUID.fromString(organizationId))
                .getSingleResult();
        assertEquals(1, entities);
    }

    @Test
    void createOrganization_FAILED_idIsProvided() {
        final String json = "{ \"id\": " + TestData.ORGANIZATION_ID + ",\n" +
                "  \"name\": \"" + TestData.ORGANIZATION_NAME + "\",\n" +
                "  \"phone\": \"" + TestData.ORGANIZATION_PHONE + "\",\n" +
                "  \"email\": \"" + TestData.ORGANIZATION_EMAIL + "\",\n" +
                "  \"trade\": \"" + TestData.ORGANIZATION_TRADE + "\"\n" +
                "}";

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(ContentType.JSON)
                .body(json)
                .post(BASE_PATH)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void createOrganization_FAILED_noAuthentication() {
        final String json = "{\"name\": \"" + TestData.ORGANIZATION_NAME + "\",\n" +
                "  \"phone\": \"" + TestData.ORGANIZATION_PHONE + "\",\n" +
                "  \"email\": \"" + TestData.ORGANIZATION_EMAIL + "\",\n" +
                "  \"vatIdentificationNumber\": \"" + TestData.ORGANIZATION_VAT_IDENTIFICATION_NUMBER + "\",\n" +
                "  \"trade\": \"" + TestData.ORGANIZATION_TRADE + "\"\n" +
                "}";

        given()
            .when()
            .contentType(ContentType.JSON)
            .body(json)
            .post(BASE_PATH)
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void createOrganization_FAILED_noName() {
        final String json = "{\"name\": \" \",\n" +
                "  \"phone\": \"" + TestData.ORGANIZATION_PHONE + "\",\n" +
                "  \"email\": \"" + TestData.ORGANIZATION_EMAIL + "\",\n" +
                "  \"trade\": \"" + TestData.ORGANIZATION_TRADE + "\"\n" +
                "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body(json)
            .post(BASE_PATH)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void getOrganization_SUCCESS_sameOrganizationIsReturned() {
        final String json = "{\"name\": \"" + TestData.ORGANIZATION_NAME + "\",\n" +
                "  \"phone\": \"" + TestData.ORGANIZATION_PHONE + "\",\n" +
                "  \"email\": \"" + TestData.ORGANIZATION_EMAIL + "\",\n" +
                "  \"vatIdentificationNumber\": \"" + TestData.ORGANIZATION_VAT_IDENTIFICATION_NUMBER + "\",\n" +
                "  \"trade\": \"" + TestData.ORGANIZATION_TRADE + "\"\n" +
                "}";

        final io.restassured.response.Response res = given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .post(BASE_PATH)
                .thenReturn();

        final String organizationId = res.then()
                .contentType(MediaType.APPLICATION_JSON)
                .extract().path("id");

        final String organizationUrl = res.then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .header("location", Matchers.startsWith("http://localhost:8081/api/v1/organization"))
                .header("location", Matchers.endsWith(organizationId))
                .extract().header("location");

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .get(organizationUrl)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("id", Matchers.equalTo(organizationId))
                .and().body("name", Matchers.equalTo(TestData.ORGANIZATION_NAME))
                .and().body("phone", Matchers.equalTo(TestData.ORGANIZATION_PHONE))
                .and().body("email", Matchers.equalTo(TestData.ORGANIZATION_EMAIL))
                .and().body("vatIdentificationNumber", Matchers.equalTo(TestData.ORGANIZATION_VAT_IDENTIFICATION_NUMBER))
                .and().body("trade", Matchers.equalTo(TestData.ORGANIZATION_TRADE));
    }

    @Test
    void getAllOrganizations_SUCCESS_ownedOrganizationsAreReturned() {
        // USER_ID is OWNER of all 3 test organizations
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("organizations.size()", Matchers.equalTo(3))
            .and().body("total", Matchers.equalTo(3));

        // USER_ID_2 is MANAGER (not OWNER) → empty list
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .get(BASE_PATH)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("organizations.size()", Matchers.equalTo(0));
    }

    @Test
    void getOrganizationEmployments_SUCCESS_employmentListIsReturned() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH + "/employments")
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("employees.size()", Matchers.equalTo(3))
            .and().body("employees[0].organizationId", Matchers.notNullValue())
            .and().body("employees[0].organizationName", Matchers.notNullValue());

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .get(BASE_PATH + "/employments")
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("employees.size()", Matchers.equalTo(1))
            .and().body("employees[0].employeeRole", Matchers.equalTo("MANAGER"))
            .and().body("employees[0].organizationId", Matchers.notNullValue())
            .and().body("employees[0].organizationName", Matchers.notNullValue());
    }

    @Test
    void updateOrganization_SUCCESS_changedName() {
        final String json_updated = "{\"name\": \"" + "New Name" + "\",\n" +
                "  \"phone\": \"" + TestData.ORGANIZATION_PHONE + "\",\n" +
                "  \"email\": \"" + TestData.ORGANIZATION_EMAIL + "\",\n" +
                "  \"vatIdentificationNumber\": \"" + TestData.ORGANIZATION_VAT_IDENTIFICATION_NUMBER + "\",\n" +
                "  \"trade\": \"" + TestData.ORGANIZATION_TRADE + "\",\n" +
                " \"address\": { " +
                    "\"street\":\"" + TestData.ADDRESS_STREET + "\",\n" +
                    "\"city\":\"" + TestData.ADDRESS_CITY + "\",\n" +
                    "\"province\":\"" + TestData.ADDRESS_PROVINCE + "\",\n" +
                    "\"zip\":\"" + TestData.ADDRESS_ZIP + "\",\n" +
                    "\"countryCode\":\"" + TestData.ADDRESS_COUNTRY + "\"}" +
                "}";

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(json_updated)
                .patch(BASE_PATH + "/" + TestData.ORGANIZATION_ID)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("id", Matchers.equalTo(TestData.ORGANIZATION_ID.toString()))
                .and().body("name", Matchers.equalTo("New Name"))
                .and().body("phone", Matchers.equalTo(TestData.ORGANIZATION_PHONE))
                .and().body("email", Matchers.equalTo(TestData.ORGANIZATION_EMAIL))
                .and().body("vatIdentificationNumber", Matchers.equalTo(TestData.ORGANIZATION_VAT_IDENTIFICATION_NUMBER))
                .and().body("trade", Matchers.equalTo(TestData.ORGANIZATION_TRADE));
    }

    @Test
    void deleteOrganization_SUCCESS_singleOrganization() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .delete(BASE_PATH + "/" + TestData.ORGANIZATION_ID)
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        long entities = entityManager
                .createQuery("SELECT count(organization) FROM OrganizationEntity organization where organization.id = :id", Long.class)
                .setParameter("id", TestData.ORGANIZATION_ID)
                .getSingleResult();
        assertEquals(0, entities);
    }

    @Test
    void deleteOrganization_FAILED_notFound() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .delete(BASE_PATH + "/" + UUID.randomUUID())
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void updatedOrganization_FAILED_noPermission() {
        final String json = "{\"name\": \"" + "New Name" + "\",\n" +
            "  \"phone\": \"" + TestData.ORGANIZATION_PHONE_3 + "\",\n" +
            "  \"email\": \"" + TestData.ORGANIZATION_EMAIL_3 + "\",\n" +
            "  \"trade\": \"" + TestData.ORGANIZATION_TRADE_3 + "\"\n" +
            "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .patch(BASE_PATH + "/" + TestData.ORGANIZATION_ID_3)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void deleteOrganization_FAILED_noPermission() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .delete(BASE_PATH + "/" + TestData.ORGANIZATION_ID_3)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void searchOrganizations_SUCCESS_resultsReturned() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .queryParam("name", TestData.ORGANIZATION_NAME.substring(0, 4))
            .get(BASE_PATH + "/search")
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("organizations.size()", Matchers.greaterThanOrEqualTo(1))
            .and().body("total", Matchers.greaterThanOrEqualTo(1));
    }

    @Test
    void searchOrganizations_FAILED_noAuthentication() {
        given()
            .when()
            .queryParam("name", TestData.ORGANIZATION_NAME)
            .get(BASE_PATH + "/search")
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getContractors_FAILED_noAuthentication() {
        given()
            .when()
            .get(BASE_PATH + "/contractors")
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getContractors_SUCCESS_directProjectMember() {
        // PROJECT_ID_1 with USER_ID as direct MANAGER member
        super.setupTestProjects();
        final UUID contractorId = UUID.fromString("cc000000-0000-0000-0000-000000000001");
        insertContractor(contractorId, TestData.PROJECT_ID_1, "Test Contractor", TestData.ORGANIZATION_ID);

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH + "/contractors")
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("organizations.size()", Matchers.equalTo(1))
            .and().body("total", Matchers.equalTo(1))
            .and().body("organizations[0].id", Matchers.equalTo(TestData.ORGANIZATION_ID.toString()));
    }

    @Test
    void getContractors_SUCCESS_viaOrganizationMembership() {
        // USER_ID_2 is MANAGER of ORGANIZATION_ID_3
        // Link ORGANIZATION_ID_3 to PROJECT_ID_1 → USER_ID_2 gets access via org membership
        super.setupTestProjects();
        insertProjectOrganization(TestData.PROJECT_ID_1, TestData.ORGANIZATION_ID_3, "COLLABORATOR");
        final UUID contractorId = UUID.fromString("cc000000-0000-0000-0000-000000000002");
        insertContractor(contractorId, TestData.PROJECT_ID_1, "Test Contractor", TestData.ORGANIZATION_ID);

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .get(BASE_PATH + "/contractors")
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("organizations.size()", Matchers.equalTo(1))
            .and().body("total", Matchers.equalTo(1))
            .and().body("organizations[0].id", Matchers.equalTo(TestData.ORGANIZATION_ID.toString()));
    }
}
