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
class IssueOrderPlacementResourceTest extends AbstractTicketingTest {

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

    private String setupQuotation(final String issueId, final UUID contractorId,
        final UUID organizationId, final UUID contractorUserId) {
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body("{ \"contractors\":[{\"id\":\"" + contractorId
                + "\",\"companyName\":\"Test Betrieb\",\"organizationId\":\"" + organizationId + "\"}],"
                + "\"projectOwner\":\"Mustermann Verwaltung GmbH\","
                + "\"projectCareOf\":\"Max Mustermann\","
                + "\"billingAddress\":{"
                + "\"street\":\"Musterstraße 1\","
                + "\"city\":\"Berlin\","
                + "\"province\":\"Berlin\","
                + "\"zip\":\"10115\","
                + "\"countryCode\":\"DE\""
                + "}}")
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        final String requestId = given()
            .when()
            .cookie(buildCookie(contractorUserId, "contractor@test.com", "Contractor",
                Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of()))
            .get(QUOTATION_PATH)
            .then()
            .statusCode(200)
            .extract().path("items[0].id");

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

    private String placeOrderPath(final String issueId, final String quotationId) {
        return BASE_PATH + "/" + issueId + "/quotations/" + quotationId + "/orders";
    }

    private String ordersListPath(final String issueId) {
        return BASE_PATH + "/" + issueId + "/orders";
    }

    private String orderPath(final String issueId, final String orderId) {
        return ordersListPath(issueId) + "/" + orderId;
    }

    private String placeOrder(final String issueId, final String quotationId) {
        return given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(placeOrderPath(issueId, quotationId))
            .then()
            .statusCode(201)
            .extract().path("id");
    }

    // --- POST place order ---

    @Test
    void placeOrder_FAILED_noAuthentication() {
        given()
            .when()
            .post(placeOrderPath(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
            .then()
            .statusCode(401);
    }

    @Test
    void placeOrder_FAILED_noProjectRole() {
        final String issueId = createIssue();

        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "other@test.com", "Other",
                Map.of(), Map.of(), Map.of()))
            .post(placeOrderPath(issueId, UUID.randomUUID().toString()))
            .then()
            .statusCode(403);
    }

    @Test
    void placeOrder_SUCCESS_createsOrderPlacement() {
        final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
        final UUID contractorId = UUID.randomUUID();
        final UUID contractorUserId = UUID.randomUUID();

        final String issueId = createIssue();
        final String quotationId = setupQuotation(issueId, contractorId, organizationId, contractorUserId);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(placeOrderPath(issueId, quotationId))
            .then()
            .statusCode(201);
    }

    // --- GET orders (list) ---

    @Test
    void getOrders_FAILED_noAuthentication() {
        given()
            .when()
            .get(ordersListPath(UUID.randomUUID().toString()))
            .then()
            .statusCode(401);
    }

    @Test
    void getOrders_SUCCESS_emptyListWhenNoOrders() {
        final String issueId = createIssue();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(ordersListPath(issueId))
            .then()
            .statusCode(200)
            .body("items", hasSize(0));
    }

    @Test
    void getOrders_SUCCESS_returnsOrderList() {
        final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
        final UUID contractorId = UUID.randomUUID();
        final UUID contractorUserId = UUID.randomUUID();

        final String issueId = createIssue();
        final String quotationId = setupQuotation(issueId, contractorId, organizationId, contractorUserId);
        placeOrder(issueId, quotationId);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(ordersListPath(issueId))
            .then()
            .statusCode(200)
            .body("items", hasSize(1))
            .body("items[0].id", notNullValue())
            .body("items[0].issueId", equalTo(issueId))
            .body("items[0].quotationId", equalTo(quotationId))
            .body("items[0].status", equalTo("PLACED"));
    }

    // --- GET order placement ---

    @Test
    void getOrderPlacement_FAILED_noAuthentication() {
        given()
            .when()
            .get(orderPath(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
            .then()
            .statusCode(401);
    }

    @Test
    void getOrderPlacement_SUCCESS_returnsPlacement() {
        final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
        final UUID contractorId = UUID.randomUUID();
        final UUID contractorUserId = UUID.randomUUID();

        final String issueId = createIssue();
        final String quotationId = setupQuotation(issueId, contractorId, organizationId, contractorUserId);
        final String orderId = placeOrder(issueId, quotationId);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(orderPath(issueId, orderId))
            .then()
            .statusCode(200)
            .body("id", equalTo(orderId))
            .body("issueId", equalTo(issueId))
            .body("quotationId", equalTo(quotationId))
            .body("projectId", equalTo(TicketingTestData.PROJECT_ID.toString()))
            .body("projectOwner", equalTo("Mustermann Verwaltung GmbH"))
            .body("projectCareOf", equalTo("Max Mustermann"))
            .body("projectBillingAddress1", equalTo("Musterstraße 1"))
            .body("projectBillingAddress2", equalTo("10115 Berlin"))
            .body("projectBillingAddress3", equalTo("Berlin, DE"))
            .body("contractorId", equalTo(contractorId.toString()))
            .body("contractorName", equalTo("Test Betrieb"))
            .body("status", equalTo("PLACED"))
            .body("organizationId", equalTo(organizationId.toString()));
    }

    @Test
    void getOrderPlacement_FAILED_notFound() {
        final String issueId = createIssue();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(orderPath(issueId, UUID.randomUUID().toString()))
            .then()
            .statusCode(404);
    }

    // --- DELETE withdraw order placement ---

    @Test
    void withdrawOrderPlacement_FAILED_noAuthentication() {
        given()
            .when()
            .delete(orderPath(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
            .then()
            .statusCode(401);
    }

    @Test
    void withdrawOrderPlacement_SUCCESS_setsStatusWithdrawn() {
        final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
        final UUID contractorId = UUID.randomUUID();
        final UUID contractorUserId = UUID.randomUUID();

        final String issueId = createIssue();
        final String quotationId = setupQuotation(issueId, contractorId, organizationId, contractorUserId);
        final String orderId = placeOrder(issueId, quotationId);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(orderPath(issueId, orderId))
            .then()
            .statusCode(204);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(orderPath(issueId, orderId))
            .then()
            .statusCode(200)
            .body("status", equalTo("WITHDRAWN"));
    }

}
