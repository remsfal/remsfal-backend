package de.remsfal.ticketing.boundary.contractor;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.TicketingTestData;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class QuotationRequestResourceTest extends AbstractTicketingTest {

    static final String BASE_PATH = "/ticketing/v1/issues";
    static final String QUOTATION_PATH = "/ticketing/v1/order-management/quotation-requests";

    @Test
    void getQuotationRequests_FAILED_noAuthentication() {
        given()
            .when()
            .get(QUOTATION_PATH)
            .then()
            .statusCode(401);
    }

    @Test
    void getQuotationRequests_FAILED_staffOrgRole() {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                TicketingTestData.USER_NAME,
                Map.of(), Map.of(TicketingTestData.ORGANIZATION_ID.toString(), "STAFF"), Map.of()))
            .get(QUOTATION_PATH)
            .then()
            .statusCode(403);
    }

    @Test
    void getQuotationRequests_FAILED_noOrgRole() {
        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "noorg@test.com", "No Org",
                Map.of(), Map.of(), Map.of()))
            .get(QUOTATION_PATH)
            .then()
            .statusCode(403);
    }

    @Test
    void getQuotationRequests_SUCCESS_contractorManagerSeesOwnRequests() {
        final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
        final UUID contractorId = UUID.randomUUID();

        // Create an issue as manager
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\""
            + "}";
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        // Create quotation request linked to the organization
        String requestJson = "{ \"contractors\":[{\"id\":\"" + contractorId
            + "\",\"companyName\":\"Test Betrieb\",\"organizationId\":\"" + organizationId + "\"}],"
            + "\"scopeOfWork\":\"Bitte Angebot einreichen.\" }";
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        // Contractor with MANAGER org role retrieves quotation requests
        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "contractor@test.com", "Contractor Manager",
                Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of()))
            .get(QUOTATION_PATH)
            .then()
            .statusCode(200)
            .body("items", hasSize(1))
            .body("items[0].contractorId", equalTo(contractorId.toString()))
            .body("items[0].organizationId", equalTo(organizationId.toString()))
            .body("items[0].status", equalTo("REQUESTED"));
    }

    @Test
    void updateQuotationRequest_SUCCESS_contractorSetsSubmitted() {
        final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
        final UUID contractorId = UUID.randomUUID();

        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\""
            + "}";
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body("{ \"contractors\":[{\"id\":\"" + contractorId
                + "\",\"companyName\":\"Test Betrieb\",\"organizationId\":\"" + organizationId + "\"}] }")
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        final String requestId = given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "contractor@test.com", "Contractor Manager",
                Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of()))
            .get(QUOTATION_PATH)
            .then()
            .statusCode(200)
            .extract().path("items[0].id");

        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "contractor@test.com", "Contractor Manager",
                Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of()))
            .contentType(ContentType.JSON)
            .body("{ \"status\":\"SUBMITTED\" }")
            .patch(QUOTATION_PATH + "/" + requestId)
            .then()
            .statusCode(200)
            .body("status", equalTo("SUBMITTED"));
    }

    @Test
    void updateQuotationRequest_FAILED_invalidContractorStatus() {
        final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
        final UUID contractorId = UUID.randomUUID();

        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\""
            + "}";
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body("{ \"contractors\":[{\"id\":\"" + contractorId
                + "\",\"companyName\":\"Test Betrieb\",\"organizationId\":\"" + organizationId + "\"}] }")
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        final String requestId = given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "contractor@test.com", "Contractor Manager",
                Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of()))
            .get(QUOTATION_PATH)
            .then()
            .statusCode(200)
            .extract().path("items[0].id");

        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "contractor@test.com", "Contractor Manager",
                Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of()))
            .contentType(ContentType.JSON)
            .body("{ \"status\":\"WITHDRAWN\" }")
            .patch(QUOTATION_PATH + "/" + requestId)
            .then()
            .statusCode(400);
    }

    @Test
    void getQuotationRequests_SUCCESS_includesBillingAddressFields() {
        final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
        final UUID contractorId = UUID.randomUUID();

        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\""
            + "}";
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        String requestJson = "{ \"contractors\":[{\"id\":\"" + contractorId
            + "\",\"companyName\":\"Bauservice GmbH\",\"organizationId\":\"" + organizationId + "\"}],"
            + "\"projectOwner\":\"Mustermann Verwaltung GmbH\","
            + "\"projectCareOf\":\"Max Mustermann\","
            + "\"billingAddress\":{"
            + "\"street\":\"Musterstraße 1\","
            + "\"city\":\"Berlin\","
            + "\"province\":\"Berlin\","
            + "\"zip\":\"10115\","
            + "\"countryCode\":\"DE\""
            + "}}";
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "contractor@test.com", "Contractor Manager",
                Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of()))
            .get(QUOTATION_PATH)
            .then()
            .statusCode(200)
            .body("items", hasSize(1))
            .body("items[0].projectOwner", equalTo("Mustermann Verwaltung GmbH"))
            .body("items[0].projectCareOf", equalTo("Max Mustermann"))
            .body("items[0].projectBillingAddress1", equalTo("Musterstraße 1"))
            .body("items[0].projectBillingAddress2", equalTo("10115 Berlin"))
            .body("items[0].projectBillingAddress3", equalTo("Berlin, DE"));
    }

    @Test
    void getQuotationRequests_SUCCESS_ownerOrgRoleAlsoAllowed() {
        final UUID organizationId = TicketingTestData.ORGANIZATION_ID_2;
        final UUID contractorId = UUID.randomUUID();

        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\""
            + "}";
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        String requestJson = "{ \"contractors\":[{\"id\":\"" + contractorId
            + "\",\"companyName\":\"Owner Betrieb\",\"organizationId\":\"" + organizationId + "\"}] }";
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        // Contractor with OWNER org role can also retrieve
        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "owner@test.com", "Org Owner",
                Map.of(), Map.of(organizationId.toString(), "OWNER"), Map.of()))
            .get(QUOTATION_PATH)
            .then()
            .statusCode(200)
            .body("items", hasSize(1));
    }

    @Test
    void createQuotation_SUCCESS_contractorManagerCreatesQuotationResponse() {
        final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
        final UUID contractorId = UUID.randomUUID();

        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\""
            + "}";
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body("{ \"contractors\":[{\"id\":\"" + contractorId
                + "\",\"companyName\":\"Test Betrieb\",\"organizationId\":\"" + organizationId + "\"}] }")
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        final UUID contractorUserId = UUID.randomUUID();
        final String requestId = given()
            .when()
            .cookie(buildCookie(contractorUserId, "contractor@test.com", "Contractor Manager",
                Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of()))
            .get(QUOTATION_PATH)
            .then()
            .statusCode(200)
            .extract().path("items[0].id");

        given()
            .when()
            .cookie(buildCookie(contractorUserId, "contractor@test.com", "Contractor Manager",
                Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of()))
            .contentType(ContentType.JSON)
            .body("{ \"validUntil\":\"2030-12-31T23:59:59Z\","
                + "\"status\":\"VALID\" }")
            .post(QUOTATION_PATH + "/" + requestId + "/quotation")
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("issueId", equalTo(issueId))
            .body("requestId", equalTo(requestId))
            .body("projectId", equalTo(TicketingTestData.PROJECT_ID.toString()))
            .body("offererId", equalTo(contractorUserId.toString()))
            .body("contractorId", equalTo(contractorId.toString()))
            .body("status", equalTo("VALID"))
            .body("createdAt", notNullValue());
    }

    @Test
    void createQuotation_FAILED_noOrganizationRole() {
        final String randomRequestId = UUID.randomUUID().toString();

        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "noorg@test.com", "No Org", Map.of(), Map.of(), Map.of()))
            .contentType(ContentType.JSON)
            .body("{ \"status\":\"VALID\" }")
            .post(QUOTATION_PATH + "/" + randomRequestId + "/quotation")
            .then()
            .statusCode(403);
    }

}
