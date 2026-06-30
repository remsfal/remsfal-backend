package de.remsfal.ticketing.boundary;

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
class IssueQuotationResourceTest extends AbstractTicketingTest {

    static final String BASE_PATH = "/ticketing/v1/issues";
    static final String QUOTATION_PATH = "/ticketing/v1/order-management/quotation-requests";

    private String createIssue() {
        return given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body("{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
                + "\"type\":\"TASK\""
                + "}")
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");
    }

    private String createQuotationRequest(final String issueId, final UUID contractorId,
        final UUID organizationId) {
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body("{ \"contractors\":[{\"id\":\"" + contractorId
                + "\",\"companyName\":\"Test Betrieb\",\"organizationId\":\"" + organizationId + "\"}] }")
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        return given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "contractor@test.com", "Contractor",
                Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of()))
            .get(QUOTATION_PATH)
            .then()
            .statusCode(200)
            .extract().path("items[0].id");
    }

    private String createQuotation(final String requestId, final UUID contractorUserId,
        final UUID organizationId) {
        return given()
            .when()
            .cookie(buildCookie(contractorUserId, "contractor@test.com", "Contractor",
                Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of()))
            .contentType(ContentType.JSON)
            .body("{ \"status\":\"VALID\" }")
            .post(QUOTATION_PATH + "/" + requestId + "/quotation")
            .then()
            .statusCode(200)
            .extract().path("id");
    }

    // --- GET quotations ---

    @Test
    void getQuotations_FAILED_noAuthentication() {
        given()
            .when()
            .get(BASE_PATH + "/" + UUID.randomUUID() + "/quotations")
            .then()
            .statusCode(401);
    }

    @Test
    void getQuotations_FAILED_noProjectRole() {
        final String issueId = createIssue();

        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "other@test.com", "Other",
                Map.of(), Map.of(), Map.of()))
            .get(BASE_PATH + "/" + issueId + "/quotations")
            .then()
            .statusCode(403);
    }

    @Test
    void getQuotations_SUCCESS_returnsQuotationList() {
        final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
        final UUID contractorId = UUID.randomUUID();
        final UUID contractorUserId = UUID.randomUUID();

        final String issueId = createIssue();
        final String requestId = createQuotationRequest(issueId, contractorId, organizationId);
        createQuotation(requestId, contractorUserId, organizationId);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueId + "/quotations")
            .then()
            .statusCode(200)
            .body("items", hasSize(1))
            .body("items[0].id", notNullValue())
            .body("items[0].issueId", equalTo(issueId))
            .body("items[0].status", equalTo("VALID"));
    }

    @Test
    void getQuotations_SUCCESS_emptyListWhenNoQuotations() {
        final String issueId = createIssue();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueId + "/quotations")
            .then()
            .statusCode(200)
            .body("items", hasSize(0));
    }

    // --- GET single quotation ---

    @Test
    void getQuotation_FAILED_noAuthentication() {
        given()
            .when()
            .get(BASE_PATH + "/" + UUID.randomUUID() + "/quotations/" + UUID.randomUUID())
            .then()
            .statusCode(401);
    }

    @Test
    void getQuotation_SUCCESS_returnsQuotation() {
        final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
        final UUID contractorId = UUID.randomUUID();
        final UUID contractorUserId = UUID.randomUUID();

        final String issueId = createIssue();
        final String requestId = createQuotationRequest(issueId, contractorId, organizationId);
        final String quotationId = createQuotation(requestId, contractorUserId, organizationId);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueId + "/quotations/" + quotationId)
            .then()
            .statusCode(200)
            .body("id", equalTo(quotationId))
            .body("issueId", equalTo(issueId))
            .body("status", equalTo("VALID"));
    }

    @Test
    void getQuotation_FAILED_notFound() {
        final String issueId = createIssue();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueId + "/quotations/" + UUID.randomUUID())
            .then()
            .statusCode(404);
    }

}
