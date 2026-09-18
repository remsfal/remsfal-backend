package de.remsfal.ticketing.boundary.contractor;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;
import java.util.List;
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
class ContractorTimelineResourceTest extends AbstractTicketingTest {

    static final String ISSUE_BASE_PATH = "/ticketing/v1/issues";
    static final String ORDER_MANAGEMENT_PATH = "/ticketing/v1/order-management";

    final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
    final UUID contractorUserId = UUID.randomUUID();

    String issueId;

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
            .post(ISSUE_BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        final String requestJson = "{ \"contractors\":[{\"id\":\"" + UUID.randomUUID()
            + "\",\"name\":\"Bauservice GmbH\",\"organizationId\":\"" + organizationId + "\"}] }";
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(ISSUE_BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);
    }

    private io.restassured.http.Cookie contractorCookie() {
        return buildCookie(contractorUserId, "contractor@test.com", "Contractor Manager",
            Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of());
    }

    private String timelinePath() {
        return ORDER_MANAGEMENT_PATH + "/" + issueId + "/timeline";
    }

    @Test
    void getTimelineEntries_SUCCESS_containsQuotationRequestedEntryInitially() {
        given()
            .when()
            .cookie(contractorCookie())
            .get(timelinePath())
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("timelines", hasSize(1))
            .body("timelines[0].purpose", equalTo("QUOTATION_REQUESTED"));
    }

    @Test
    void getTimelineEntries_FAILED_wrongOrganization() {
        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "other@test.com", "Other Contractor",
                Map.of(), Map.of(UUID.randomUUID().toString(), "MANAGER"), Map.of()))
            .get(timelinePath())
            .then()
            .statusCode(404);
    }

    @Test
    void createTimelineEntry_SUCCESS_asContractor() {
        final String timelineJson = "{"
            + "\"purpose\":\"MESSAGE_SENT\","
            + "\"message\":\"Angebot in Vorbereitung\""
            + "}";

        given()
            .when()
            .cookie(contractorCookie())
            .multiPart("timeline", timelineJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(timelinePath())
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("timelineId", notNullValue())
            .body("senderRole", equalTo("CONTRACTOR"))
            .body("purpose", equalTo("MESSAGE_SENT"))
            .body("message", equalTo("Angebot in Vorbereitung"));
    }

    @Test
    void createTimelineEntry_FAILED_wrongOrganization() {
        final String timelineJson = "{"
            + "\"purpose\":\"MESSAGE_SENT\","
            + "\"message\":\"Angebot in Vorbereitung\""
            + "}";

        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "other@test.com", "Other Contractor",
                Map.of(), Map.of(UUID.randomUUID().toString(), "MANAGER"), Map.of()))
            .multiPart("timeline", timelineJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(timelinePath())
            .then()
            .statusCode(404);
    }

    @Test
    void createTimelineEntryWithAttachments_FAILED_missingTimelinePart() {
        given()
            .when()
            .cookie(contractorCookie())
            .multiPart("notTimeline", "{}", MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(timelinePath())
            .then()
            .statusCode(400);
    }

    @Test
    void createTimelineEntryWithAttachments_SUCCESS_uploadedAttachmentIsLinkedAndVisible() {
        final String timelineJson = "{"
            + "\"purpose\":\"MESSAGE_SENT\","
            + "\"message\":\"Angebot in Vorbereitung\""
            + "}";
        final InputStream attachmentStream = getTestFileStream(TicketingTestData.ATTACHMENT_FILE_PATH_1);

        given()
            .when()
            .cookie(contractorCookie())
            .multiPart("timeline", timelineJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .multiPart("attachment", TicketingTestData.ATTACHMENT_FILE_PATH_1,
                attachmentStream, TicketingTestData.ATTACHMENT_FILE_TYPE_1)
            .post(timelinePath())
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("attachments", hasSize(1))
            .body("attachments[0].fileName", equalTo(TicketingTestData.ATTACHMENT_FILE_PATH_1));

        final List<Map<String, Object>> timelines = given()
            .when()
            .cookie(contractorCookie())
            .get(timelinePath())
            .then()
            .statusCode(200)
            .extract().jsonPath().getList("timelines");

        assertEquals(2, timelines.size());
        final Map<String, Object> messageSent = timelines.stream()
            .filter(t -> "MESSAGE_SENT".equals(t.get("purpose")))
            .findFirst().orElseThrow();
        assertEquals(1, ((List<?>) messageSent.get("attachments")).size());
    }

    @Test
    void getTimelineEntries_SUCCESS_afterCreatingEntry() {
        final String timelineJson = "{"
            + "\"purpose\":\"MESSAGE_SENT\","
            + "\"message\":\"Angebot in Vorbereitung\""
            + "}";
        given()
            .when()
            .cookie(contractorCookie())
            .multiPart("timeline", timelineJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(timelinePath())
            .then()
            .statusCode(201);

        final List<Map<String, Object>> timelines = given()
            .when()
            .cookie(contractorCookie())
            .get(timelinePath())
            .then()
            .statusCode(200)
            .extract().jsonPath().getList("timelines");

        assertEquals(2, timelines.size());
        final Map<String, Object> messageSent = timelines.stream()
            .filter(t -> "MESSAGE_SENT".equals(t.get("purpose")))
            .findFirst().orElseThrow();
        assertEquals("Angebot in Vorbereitung", messageSent.get("message"));
    }

}
