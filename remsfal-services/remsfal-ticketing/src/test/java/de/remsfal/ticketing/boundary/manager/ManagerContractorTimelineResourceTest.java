package de.remsfal.ticketing.boundary.manager;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.io.InputStream;
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
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class ManagerContractorTimelineResourceTest extends AbstractTicketingTest {

    static final String BASE_PATH = "/ticketing/v1/issues";

    String issueId;
    String requestId;

    @BeforeEach
    void setUpIssueAndQuotationRequest() {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\","
            + "\"visibleToTenants\":false"
            + "}";
        issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        final String requestJson = "{ \"contractors\":[{\"id\":\"" + UUID.randomUUID()
            + "\",\"name\":\"Bauservice GmbH\",\"organizationId\":\"" + UUID.randomUUID() + "\"}] }";
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        requestId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(200)
            .extract().path("items[0].id");
    }

    private String timelinePath() {
        return BASE_PATH + "/" + issueId + "/quotation-request/" + requestId + "/timeline";
    }

    @Test
    void getTimelineEntries_SUCCESS_returnsEmptyListInitially() {
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(timelinePath())
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("timelines", hasSize(0));
    }

    @Test
    void getTimelineEntries_FAILED_noPermission() {
        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "unauthorized@test.com",
                "Unauthorized", Map.of(), Map.of(), Map.of()))
            .get(timelinePath())
            .then()
            .statusCode(403);
    }

    @Test
    void createTimelineEntry_SUCCESS_asManager() {
        final String timelineJson = "{"
            + "\"purpose\":\"MESSAGE_SENT\","
            + "\"message\":\"Bitte um Rueckmeldung\""
            + "}";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .multiPart("timeline", timelineJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(timelinePath())
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("timelineId", notNullValue())
            .body("senderRole", equalTo("MANAGER"))
            .body("purpose", equalTo("MESSAGE_SENT"))
            .body("message", equalTo("Bitte um Rueckmeldung"));
    }

    @Test
    void createTimelineEntry_FAILED_noPermission() {
        final String timelineJson = "{"
            + "\"purpose\":\"MESSAGE_SENT\","
            + "\"message\":\"Bitte um Rueckmeldung\""
            + "}";

        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "unauthorized@test.com",
                "Unauthorized", Map.of(), Map.of(), Map.of()))
            .multiPart("timeline", timelineJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(timelinePath())
            .then()
            .statusCode(403);
    }

    @Test
    void createTimelineEntryWithAttachments_FAILED_missingTimelinePart() {
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .multiPart("notTimeline", "{}", MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(timelinePath())
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
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .multiPart("timeline", timelineJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .multiPart("attachment", TicketingTestData.ATTACHMENT_FILE_PATH_1,
                attachmentStream, TicketingTestData.ATTACHMENT_FILE_TYPE_1)
            .post(timelinePath())
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("attachments", hasSize(1))
            .body("attachments[0].fileName", equalTo(TicketingTestData.ATTACHMENT_FILE_PATH_1));

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(timelinePath())
            .then()
            .statusCode(200)
            .body("timelines", hasSize(1))
            .body("timelines[0].attachments", hasSize(1));
    }

    @Test
    void getTimelineEntries_SUCCESS_afterCreatingEntry() {
        final String timelineJson = "{"
            + "\"purpose\":\"MESSAGE_SENT\","
            + "\"message\":\"Bitte um Rueckmeldung\""
            + "}";
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .multiPart("timeline", timelineJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(timelinePath())
            .then()
            .statusCode(201);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(timelinePath())
            .then()
            .statusCode(200)
            .body("timelines", hasSize(1))
            .body("timelines[0].message", equalTo("Bitte um Rueckmeldung"));
    }

}
