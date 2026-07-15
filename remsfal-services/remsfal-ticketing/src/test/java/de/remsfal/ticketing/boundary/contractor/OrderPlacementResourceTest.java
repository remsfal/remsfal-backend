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
class OrderPlacementResourceTest extends AbstractTicketingTest {

    static final String ISSUE_BASE_PATH = "/ticketing/v1/issues";
    static final String QUOTATION_REQUEST_PATH = "/ticketing/v1/order-management/quotation-requests";
    static final String ORDER_PLACEMENT_PATH = "/ticketing/v1/order-management/order-placements";

    private String createIssueAndPlaceOrder(final UUID contractorId, final UUID organizationId,
        final UUID contractorUserId) {
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body("{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
                + "\"type\":\"TASK\""
                + "}")
            .post(ISSUE_BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body("{ \"contractors\":[{\"id\":\"" + contractorId
                + "\",\"companyName\":\"Test Betrieb\",\"organizationId\":\"" + organizationId + "\"}] }")
            .post(ISSUE_BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        final String requestId = given()
            .when()
            .cookie(buildCookie(contractorUserId, "contractor@test.com", "Contractor",
                Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of()))
            .get(QUOTATION_REQUEST_PATH)
            .then()
            .statusCode(200)
            .extract().path("items[0].id");

        final String quotationId = given()
            .when()
            .cookie(buildCookie(contractorUserId, "contractor@test.com", "Contractor",
                Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of()))
            .contentType(ContentType.JSON)
            .body("{ \"status\":\"VALID\" }")
            .post(QUOTATION_REQUEST_PATH + "/" + requestId + "/quotation")
            .then()
            .statusCode(200)
            .extract().path("id");

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(ISSUE_BASE_PATH + "/" + issueId + "/quotations/" + quotationId + "/order-placement")
            .then()
            .statusCode(201);

        return given()
            .when()
            .cookie(buildCookie(contractorUserId, "contractor@test.com", "Contractor",
                Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of()))
            .get(ORDER_PLACEMENT_PATH)
            .then()
            .statusCode(200)
            .body("items", hasSize(1))
            .extract().path("items[0].id");
    }

    // --- GET order placements ---

    @Test
    void getOrderPlacements_FAILED_noAuthentication() {
        given()
            .when()
            .get(ORDER_PLACEMENT_PATH)
            .then()
            .statusCode(401);
    }

    @Test
    void getOrderPlacements_FAILED_staffOrgRole() {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                TicketingTestData.USER_NAME,
                Map.of(), Map.of(TicketingTestData.ORGANIZATION_ID.toString(), "STAFF"), Map.of()))
            .get(ORDER_PLACEMENT_PATH)
            .then()
            .statusCode(403);
    }

    @Test
    void getOrderPlacements_FAILED_noOrgRole() {
        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "noorg@test.com", "No Org",
                Map.of(), Map.of(), Map.of()))
            .get(ORDER_PLACEMENT_PATH)
            .then()
            .statusCode(403);
    }

    @Test
    void getOrderPlacements_SUCCESS_contractorManagerSeesOwnPlacements() {
        final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
        final UUID contractorId = UUID.randomUUID();
        final UUID contractorUserId = UUID.randomUUID();

        createIssueAndPlaceOrder(contractorId, organizationId, contractorUserId);

        given()
            .when()
            .cookie(buildCookie(contractorUserId, "contractor@test.com", "Contractor",
                Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of()))
            .get(ORDER_PLACEMENT_PATH)
            .then()
            .statusCode(200)
            .body("items", hasSize(1))
            .body("items[0].status", equalTo("PLACED"))
            .body("items[0].organizationId", equalTo(organizationId.toString()));
    }

    // --- GET single order placement ---

    @Test
    void getOrderPlacement_FAILED_noAuthentication() {
        given()
            .when()
            .get(ORDER_PLACEMENT_PATH + "/" + UUID.randomUUID())
            .then()
            .statusCode(401);
    }

    @Test
    void getOrderPlacement_FAILED_notFound() {
        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "contractor@test.com", "Contractor",
                Map.of(), Map.of(TicketingTestData.ORGANIZATION_ID.toString(), "MANAGER"), Map.of()))
            .get(ORDER_PLACEMENT_PATH + "/" + UUID.randomUUID())
            .then()
            .statusCode(404);
    }

    @Test
    void getOrderPlacement_SUCCESS_returnsCorrectPlacement() {
        final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
        final UUID contractorId = UUID.randomUUID();
        final UUID contractorUserId = UUID.randomUUID();

        final String placementId = createIssueAndPlaceOrder(contractorId, organizationId, contractorUserId);

        given()
            .when()
            .cookie(buildCookie(contractorUserId, "contractor@test.com", "Contractor",
                Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of()))
            .get(ORDER_PLACEMENT_PATH + "/" + placementId)
            .then()
            .statusCode(200)
            .body("id", equalTo(placementId))
            .body("status", equalTo("PLACED"))
            .body("organizationId", equalTo(organizationId.toString()));
    }

    // --- PATCH update order placement ---

    @Test
    void updateOrderPlacement_FAILED_noAuthentication() {
        given()
            .when()
            .contentType(ContentType.JSON)
            .body("{ \"status\":\"CONFIRMED\" }")
            .patch(ORDER_PLACEMENT_PATH + "/" + UUID.randomUUID())
            .then()
            .statusCode(401);
    }

    @Test
    void updateOrderPlacement_FAILED_invalidStatus() {
        final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
        final UUID contractorId = UUID.randomUUID();
        final UUID contractorUserId = UUID.randomUUID();

        final String placementId = createIssueAndPlaceOrder(contractorId, organizationId, contractorUserId);

        given()
            .when()
            .cookie(buildCookie(contractorUserId, "contractor@test.com", "Contractor",
                Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of()))
            .contentType(ContentType.JSON)
            .body("{ \"status\":\"PLACED\" }")
            .patch(ORDER_PLACEMENT_PATH + "/" + placementId)
            .then()
            .statusCode(400);
    }

    @Test
    void updateOrderPlacement_SUCCESS_confirmOrderPlacement() {
        final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
        final UUID contractorId = UUID.randomUUID();
        final UUID contractorUserId = UUID.randomUUID();

        final String placementId = createIssueAndPlaceOrder(contractorId, organizationId, contractorUserId);

        given()
            .when()
            .cookie(buildCookie(contractorUserId, "contractor@test.com", "Contractor",
                Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of()))
            .contentType(ContentType.JSON)
            .body("{ \"status\":\"CONFIRMED\" }")
            .patch(ORDER_PLACEMENT_PATH + "/" + placementId)
            .then()
            .statusCode(200)
            .body("id", equalTo(placementId))
            .body("status", equalTo("CONFIRMED"))
            .body("confirmorId", notNullValue())
            .body("confirmedBy", equalTo("Contractor"));
    }

    @Test
    void updateOrderPlacement_SUCCESS_rejectOrderPlacement() {
        final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
        final UUID contractorId = UUID.randomUUID();
        final UUID contractorUserId = UUID.randomUUID();

        final String placementId = createIssueAndPlaceOrder(contractorId, organizationId, contractorUserId);

        given()
            .when()
            .cookie(buildCookie(contractorUserId, "contractor@test.com", "Contractor",
                Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of()))
            .contentType(ContentType.JSON)
            .body("{ \"status\":\"REJECTED\" }")
            .patch(ORDER_PLACEMENT_PATH + "/" + placementId)
            .then()
            .statusCode(200)
            .body("status", equalTo("REJECTED"));
    }

}
