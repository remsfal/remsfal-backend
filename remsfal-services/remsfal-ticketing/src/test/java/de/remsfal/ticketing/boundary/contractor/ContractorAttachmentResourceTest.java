package de.remsfal.ticketing.boundary.contractor;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.TicketingTestData;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import jakarta.ws.rs.core.MediaType;

/**
 * Attachments are stored once per issue and shared with contractors only by reference, either from the
 * contractor timeline or from a quotation request, quotation or order placement of their organization.
 */
@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class ContractorAttachmentResourceTest extends AbstractTicketingTest {

    static final String ISSUE_BASE_PATH = "/ticketing/v1/issues";
    static final String ORDER_MANAGEMENT_PATH = "/ticketing/v1/order-management";
    static final String QUOTATION_REQUEST_PATH = ORDER_MANAGEMENT_PATH + "/quotation-requests";
    static final String ORDER_PLACEMENT_PATH = ORDER_MANAGEMENT_PATH + "/order-placements";

    final UUID organizationA = UUID.randomUUID();
    final UUID organizationB = UUID.randomUUID();
    final UUID organizationC = UUID.randomUUID();

    String issueId;

    @BeforeEach
    void setUpIssue() {
        issueId = given()
            .when()
            .cookie(managerCookie())
            .contentType(ContentType.JSON)
            .body("{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
                + "\"type\":\"TASK\","
                + "\"visibleToTenants\":false"
                + "}")
            .post(ISSUE_BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");
    }

    private Cookie managerCookie() {
        return buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES);
    }

    private Cookie contractorCookie(final UUID organizationId) {
        return buildCookie(UUID.randomUUID(), "contractor@test.com", "Contractor",
            Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of());
    }

    private String contractorJson(final UUID organizationId) {
        return "{\"id\":\"" + UUID.randomUUID() + "\",\"name\":\"Betrieb\",\"organizationId\":\""
            + organizationId + "\"}";
    }

    private String uploadManagerAttachment() {
        return given()
            .when()
            .cookie(managerCookie())
            .multiPart("attachment", TicketingTestData.ATTACHMENT_FILE_PATH_1,
                getTestFileStream(TicketingTestData.ATTACHMENT_FILE_PATH_1),
                TicketingTestData.ATTACHMENT_FILE_TYPE_1)
            .post(ISSUE_BASE_PATH + "/" + issueId + "/attachments")
            .then()
            .statusCode(200)
            .body("[0].uploaderContext", equalTo("MANAGER"))
            .extract().path("[0].attachmentId");
    }

    private void requestQuotation(final String body) {
        given()
            .when()
            .cookie(managerCookie())
            .contentType(ContentType.JSON)
            .body(body)
            .post(ISSUE_BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);
    }

    private String contractorDownloadPath(final String attachmentId, final String fileName) {
        return ORDER_MANAGEMENT_PATH + "/" + issueId + "/attachments/" + attachmentId + "/" + fileName;
    }

    private String contractorRequestId(final UUID organizationId) {
        return given()
            .when()
            .cookie(contractorCookie(organizationId))
            .get(QUOTATION_REQUEST_PATH)
            .then()
            .statusCode(200)
            .extract().path("items[0].id");
    }

    private String uploadViaContractorTimeline(final UUID organizationId) {
        return given()
            .when()
            .cookie(contractorCookie(organizationId))
            .multiPart("timeline", "{\"purpose\":\"MESSAGE_SENT\",\"message\":\"Angebot anbei\"}",
                MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .multiPart("attachment", TicketingTestData.ATTACHMENT_FILE_PATH_2,
                getTestFileStream(TicketingTestData.ATTACHMENT_FILE_PATH_2),
                TicketingTestData.ATTACHMENT_FILE_TYPE_2)
            .post(ORDER_MANAGEMENT_PATH + "/" + issueId + "/timeline")
            .then()
            .statusCode(201)
            .extract().path("attachments[0].attachmentId");
    }

    @Test
    void quotationRequest_SUCCESS_sharedAttachmentIsReferencedNotCopied() {
        final String attachmentId = uploadManagerAttachment();

        requestQuotation("{ \"contractors\":[" + contractorJson(organizationA) + ","
            + contractorJson(organizationB) + "," + contractorJson(organizationC) + "],"
            + "\"attachmentIds\":[\"" + attachmentId + "\"] }");

        given()
            .when()
            .cookie(managerCookie())
            .get(ISSUE_BASE_PATH + "/" + issueId)
            .then()
            .statusCode(200)
            .body("attachments", hasSize(1))
            .body("attachments[0].attachmentId", equalTo(attachmentId));

        final String requestId = contractorRequestId(organizationA);
        given()
            .when()
            .cookie(managerCookie())
            .get(ISSUE_BASE_PATH + "/" + issueId + "/quotation-request/" + requestId)
            .then()
            .statusCode(200)
            .body("attachments", hasSize(1))
            .body("attachments[0].attachmentId", equalTo(attachmentId));

        for (final UUID organizationId : new UUID[] { organizationA, organizationB, organizationC }) {
            given()
                .when()
                .cookie(contractorCookie(organizationId))
                .get(contractorDownloadPath(attachmentId, TicketingTestData.ATTACHMENT_FILE_PATH_1))
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", containsString(TicketingTestData.ATTACHMENT_FILE_PATH_1));
        }
    }

    @Test
    void download_FAILED_attachmentNotSharedWithOrganization() {
        final String attachmentId = uploadManagerAttachment();
        requestQuotation("{ \"contractors\":[" + contractorJson(organizationA) + "],"
            + "\"attachmentIds\":[\"" + attachmentId + "\"] }");
        requestQuotation("{ \"contractors\":[" + contractorJson(organizationB) + "] }");

        given()
            .when()
            .cookie(contractorCookie(organizationB))
            .get(contractorDownloadPath(attachmentId, TicketingTestData.ATTACHMENT_FILE_PATH_1))
            .then()
            .statusCode(403);
    }

    @Test
    void download_FAILED_attachmentNeverShared() {
        requestQuotation("{ \"contractors\":[" + contractorJson(organizationA) + "] }");
        final String attachmentId = uploadManagerAttachment();

        given()
            .when()
            .cookie(contractorCookie(organizationA))
            .get(contractorDownloadPath(attachmentId, TicketingTestData.ATTACHMENT_FILE_PATH_1))
            .then()
            .statusCode(403);
    }

    @Test
    void download_FAILED_organizationWithoutQuotationRequest() {
        final String attachmentId = uploadManagerAttachment();
        requestQuotation("{ \"contractors\":[" + contractorJson(organizationA) + "],"
            + "\"attachmentIds\":[\"" + attachmentId + "\"] }");

        given()
            .when()
            .cookie(contractorCookie(organizationC))
            .get(contractorDownloadPath(attachmentId, TicketingTestData.ATTACHMENT_FILE_PATH_1))
            .then()
            .statusCode(404);
    }

    @Test
    void download_FAILED_unauthenticated() {
        given()
            .when()
            .get(contractorDownloadPath(UUID.randomUUID().toString(), "test.png"))
            .then()
            .statusCode(401);
    }

    @Test
    void downloadUrl_SUCCESS_pointsToRoleSpecificDownloadEndpoint() {
        requestQuotation("{ \"contractors\":[" + contractorJson(organizationA) + "] }");
        final String attachmentId = uploadViaContractorTimeline(organizationA);

        final String contractorUrl = given()
            .when()
            .cookie(contractorCookie(organizationA))
            .get(ORDER_MANAGEMENT_PATH + "/" + issueId + "/timeline")
            .then()
            .statusCode(200)
            .extract().path("timelines.find { it.attachments.size() > 0 }.attachments[0].downloadUrl");
        assertEquals(contractorDownloadPath(attachmentId, TicketingTestData.ATTACHMENT_FILE_PATH_2), contractorUrl);
        given()
            .when()
            .cookie(contractorCookie(organizationA))
            .get(contractorUrl)
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_OCTET_STREAM);

        final String managerUrl = given()
            .when()
            .cookie(managerCookie())
            .get(ISSUE_BASE_PATH + "/" + issueId)
            .then()
            .statusCode(200)
            .body("attachments[0].objectName", nullValue())
            .body("attachments[0].uploaderId", nullValue())
            .extract().path("attachments[0].downloadUrl");
        assertEquals(ISSUE_BASE_PATH + "/" + issueId + "/attachments/" + attachmentId + "/"
            + TicketingTestData.ATTACHMENT_FILE_PATH_2, managerUrl);
        given()
            .when()
            .cookie(managerCookie())
            .get(managerUrl)
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_OCTET_STREAM);
    }

    @Test
    void timelineUpload_SUCCESS_downloadableByContractorAndVisibleToManager() {
        requestQuotation("{ \"contractors\":[" + contractorJson(organizationA) + "] }");
        final String attachmentId = uploadViaContractorTimeline(organizationA);

        given()
            .when()
            .cookie(contractorCookie(organizationA))
            .get(contractorDownloadPath(attachmentId, TicketingTestData.ATTACHMENT_FILE_PATH_2))
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_OCTET_STREAM);

        given()
            .when()
            .cookie(managerCookie())
            .get(ISSUE_BASE_PATH + "/" + issueId)
            .then()
            .statusCode(200)
            .body("attachments", hasSize(1))
            .body("attachments[0].attachmentId", equalTo(attachmentId))
            .body("attachments[0].uploaderContext", equalTo("CONTRACTOR"));

        given()
            .when()
            .cookie(managerCookie())
            .get(ISSUE_BASE_PATH + "/" + issueId + "/attachments/" + attachmentId + "/"
                + TicketingTestData.ATTACHMENT_FILE_PATH_2)
            .then()
            .statusCode(200);
    }

    @Test
    void quotationAndOrder_SUCCESS_referenceContractorTimelineAttachment() {
        requestQuotation("{ \"contractors\":[" + contractorJson(organizationA) + "] }");
        final String requestId = contractorRequestId(organizationA);
        final String attachmentId = uploadViaContractorTimeline(organizationA);

        final String quotationId = given()
            .when()
            .cookie(contractorCookie(organizationA))
            .contentType(ContentType.JSON)
            .body("{ \"status\":\"VALID\", \"attachmentIds\":[\"" + attachmentId + "\"] }")
            .post(QUOTATION_REQUEST_PATH + "/" + requestId + "/quotation")
            .then()
            .statusCode(200)
            .body("attachments", hasSize(1))
            .body("attachments[0].attachmentId", equalTo(attachmentId))
            .extract().path("id");

        given()
            .when()
            .cookie(managerCookie())
            .get(ISSUE_BASE_PATH + "/" + issueId + "/quotations/" + quotationId)
            .then()
            .statusCode(200)
            .body("attachments[0].attachmentId", equalTo(attachmentId));

        given()
            .when()
            .cookie(managerCookie())
            .post(ISSUE_BASE_PATH + "/" + issueId + "/quotations/" + quotationId + "/orders")
            .then()
            .statusCode(201)
            .body("attachments[0].attachmentId", equalTo(attachmentId));

        final String placementId = given()
            .when()
            .cookie(contractorCookie(organizationA))
            .get(ORDER_PLACEMENT_PATH)
            .then()
            .statusCode(200)
            .body("items", hasSize(1))
            .extract().path("items[0].id");

        given()
            .when()
            .cookie(contractorCookie(organizationA))
            .get(ORDER_PLACEMENT_PATH + "/" + placementId)
            .then()
            .statusCode(200)
            .body("attachments[0].attachmentId", equalTo(attachmentId));
    }

    @Test
    void quotation_FAILED_referencesAttachmentNotSharedWithOrganization() {
        final String attachmentId = uploadManagerAttachment();
        requestQuotation("{ \"contractors\":[" + contractorJson(organizationA) + "] }");
        final String requestId = contractorRequestId(organizationA);

        given()
            .when()
            .cookie(contractorCookie(organizationA))
            .contentType(ContentType.JSON)
            .body("{ \"status\":\"VALID\", \"attachmentIds\":[\"" + attachmentId + "\"] }")
            .post(QUOTATION_REQUEST_PATH + "/" + requestId + "/quotation")
            .then()
            .statusCode(400);
    }

}
