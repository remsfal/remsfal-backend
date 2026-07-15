package de.remsfal.ticketing.boundary.tenant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.core.model.ticketing.tenant.MessagePurpose;
import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.TicketingTestData;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class TenantTimelineResourceTest extends AbstractTicketingTest {

    static final String TIMELINE_PATH = "/ticketing/v1/tenant-relations/issues/{issueId}/timeline";

    static final UUID PROJECT_ID = UUID.randomUUID();
    static final UUID AGREEMENT_ID = UUID.randomUUID();
    static final UUID ISSUE_ID_WITH_AGREEMENT = UUID.randomUUID();

    static final UUID PROJECT_ID_NO_AGREEMENT = UUID.randomUUID();
    static final UUID ISSUE_ID_NO_AGREEMENT = UUID.randomUUID();

    @BeforeEach
    void setUpIssues() {
        insertIssue(PROJECT_ID, ISSUE_ID_WITH_AGREEMENT,
            "Tenant issue", IssueType.TASK, IssueStatus.OPEN, IssuePriority.MEDIUM,
            UUID.randomUUID(), AGREEMENT_ID, null, "Issue for tenant timeline tests");

        insertIssue(PROJECT_ID_NO_AGREEMENT, ISSUE_ID_NO_AGREEMENT,
            "Manager issue", IssueType.TASK, IssueStatus.OPEN, IssuePriority.MEDIUM,
            UUID.randomUUID(), null, null, "Issue without agreement");
    }

    @Test
    void getTimelineEntries_SUCCESS_forTenantIssue() {
        insertTimelineEntry(ISSUE_ID_WITH_AGREEMENT, PROJECT_ID, AGREEMENT_ID, UUID.randomUUID(),
            MessagePurpose.MESSAGE_SENT, null);
        insertTimelineEntry(ISSUE_ID_WITH_AGREEMENT, PROJECT_ID, AGREEMENT_ID, UUID.randomUUID(),
            MessagePurpose.STATUS_CHANGED, null);
        insertTimelineEntry(UUID.randomUUID(), PROJECT_ID, AGREEMENT_ID, UUID.randomUUID(),
            MessagePurpose.MESSAGE_SENT, null);

        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "tenant@example.com", "Tenant", Map.of(), Map.of(),
                Map.of(AGREEMENT_ID.toString(), PROJECT_ID.toString())))
            .get(TIMELINE_PATH, ISSUE_ID_WITH_AGREEMENT)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("timelines", hasSize(2));
    }

    @Test
    void getTimelineEntries_SUCCESS_issueWithoutAgreementReturnsEmptyList() {
        given()
            .when()
            .cookie(buildManagerCookie(Map.of(PROJECT_ID_NO_AGREEMENT.toString(), "MANAGER")))
            .get(TIMELINE_PATH, ISSUE_ID_NO_AGREEMENT)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("timelines", hasSize(0));
    }

    @Test
    void createTimelineEntryWithAttachments_SUCCESS_withoutAttachments() {
        final String timelineJson = "{"
            + "\"purpose\":\"MESSAGE_SENT\","
            + "\"message\":\"Bitte um Rueckmeldung\""
            + "}";

        given()
            .when()
            .cookie(buildManagerCookie(Map.of(PROJECT_ID.toString(), "MANAGER")))
            .multiPart("timeline", timelineJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(TIMELINE_PATH, ISSUE_ID_WITH_AGREEMENT)
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .header("location",
                containsString("/ticketing/v1/tenant-relations/issues/" + ISSUE_ID_WITH_AGREEMENT + "/timeline/"))
            .body("timelineId", notNullValue())
            .body("issueId", equalTo(ISSUE_ID_WITH_AGREEMENT.toString()))
            .body("tenancyId", equalTo(AGREEMENT_ID.toString()))
            .body("purpose", equalTo("MESSAGE_SENT"))
            .body("message", equalTo("Bitte um Rueckmeldung"));
    }

    @Test
    void createTimelineEntryWithAttachments_FAILED_missingTimelinePart() {
        given()
            .when()
            .cookie(buildManagerCookie(Map.of(PROJECT_ID.toString(), "MANAGER")))
            .multiPart("notTimeline", "{}", MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(TIMELINE_PATH, ISSUE_ID_WITH_AGREEMENT)
            .then()
            .statusCode(400);
    }

    @Test
    void createTimelineEntryWithAttachments_SUCCESS_uploadedAttachmentIsLinkedAndVisible() {
        final String timelineJson = "{"
            + "\"purpose\":\"MESSAGE_SENT\","
            + "\"message\":\"Bitte um Rueckmeldung\""
            + "}";
        final InputStream attachmentStream = getTestFileStream(TicketingTestData.ATTACHMENT_FILE_PATH_1);

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                TicketingTestData.USER_NAME, Map.of(), Map.of(),
                Map.of(AGREEMENT_ID.toString(), PROJECT_ID.toString())))
            .multiPart("timeline", timelineJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .multiPart("attachment", TicketingTestData.ATTACHMENT_FILE_PATH_1,
                attachmentStream, TicketingTestData.ATTACHMENT_FILE_TYPE_1)
            .post(TIMELINE_PATH, ISSUE_ID_WITH_AGREEMENT)
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("attachments", hasSize(1))
            .body("attachments[0].fileName", equalTo(TicketingTestData.ATTACHMENT_FILE_PATH_1));

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                TicketingTestData.USER_NAME, Map.of(), Map.of(),
                Map.of(AGREEMENT_ID.toString(), PROJECT_ID.toString())))
            .get(TIMELINE_PATH, ISSUE_ID_WITH_AGREEMENT)
            .then()
            .statusCode(200)
            .body("timelines", hasSize(1))
            .body("timelines[0].attachments", hasSize(1));
    }

    @Test
    void createTimelineEntryWithAttachments_FAILED_cannotReferenceForeignAttachment() {
        // an attachment uploaded by someone else (e.g. a manager) for the same issue
        final UUID foreignAttachmentId = UUID.randomUUID();
        insertAttachment(ISSUE_ID_WITH_AGREEMENT, foreignAttachmentId, TicketingTestData.ATTACHMENT_FILE_PATH_2,
            TicketingTestData.ATTACHMENT_FILE_TYPE_2, "/issues/" + ISSUE_ID_WITH_AGREEMENT + "/attachments/"
                + foreignAttachmentId + "/" + TicketingTestData.ATTACHMENT_FILE_PATH_2, UUID.randomUUID());

        final String timelineJson = "{"
            + "\"purpose\":\"MESSAGE_SENT\","
            + "\"message\":\"Bitte um Rueckmeldung\","
            + "\"attachments\":[{\"attachmentId\":\"" + foreignAttachmentId + "\"}]"
            + "}";

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                TicketingTestData.USER_NAME, Map.of(), Map.of(),
                Map.of(AGREEMENT_ID.toString(), PROJECT_ID.toString())))
            .multiPart("timeline", timelineJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(TIMELINE_PATH, ISSUE_ID_WITH_AGREEMENT)
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("attachments", hasSize(0));
    }

}