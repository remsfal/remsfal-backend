package de.remsfal.ticketing.boundary.manager;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.TicketingTestData;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class ActivityFeedResourceTest extends AbstractTicketingTest {

    static final String ACTIVITIES_PATH = "/ticketing/v1/activities";
    static final String STATUS_PATH = "/ticketing/v1/activities/{activityId}/status";
    static final String ACTIVITY_PATH = "/ticketing/v1/activities/{activityId}";

    static final UUID USER_ID = TicketingTestData.USER_ID_1;
    static final UUID OTHER_USER_ID = TicketingTestData.USER_ID_2;

    static final UUID PROJECT_ID = UUID.randomUUID();
    static final UUID ISSUE_ID = UUID.randomUUID();
    static final UUID AGREEMENT_ID = UUID.randomUUID();
    static final UUID ORGANIZATION_ID = UUID.randomUUID();
    static final UUID CONTRACTOR_ID = UUID.randomUUID();
    static final UUID ASSIGNEE_ID = UUID.randomUUID();

    private UUID insertOwnActivity(String title) {
        return insertActivity(USER_ID, PROJECT_ID, ISSUE_ID, IssueEventType.ISSUE_CREATED,
            title, "Test description", "/api/issues/" + ISSUE_ID, UUID.randomUUID(), "Actor",
            IssueType.TASK, IssueStatus.OPEN, AGREEMENT_ID, ORGANIZATION_ID, CONTRACTOR_ID, ASSIGNEE_ID, false);
    }

    private Cookie ownCookie() {
        return buildCookie(USER_ID, TicketingTestData.USER_EMAIL_1, TicketingTestData.USER_NAME, Map.of(),
            Map.of(), Map.of());
    }

    private Cookie otherUserCookie() {
        return buildCookie(OTHER_USER_ID, TicketingTestData.USER_EMAIL_2, TicketingTestData.USER_NAME, Map.of(),
            Map.of(), Map.of());
    }

    @Test
    void getActivities_FAILED_noAuthentication() {
        given()
            .when()
            .get(ACTIVITIES_PATH)
            .then()
            .statusCode(401);
    }

    @Test
    void getActivities_SUCCESS_emptyWhenNoActivities() {
        given()
            .when()
            .cookie(ownCookie())
            .get(ACTIVITIES_PATH)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size", equalTo(0))
            .body("nextCursor", nullValue())
            .body("activities", hasSize(0));
    }

    @Test
    void getActivities_SUCCESS_returnsOwnActivitiesNewestFirst() {
        insertOwnActivity("First activity");
        final UUID secondActivityId = insertOwnActivity("Second activity");
        insertActivity(OTHER_USER_ID, PROJECT_ID, ISSUE_ID, IssueEventType.ISSUE_CREATED,
            "Other user's activity", "Test description", "/api/issues/" + ISSUE_ID, UUID.randomUUID(), "Actor",
            IssueType.TASK, IssueStatus.OPEN, AGREEMENT_ID, ORGANIZATION_ID, CONTRACTOR_ID, ASSIGNEE_ID, false);

        given()
            .when()
            .cookie(ownCookie())
            .get(ACTIVITIES_PATH)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size", equalTo(2))
            .body("nextCursor", nullValue())
            .body("activities", hasSize(2))
            .body("activities[0].id", equalTo(secondActivityId.toString()))
            .body("activities[0].title", equalTo("Second activity"))
            .body("activities[0].projectId", equalTo(PROJECT_ID.toString()))
            .body("activities[0].issueId", equalTo(ISSUE_ID.toString()))
            .body("activities[0].activityType", equalTo("ISSUE_CREATED"))
            .body("activities[0].description", equalTo("Test description"))
            .body("activities[0].link", equalTo("/api/issues/" + ISSUE_ID))
            .body("activities[0].actorName", equalTo("Actor"))
            .body("activities[0].issueType", equalTo("TASK"))
            .body("activities[0].status", equalTo("OPEN"))
            .body("activities[0].agreementId", equalTo(AGREEMENT_ID.toString()))
            .body("activities[0].organizationId", equalTo(ORGANIZATION_ID.toString()))
            .body("activities[0].contractorId", equalTo(CONTRACTOR_ID.toString()))
            .body("activities[0].assigneeId", equalTo(ASSIGNEE_ID.toString()))
            .body("activities[0].read", equalTo(false))
            .body("activities[0].createdAt", notNullValue());
    }

    @Test
    void getActivities_SUCCESS_filtersByProjectId() {
        final UUID otherProjectId = UUID.randomUUID();
        insertOwnActivity("Matching project");
        insertActivity(USER_ID, otherProjectId, ISSUE_ID, IssueEventType.ISSUE_CREATED,
            "Other project", "Test description", "/api/issues/" + ISSUE_ID, UUID.randomUUID(), "Actor",
            IssueType.TASK, IssueStatus.OPEN, AGREEMENT_ID, ORGANIZATION_ID, CONTRACTOR_ID, ASSIGNEE_ID, false);

        given()
            .when()
            .cookie(ownCookie())
            .queryParam("projectId", PROJECT_ID.toString())
            .get(ACTIVITIES_PATH)
            .then()
            .statusCode(200)
            .body("size", equalTo(1))
            .body("activities[0].title", equalTo("Matching project"));
    }

    @Test
    void getActivities_SUCCESS_filtersByIssueId() {
        final UUID otherIssueId = UUID.randomUUID();
        insertOwnActivity("Matching issue");
        insertActivity(USER_ID, PROJECT_ID, otherIssueId, IssueEventType.ISSUE_CREATED,
            "Other issue", "Test description", "/api/issues/" + otherIssueId, UUID.randomUUID(), "Actor",
            IssueType.TASK, IssueStatus.OPEN, AGREEMENT_ID, ORGANIZATION_ID, CONTRACTOR_ID, ASSIGNEE_ID, false);

        given()
            .when()
            .cookie(ownCookie())
            .queryParam("issueId", ISSUE_ID.toString())
            .get(ACTIVITIES_PATH)
            .then()
            .statusCode(200)
            .body("size", equalTo(1))
            .body("activities[0].title", equalTo("Matching issue"));
    }

    @Test
    void getActivities_SUCCESS_filtersByAgreementId() {
        final UUID otherAgreementId = UUID.randomUUID();
        insertOwnActivity("Matching agreement");
        insertActivity(USER_ID, PROJECT_ID, ISSUE_ID, IssueEventType.ISSUE_CREATED,
            "Other agreement", "Test description", "/api/issues/" + ISSUE_ID, UUID.randomUUID(), "Actor",
            IssueType.TASK, IssueStatus.OPEN, otherAgreementId, ORGANIZATION_ID, CONTRACTOR_ID, ASSIGNEE_ID, false);

        given()
            .when()
            .cookie(ownCookie())
            .queryParam("agreementId", AGREEMENT_ID.toString())
            .get(ACTIVITIES_PATH)
            .then()
            .statusCode(200)
            .body("size", equalTo(1))
            .body("activities[0].title", equalTo("Matching agreement"));
    }

    @Test
    void getActivities_SUCCESS_filtersByOrganizationId() {
        final UUID otherOrganizationId = UUID.randomUUID();
        insertOwnActivity("Matching organization");
        insertActivity(USER_ID, PROJECT_ID, ISSUE_ID, IssueEventType.ISSUE_CREATED,
            "Other organization", "Test description", "/api/issues/" + ISSUE_ID, UUID.randomUUID(), "Actor",
            IssueType.TASK, IssueStatus.OPEN, AGREEMENT_ID, otherOrganizationId, CONTRACTOR_ID, ASSIGNEE_ID, false);

        given()
            .when()
            .cookie(ownCookie())
            .queryParam("organizationId", ORGANIZATION_ID.toString())
            .get(ACTIVITIES_PATH)
            .then()
            .statusCode(200)
            .body("size", equalTo(1))
            .body("activities[0].title", equalTo("Matching organization"));
    }

    @Test
    void getActivities_SUCCESS_filtersByContractorId() {
        final UUID otherContractorId = UUID.randomUUID();
        insertOwnActivity("Matching contractor");
        insertActivity(USER_ID, PROJECT_ID, ISSUE_ID, IssueEventType.ISSUE_CREATED,
            "Other contractor", "Test description", "/api/issues/" + ISSUE_ID, UUID.randomUUID(), "Actor",
            IssueType.TASK, IssueStatus.OPEN, AGREEMENT_ID, ORGANIZATION_ID, otherContractorId, ASSIGNEE_ID, false);

        given()
            .when()
            .cookie(ownCookie())
            .queryParam("contractorId", CONTRACTOR_ID.toString())
            .get(ACTIVITIES_PATH)
            .then()
            .statusCode(200)
            .body("size", equalTo(1))
            .body("activities[0].title", equalTo("Matching contractor"));
    }

    @Test
    void getActivities_SUCCESS_filtersByAssigneeId() {
        final UUID otherAssigneeId = UUID.randomUUID();
        insertOwnActivity("Matching assignee");
        insertActivity(USER_ID, PROJECT_ID, ISSUE_ID, IssueEventType.ISSUE_CREATED,
            "Other assignee", "Test description", "/api/issues/" + ISSUE_ID, UUID.randomUUID(), "Actor",
            IssueType.TASK, IssueStatus.OPEN, AGREEMENT_ID, ORGANIZATION_ID, CONTRACTOR_ID, otherAssigneeId, false);

        given()
            .when()
            .cookie(ownCookie())
            .queryParam("assigneeId", ASSIGNEE_ID.toString())
            .get(ACTIVITIES_PATH)
            .then()
            .statusCode(200)
            .body("size", equalTo(1))
            .body("activities[0].title", equalTo("Matching assignee"));
    }

    @Test
    void getActivities_SUCCESS_fullPageReturnsNextCursorForPagination() {
        insertOwnActivity("Activity 1");
        insertOwnActivity("Activity 2");
        insertOwnActivity("Activity 3");

        final String firstPageNextCursor = given()
            .when()
            .cookie(ownCookie())
            .queryParam("limit", 2)
            .get(ACTIVITIES_PATH)
            .then()
            .statusCode(200)
            .body("size", equalTo(2))
            .body("nextCursor", notNullValue())
            .body("activities[0].title", equalTo("Activity 3"))
            .body("activities[1].title", equalTo("Activity 2"))
            .extract()
            .path("nextCursor");

        given()
            .when()
            .cookie(ownCookie())
            .queryParam("limit", 2)
            .queryParam("cursor", firstPageNextCursor)
            .get(ACTIVITIES_PATH)
            .then()
            .statusCode(200)
            .body("size", equalTo(1))
            .body("nextCursor", nullValue())
            .body("activities[0].title", equalTo("Activity 1"));
    }

    @Test
    void updateActivityStatus_SUCCESS_marksAsRead() {
        final UUID activityId = insertOwnActivity("Activity");

        given()
            .when()
            .cookie(ownCookie())
            .queryParam("read", true)
            .patch(STATUS_PATH, activityId)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(activityId.toString()))
            .body("read", equalTo(true));

        given()
            .when()
            .cookie(ownCookie())
            .get(ACTIVITIES_PATH)
            .then()
            .statusCode(200)
            .body("activities[0].read", equalTo(true));
    }

    @Test
    void updateActivityStatus_SUCCESS_marksAsUnread() {
        final UUID activityId = insertOwnActivity("Activity");

        given()
            .when()
            .cookie(ownCookie())
            .queryParam("read", true)
            .patch(STATUS_PATH, activityId)
            .then()
            .statusCode(200);

        given()
            .when()
            .cookie(ownCookie())
            .queryParam("read", false)
            .patch(STATUS_PATH, activityId)
            .then()
            .statusCode(200)
            .body("read", equalTo(false));
    }

    @Test
    void updateActivityStatus_FAILED_noAuthentication() {
        final UUID activityId = insertOwnActivity("Activity");

        given()
            .when()
            .queryParam("read", true)
            .patch(STATUS_PATH, activityId)
            .then()
            .statusCode(401);
    }

    @Test
    void updateActivityStatus_FAILED_notFoundForNonexistentActivity() {
        given()
            .when()
            .cookie(ownCookie())
            .queryParam("read", true)
            .patch(STATUS_PATH, UUID.randomUUID())
            .then()
            .statusCode(404);
    }

    @Test
    void updateActivityStatus_FAILED_notFoundForOtherUsersActivity() {
        final UUID activityId = insertOwnActivity("Activity");

        given()
            .when()
            .cookie(otherUserCookie())
            .queryParam("read", true)
            .patch(STATUS_PATH, activityId)
            .then()
            .statusCode(404);
    }

    @Test
    void deleteActivity_SUCCESS() {
        final UUID activityId = insertOwnActivity("Activity");

        given()
            .when()
            .cookie(ownCookie())
            .delete(ACTIVITY_PATH, activityId)
            .then()
            .statusCode(204);

        given()
            .when()
            .cookie(ownCookie())
            .get(ACTIVITIES_PATH)
            .then()
            .statusCode(200)
            .body("size", equalTo(0));
    }

    @Test
    void deleteActivity_FAILED_noAuthentication() {
        final UUID activityId = insertOwnActivity("Activity");

        given()
            .when()
            .delete(ACTIVITY_PATH, activityId)
            .then()
            .statusCode(401);
    }

    @Test
    void deleteActivity_FAILED_notFoundForNonexistentActivity() {
        given()
            .when()
            .cookie(ownCookie())
            .delete(ACTIVITY_PATH, UUID.randomUUID())
            .then()
            .statusCode(404);
    }

    @Test
    void deleteActivity_FAILED_notFoundForOtherUsersActivity() {
        final UUID activityId = insertOwnActivity("Activity");

        given()
            .when()
            .cookie(otherUserCookie())
            .delete(ACTIVITY_PATH, activityId)
            .then()
            .statusCode(404);

        given()
            .when()
            .cookie(ownCookie())
            .get(ACTIVITIES_PATH)
            .then()
            .statusCode(200)
            .body("size", equalTo(1));
    }

}
