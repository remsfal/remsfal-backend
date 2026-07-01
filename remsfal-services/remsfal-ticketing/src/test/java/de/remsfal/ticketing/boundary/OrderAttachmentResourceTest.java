package de.remsfal.ticketing.boundary;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.TicketingTestData;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class OrderAttachmentResourceTest extends AbstractTicketingTest {

    static final String ISSUE_BASE_PATH = "/ticketing/v1/issues";
    static final String QUOTATION_REQUEST_PATH = "/ticketing/v1/order-management/quotation-requests";
    static final String QUOTATION_PATH = "/ticketing/v1/order-management/quotations";
    static final String ORDER_PLACEMENT_PATH = "/ticketing/v1/order-management/order-placements";

    private record Flow(String issueId, String requestId, String quotationId, String placementId,
        UUID organizationId, UUID contractorUserId) {
    }

    private Flow createQuotationRequestFlow() {
        final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
        final UUID contractorId = UUID.randomUUID();
        final UUID contractorUserId = UUID.randomUUID();

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

        return new Flow(issueId, requestId, null, null, organizationId, contractorUserId);
    }

    private Flow createQuotationFlow() {
        final Flow requestFlow = createQuotationRequestFlow();

        final String quotationId = given()
            .when()
            .cookie(buildCookie(requestFlow.contractorUserId(), "contractor@test.com", "Contractor",
                Map.of(), Map.of(requestFlow.organizationId().toString(), "MANAGER"), Map.of()))
            .contentType(ContentType.JSON)
            .body("{ \"status\":\"VALID\" }")
            .post(QUOTATION_REQUEST_PATH + "/" + requestFlow.requestId() + "/quotation")
            .then()
            .statusCode(200)
            .extract().path("id");

        return new Flow(requestFlow.issueId(), requestFlow.requestId(), quotationId, null,
            requestFlow.organizationId(), requestFlow.contractorUserId());
    }

    private Flow createOrderPlacementFlow() {
        final Flow quotationFlow = createQuotationFlow();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(ISSUE_BASE_PATH + "/" + quotationFlow.issueId() + "/quotations/"
                + quotationFlow.quotationId() + "/order-placement")
            .then()
            .statusCode(201);

        final String placementId = given()
            .when()
            .cookie(buildCookie(quotationFlow.contractorUserId(), "contractor@test.com", "Contractor",
                Map.of(), Map.of(quotationFlow.organizationId().toString(), "MANAGER"), Map.of()))
            .get(ORDER_PLACEMENT_PATH)
            .then()
            .statusCode(200)
            .extract().path("items[0].id");

        return new Flow(quotationFlow.issueId(), quotationFlow.requestId(), quotationFlow.quotationId(),
            placementId, quotationFlow.organizationId(), quotationFlow.contractorUserId());
    }

    // --- QuotationRequest: upload via manager, download/delete via contractor ---

    @Test
    void quotationRequestAttachment_SUCCESS_uploadViaManagerVisibleToContractor() {
        final Flow flow = createQuotationRequestFlow();

        final String attachmentId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .multiPart("attachment", TicketingTestData.ATTACHMENT_FILE_PATH_1,
                getTestFileStream(TicketingTestData.ATTACHMENT_FILE_PATH_1),
                TicketingTestData.ATTACHMENT_FILE_TYPE_1)
            .post(ISSUE_BASE_PATH + "/" + flow.issueId() + "/quotation-request/" + flow.requestId() + "/attachments")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("[0].processPhase", equalTo("QUOTATION_REQUEST"))
            .body("[0].processId", equalTo(flow.requestId()))
            .body("[0].fileName", equalTo(TicketingTestData.ATTACHMENT_FILE_PATH_1))
            .extract().path("[0].attachmentId");

        given()
            .when()
            .cookie(buildCookie(flow.contractorUserId(), "contractor@test.com", "Contractor",
                Map.of(), Map.of(flow.organizationId().toString(), "MANAGER"), Map.of()))
            .get(QUOTATION_REQUEST_PATH + "/" + flow.requestId() + "/attachments/"
                + attachmentId + "/" + TicketingTestData.ATTACHMENT_FILE_PATH_1)
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", containsString(TicketingTestData.ATTACHMENT_FILE_PATH_1));

        given()
            .when()
            .cookie(buildCookie(flow.contractorUserId(), "contractor@test.com", "Contractor",
                Map.of(), Map.of(flow.organizationId().toString(), "MANAGER"), Map.of()))
            .delete(QUOTATION_REQUEST_PATH + "/" + flow.requestId() + "/attachments/" + attachmentId)
            .then()
            .statusCode(204);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(ISSUE_BASE_PATH + "/" + flow.issueId() + "/quotation-request/" + flow.requestId()
                + "/attachments/" + attachmentId + "/" + TicketingTestData.ATTACHMENT_FILE_PATH_1)
            .then()
            .statusCode(404);
    }

    @Test
    void quotationRequestAttachment_FAILED_noAuthentication() {
        final Flow flow = createQuotationRequestFlow();

        given()
            .when()
            .multiPart("attachment", TicketingTestData.ATTACHMENT_FILE_PATH_1,
                getTestFileStream(TicketingTestData.ATTACHMENT_FILE_PATH_1),
                TicketingTestData.ATTACHMENT_FILE_TYPE_1)
            .post(ISSUE_BASE_PATH + "/" + flow.issueId() + "/quotation-request/" + flow.requestId() + "/attachments")
            .then()
            .statusCode(401);
    }

    @Test
    void quotationRequestAttachment_FAILED_contractorFromOtherOrganization() {
        final Flow flow = createQuotationRequestFlow();
        final UUID otherOrgId = UUID.randomUUID();

        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "other@test.com", "Other Contractor",
                Map.of(), Map.of(otherOrgId.toString(), "MANAGER"), Map.of()))
            .multiPart("attachment", TicketingTestData.ATTACHMENT_FILE_PATH_1,
                getTestFileStream(TicketingTestData.ATTACHMENT_FILE_PATH_1),
                TicketingTestData.ATTACHMENT_FILE_TYPE_1)
            .post(QUOTATION_REQUEST_PATH + "/" + flow.requestId() + "/attachments")
            .then()
            .statusCode(404);
    }

    @Test
    void quotationRequestAttachment_FAILED_noOrganizationRole() {
        final Flow flow = createQuotationRequestFlow();

        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "unauthorized@test.com",
                "Unauthorized", Map.of(), Map.of(), Map.of()))
            .multiPart("attachment", TicketingTestData.ATTACHMENT_FILE_PATH_1,
                getTestFileStream(TicketingTestData.ATTACHMENT_FILE_PATH_1),
                TicketingTestData.ATTACHMENT_FILE_TYPE_1)
            .post(QUOTATION_REQUEST_PATH + "/" + flow.requestId() + "/attachments")
            .then()
            .statusCode(403);
    }

    // --- Quotation: upload via contractor, download/delete via manager ---

    @Test
    void quotationAttachment_SUCCESS_uploadViaContractorVisibleToManager() {
        final Flow flow = createQuotationFlow();

        final String attachmentId = given()
            .when()
            .cookie(buildCookie(flow.contractorUserId(), "contractor@test.com", "Contractor",
                Map.of(), Map.of(flow.organizationId().toString(), "MANAGER"), Map.of()))
            .multiPart("attachment", TicketingTestData.ATTACHMENT_FILE_PATH_2,
                getTestFileStream(TicketingTestData.ATTACHMENT_FILE_PATH_2),
                TicketingTestData.ATTACHMENT_FILE_TYPE_2)
            .post(QUOTATION_PATH + "/" + flow.quotationId() + "/attachments")
            .then()
            .statusCode(200)
            .body("[0].processPhase", equalTo("QUOTATION"))
            .body("[0].processId", equalTo(flow.quotationId()))
            .extract().path("[0].attachmentId");

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(ISSUE_BASE_PATH + "/" + flow.issueId() + "/quotations/" + flow.quotationId() + "/attachments/"
                + attachmentId + "/" + TicketingTestData.ATTACHMENT_FILE_PATH_2)
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_OCTET_STREAM);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(ISSUE_BASE_PATH + "/" + flow.issueId() + "/quotations/" + flow.quotationId())
            .then()
            .statusCode(200)
            .body("attachments[0].attachmentId", equalTo(attachmentId))
            .body("attachments[0].fileName", equalTo(TicketingTestData.ATTACHMENT_FILE_PATH_2));

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(ISSUE_BASE_PATH + "/" + flow.issueId() + "/quotations/" + flow.quotationId()
                + "/attachments/" + attachmentId)
            .then()
            .statusCode(204);

        given()
            .when()
            .cookie(buildCookie(flow.contractorUserId(), "contractor@test.com", "Contractor",
                Map.of(), Map.of(flow.organizationId().toString(), "MANAGER"), Map.of()))
            .get(QUOTATION_PATH + "/" + flow.quotationId() + "/attachments/"
                + attachmentId + "/" + TicketingTestData.ATTACHMENT_FILE_PATH_2)
            .then()
            .statusCode(404);
    }

    @Test
    void quotationAttachment_FAILED_notFound() {
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(ISSUE_BASE_PATH + "/" + UUID.randomUUID() + "/quotations/" + UUID.randomUUID()
                + "/attachments/" + UUID.randomUUID())
            .then()
            .statusCode(404);
    }

    // --- OrderPlacement: manager path (keyed by quotationId in the URL) must resolve to the
    // same placement the contractor path (keyed by the placement's own id) sees ---

    @Test
    void orderPlacementAttachment_SUCCESS_managerPathResolvesToContractorPlacement() {
        final Flow flow = createOrderPlacementFlow();

        final String attachmentId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .multiPart("attachment", TicketingTestData.ATTACHMENT_FILE_PATH_3,
                getTestFileStream(TicketingTestData.ATTACHMENT_FILE_PATH_3),
                TicketingTestData.ATTACHMENT_FILE_TYPE_3)
            .post(ISSUE_BASE_PATH + "/" + flow.issueId() + "/quotations/" + flow.quotationId()
                + "/order-placement/attachments")
            .then()
            .statusCode(200)
            .body("[0].processPhase", equalTo("ORDER_PLACEMENT"))
            .body("[0].processId", equalTo(flow.placementId()))
            .extract().path("[0].attachmentId");

        given()
            .when()
            .cookie(buildCookie(flow.contractorUserId(), "contractor@test.com", "Contractor",
                Map.of(), Map.of(flow.organizationId().toString(), "MANAGER"), Map.of()))
            .get(ORDER_PLACEMENT_PATH + "/" + flow.placementId() + "/attachments/"
                + attachmentId + "/" + TicketingTestData.ATTACHMENT_FILE_PATH_3)
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_OCTET_STREAM);

        given()
            .when()
            .cookie(buildCookie(flow.contractorUserId(), "contractor@test.com", "Contractor",
                Map.of(), Map.of(flow.organizationId().toString(), "MANAGER"), Map.of()))
            .get(ORDER_PLACEMENT_PATH + "/" + flow.placementId())
            .then()
            .statusCode(200)
            .body("attachments[0].attachmentId", equalTo(attachmentId));

        given()
            .when()
            .cookie(buildCookie(flow.contractorUserId(), "contractor@test.com", "Contractor",
                Map.of(), Map.of(flow.organizationId().toString(), "MANAGER"), Map.of()))
            .delete(ORDER_PLACEMENT_PATH + "/" + flow.placementId() + "/attachments/" + attachmentId)
            .then()
            .statusCode(204);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(ISSUE_BASE_PATH + "/" + flow.issueId() + "/quotations/" + flow.quotationId()
                + "/order-placement/attachments/" + attachmentId + "/" + TicketingTestData.ATTACHMENT_FILE_PATH_3)
            .then()
            .statusCode(404);
    }

    @Test
    void orderPlacementAttachment_FAILED_managerWithoutProjectPermission() {
        final Flow flow = createOrderPlacementFlow();
        final Map<String, String> otherProjectRoles = Map.of(UUID.randomUUID().toString(), "MANAGER");

        given()
            .when()
            .cookie(buildManagerCookie(otherProjectRoles))
            .multiPart("attachment", TicketingTestData.ATTACHMENT_FILE_PATH_3,
                getTestFileStream(TicketingTestData.ATTACHMENT_FILE_PATH_3),
                TicketingTestData.ATTACHMENT_FILE_TYPE_3)
            .post(ISSUE_BASE_PATH + "/" + flow.issueId() + "/quotations/" + flow.quotationId()
                + "/order-placement/attachments")
            .then()
            .statusCode(403);
    }

    @Test
    void attachment_FAILED_downloadNotFound() {
        final Flow flow = createQuotationRequestFlow();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(ISSUE_BASE_PATH + "/" + flow.issueId() + "/quotation-request/" + flow.requestId()
                + "/attachments/" + UUID.randomUUID() + "/test.png")
            .then()
            .statusCode(404);
    }

}
