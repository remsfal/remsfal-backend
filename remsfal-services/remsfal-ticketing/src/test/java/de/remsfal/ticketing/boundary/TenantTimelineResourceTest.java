package de.remsfal.ticketing.boundary;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.entity.dto.TenantTimelineEntity;
import de.remsfal.ticketing.entity.dto.TenantTimelineKey;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class TenantTimelineResourceTest extends AbstractTicketingTest {

    static final String TIMELINE_PATH = "/ticketing/v1/tenant-relations/issues/{issueId}/timelines";

    static final UUID PROJECT_ID = UUID.randomUUID();
    static final UUID AGREEMENT_ID = UUID.randomUUID();
    static final UUID ISSUE_ID_WITH_AGREEMENT = UUID.randomUUID();

    static final UUID PROJECT_ID_NO_AGREEMENT = UUID.randomUUID();
    static final UUID ISSUE_ID_NO_AGREEMENT = UUID.randomUUID();

    @Inject
    CqlSession cqlSession;

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
        insertTimelineEntry(ISSUE_ID_WITH_AGREEMENT, PROJECT_ID, AGREEMENT_ID, UUID.randomUUID(), "Eintrag 1");
        insertTimelineEntry(ISSUE_ID_WITH_AGREEMENT, PROJECT_ID, AGREEMENT_ID, UUID.randomUUID(), "Eintrag 2");
        insertTimelineEntry(UUID.randomUUID(), PROJECT_ID, AGREEMENT_ID, UUID.randomUUID(), "Andere Issue");

        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "tenant@example.com", "Tenant", Map.of(), Map.of(),
                Map.of(AGREEMENT_ID.toString(), PROJECT_ID.toString())))
            .get(TIMELINE_PATH, ISSUE_ID_WITH_AGREEMENT)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size", equalTo(2))
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
            .body("size", equalTo(0))
            .body("timelines", hasSize(0));
    }

    @Test
    void createTimelineEntryWithAttachments_SUCCESS_withoutAttachments() {
        final String timelineJson = "{"
            + "\"title\":\"Mieter-Update\"," 
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
                containsString("/ticketing/v1/tenant-relations/issues/" + ISSUE_ID_WITH_AGREEMENT + "/timelines/"))
            .body("timelineId", notNullValue())
            .body("issueId", equalTo(ISSUE_ID_WITH_AGREEMENT.toString()))
            .body("tenancyId", equalTo(AGREEMENT_ID.toString()))
            .body("projectId", equalTo(PROJECT_ID.toString()))
            .body("title", equalTo("Mieter-Update"))
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

    private void insertTimelineEntry(final UUID issueId, final UUID projectId, final UUID tenancyId,
        final UUID timelineId, final String title) {
        TenantTimelineKey key = new TenantTimelineKey();
        key.setTenancyId(tenancyId);
        key.setIssueId(issueId);
        key.setProjectId(projectId);
        key.setTimelineId(timelineId);

        TenantTimelineEntity entity = new TenantTimelineEntity();
        entity.setKey(key);
        entity.setSenderId(UUID.randomUUID());
        entity.setSenderName("Tenant");
        entity.setTitle(title);
        entity.setMessage("Message " + title);

        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setModifiedAt(now);

        cqlSession.execute("INSERT INTO remsfal.tenant_timelines "
            + "(tenancy_id, issue_id, timeline_id, project_id, attachment_id, sender_id, sender_name, "
            + "title, message, created_at, modified_at) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            entity.getTenancyId(), entity.getIssueId(), entity.getTimelineId(), entity.getProjectId(),
            entity.getAttachmentId(), entity.getSenderId(), entity.getSenderName(), entity.getTitle(),
            entity.getMessage(), entity.getCreatedAt(), entity.getModifiedAt());
    }

}
