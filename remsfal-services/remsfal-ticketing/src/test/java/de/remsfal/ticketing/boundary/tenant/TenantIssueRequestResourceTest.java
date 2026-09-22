package de.remsfal.ticketing.boundary.tenant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.core.model.UserContext;
import de.remsfal.core.model.UserModel;
import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.TicketingTestData;
import de.remsfal.ticketing.control.ContractorTimelineController;
import de.remsfal.ticketing.control.IssueRequestController;
import de.remsfal.ticketing.entity.dao.IssueAttachmentRepository;
import de.remsfal.ticketing.entity.dto.ContractorTimelineEntity;
import de.remsfal.ticketing.entity.dto.IssueRequestEntity;
import de.remsfal.core.json.ticketing.ImmutableIssueRequestJson;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class TenantIssueRequestResourceTest extends AbstractTicketingTest {

    static final String REQUESTS_PATH = "/ticketing/v1/tenant-relations/issues/{issueId}/requests";

    static final UUID PROJECT_ID = UUID.randomUUID();
    static final UUID AGREEMENT_ID = UUID.randomUUID();
    static final UUID ISSUE_ID_WITH_AGREEMENT = UUID.randomUUID();
    static final UUID ISSUE_ID_WITHOUT_AGREEMENT = UUID.randomUUID();
    static final UUID ISSUE_ID_OTHER_WITH_AGREEMENT = UUID.randomUUID();

    @Inject
    IssueRequestController issueRequestController;

    @Inject
    ContractorTimelineController contractorTimelineController;

    @Inject
    IssueAttachmentRepository attachmentRepository;

    private static final UserModel CONTRACTOR_USER = TicketingTestData.userModel(
        UUID.randomUUID(), "Contractor");

    @BeforeEach
    void setUpIssues() {
        insertIssue(PROJECT_ID, ISSUE_ID_WITH_AGREEMENT,
            "Tenant issue", IssueType.TASK, IssueStatus.OPEN, IssuePriority.MEDIUM,
            UUID.randomUUID(), AGREEMENT_ID, null, "Issue for tenant issue-request tests");
        insertIssue(PROJECT_ID, ISSUE_ID_WITHOUT_AGREEMENT,
            "Tenant issue without agreement", IssueType.TASK, IssueStatus.OPEN, IssuePriority.MEDIUM,
            UUID.randomUUID(), null, null, "Issue without an agreement");
        insertIssue(PROJECT_ID, ISSUE_ID_OTHER_WITH_AGREEMENT,
            "Other tenant issue", IssueType.TASK, IssueStatus.OPEN, IssuePriority.MEDIUM,
            UUID.randomUUID(), UUID.randomUUID(), null, "A different issue, also visible to a tenant");
    }

    private io.restassured.http.Cookie tenantCookie() {
        return buildCookie(UUID.randomUUID(), "tenant@example.com", "Tenant", Map.of(), Map.of(),
            Map.of(AGREEMENT_ID.toString(), PROJECT_ID.toString()));
    }

    @Test
    void getRequests_SUCCESS_returnsRequestsAcrossAllContractorOrganizations() {
        issueRequestController.createRequest(ISSUE_ID_WITH_AGREEMENT, UUID.randomUUID(), CONTRACTOR_USER,
            ImmutableIssueRequestJson.builder().message("Nachricht Firma A").build());
        issueRequestController.createRequest(ISSUE_ID_WITH_AGREEMENT, UUID.randomUUID(), CONTRACTOR_USER,
            ImmutableIssueRequestJson.builder().message("Nachricht Firma B").build());
        issueRequestController.createRequest(ISSUE_ID_OTHER_WITH_AGREEMENT, UUID.randomUUID(), CONTRACTOR_USER,
            ImmutableIssueRequestJson.builder().message("Anderes Issue").build());

        given()
            .when()
            .cookie(tenantCookie())
            .get(REQUESTS_PATH, ISSUE_ID_WITH_AGREEMENT)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("requests", hasSize(2));
    }

    @Test
    void answerRequest_SUCCESS_deletesRequestAndWritesBothTimelines() {
        final UUID organizationId = UUID.randomUUID();
        final IssueRequestEntity created = issueRequestController.createRequest(ISSUE_ID_WITH_AGREEMENT,
            organizationId, CONTRACTOR_USER, ImmutableIssueRequestJson.builder().message("Termin?").build());

        final String responseJson = "{ \"message\":\"Termin bestaetigt\" }";

        given()
            .when()
            .cookie(tenantCookie())
            .multiPart("response", responseJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(REQUESTS_PATH + "/{issueRequestId}/response", ISSUE_ID_WITH_AGREEMENT,
                created.getIssueRequestId())
            .then()
            .statusCode(204);

        given()
            .when()
            .cookie(tenantCookie())
            .get(REQUESTS_PATH, ISSUE_ID_WITH_AGREEMENT)
            .then()
            .statusCode(200)
            .body("requests", hasSize(0));

        final List<Map<String, Object>> tenantTimeline = given()
            .when()
            .cookie(tenantCookie())
            .get("/ticketing/v1/tenant-relations/issues/{issueId}/timeline", ISSUE_ID_WITH_AGREEMENT)
            .then()
            .statusCode(200)
            .extract().jsonPath().getList("timelines");

        assertEquals(1, tenantTimeline.size());
        final Map<String, Object> answerEntry = tenantTimeline.stream()
            .filter(t -> "REQUEST_ANSWERED".equals(t.get("purpose")))
            .findFirst().orElseThrow();
        assertEquals("Termin bestaetigt", answerEntry.get("message"));

        final List<ContractorTimelineEntity> contractorTimeline =
            contractorTimelineController.getTimelineEntries(ISSUE_ID_WITH_AGREEMENT, organizationId);
        assertEquals(2, contractorTimeline.size());
        final ContractorTimelineEntity contractorAnswerEntry = contractorTimeline.stream()
            .filter(e -> MessagePurpose.REQUEST_ANSWERED.equals(e.getPurpose()))
            .findFirst().orElseThrow();
        assertEquals("Termin bestaetigt", contractorAnswerEntry.getMessage());
        assertEquals(UserContext.TENANT, contractorAnswerEntry.getSenderRole());
    }

    @Test
    void answerRequest_FAILED_missingMessageField_returns400() {
        final IssueRequestEntity created = issueRequestController.createRequest(ISSUE_ID_WITH_AGREEMENT,
            UUID.randomUUID(), CONTRACTOR_USER, ImmutableIssueRequestJson.builder().message("Termin?").build());

        given()
            .when()
            .cookie(tenantCookie())
            .multiPart("response", "{ }", MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(REQUESTS_PATH + "/{issueRequestId}/response", ISSUE_ID_WITH_AGREEMENT,
                created.getIssueRequestId())
            .then()
            .statusCode(400);
    }

    @Test
    void answerRequest_FAILED_noMatchingQuotationRequest_rollsBackUploadedAttachment() {
        // No QuotationRequestEntity exists for this organization, so the order-copy step inside
        // answerRequest fails and the attachment uploaded during this call must not be left behind.
        final UUID organizationId = UUID.randomUUID();
        final IssueRequestEntity created = issueRequestController.createRequest(ISSUE_ID_WITH_AGREEMENT,
            organizationId, CONTRACTOR_USER, ImmutableIssueRequestJson.builder().message("Foto bitte").build());

        final String responseJson = "{ \"message\":\"Hier das Foto\" }";

        given()
            .when()
            .cookie(tenantCookie())
            .multiPart("response", responseJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .multiPart("attachment", "photo.jpg", "fake-image-bytes".getBytes(), "image/jpeg")
            .post(REQUESTS_PATH + "/{issueRequestId}/response", ISSUE_ID_WITH_AGREEMENT,
                created.getIssueRequestId())
            .then()
            .statusCode(404);

        assertTrue(attachmentRepository.findByIssueId(ISSUE_ID_WITH_AGREEMENT).isEmpty());
    }

    @Test
    void answerRequest_FAILED_unknownIssueRequestId_returns404() {
        final String responseJson = "{ \"message\":\"Termin bestaetigt\" }";

        given()
            .when()
            .cookie(tenantCookie())
            .multiPart("response", responseJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(REQUESTS_PATH + "/{issueRequestId}/response", ISSUE_ID_WITH_AGREEMENT, UUID.randomUUID())
            .then()
            .statusCode(404);
    }

    @Test
    void answerRequest_FAILED_issueHasNoAgreement_returns403() {
        final String responseJson = "{ \"message\":\"Termin bestaetigt\" }";

        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "tenant@example.com", "Tenant", Map.of(), Map.of(),
                Map.of()))
            .multiPart("response", responseJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(REQUESTS_PATH + "/{issueRequestId}/response", ISSUE_ID_WITHOUT_AGREEMENT, UUID.randomUUID())
            .then()
            .statusCode(403);
    }

    @Test
    void answerRequest_FAILED_unauthenticated_returns401() {
        final String responseJson = "{ \"message\":\"Termin bestaetigt\" }";

        given()
            .when()
            .multiPart("response", responseJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(REQUESTS_PATH + "/{issueRequestId}/response", ISSUE_ID_WITH_AGREEMENT, UUID.randomUUID())
            .then()
            .statusCode(401);
    }

    @Test
    void getRequests_FAILED_issueHasNoAgreement_returns403() {
        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "tenant@example.com", "Tenant", Map.of(), Map.of(),
                Map.of()))
            .get(REQUESTS_PATH, ISSUE_ID_WITHOUT_AGREEMENT)
            .then()
            .statusCode(403);
    }

    @Test
    void getRequests_FAILED_tenantNotOnThisAgreement_returns403() {
        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "other-tenant@example.com", "Other Tenant",
                Map.of(), Map.of(), Map.of(UUID.randomUUID().toString(), PROJECT_ID.toString())))
            .get(REQUESTS_PATH, ISSUE_ID_WITH_AGREEMENT)
            .then()
            .statusCode(403);
    }

    @Test
    void getRequests_FAILED_tenancyProjectMismatch_returns403() {
        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "other-tenant@example.com", "Other Tenant",
                Map.of(), Map.of(), Map.of(AGREEMENT_ID.toString(), UUID.randomUUID().toString())))
            .get(REQUESTS_PATH, ISSUE_ID_WITH_AGREEMENT)
            .then()
            .statusCode(403);
    }

    @Test
    void getRequests_FAILED_managerCannotUseTenantEndpoint_returns403() {
        given()
            .when()
            .cookie(buildManagerCookie(Map.of(PROJECT_ID.toString(), "MANAGER")))
            .get(REQUESTS_PATH, ISSUE_ID_WITH_AGREEMENT)
            .then()
            .statusCode(403);
    }

    @Test
    void getRequests_FAILED_unknownIssueId_returns404() {
        given()
            .when()
            .cookie(tenantCookie())
            .get(REQUESTS_PATH, UUID.randomUUID())
            .then()
            .statusCode(404);
    }

    @Test
    void getRequests_FAILED_unauthenticated_returns401() {
        given()
            .when()
            .get(REQUESTS_PATH, ISSUE_ID_WITH_AGREEMENT)
            .then()
            .statusCode(401);
    }
}
