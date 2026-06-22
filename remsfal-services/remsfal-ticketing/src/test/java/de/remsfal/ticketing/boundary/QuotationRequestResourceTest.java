package de.remsfal.ticketing.boundary;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

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
    static final String QUOTATION_PATH = "/ticketing/v1/quotation-requests";

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
            + "\"freeText\":\"Bitte Angebot einreichen.\" }";
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
            .body("items[0].status", equalTo("VALID"));
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

}
