package de.remsfal.ticketing.boundary.manager;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
class IssueContractorTimelineResourceTest extends AbstractTicketingTest {

    static final String BASE_PATH = "/ticketing/v1/issues";

    String issueId;
    final UUID firstOrganizationId = UUID.randomUUID();
    final UUID secondOrganizationId = UUID.randomUUID();

    @BeforeEach
    void setUpIssueAndQuotationRequests() {
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

        final String requestJson = "{ \"contractors\":["
            + "{\"id\":\"" + UUID.randomUUID() + "\",\"name\":\"Bauservice GmbH\","
            + "\"organizationId\":\"" + firstOrganizationId + "\"},"
            + "{\"id\":\"" + UUID.randomUUID() + "\",\"name\":\"Elektro Schmidt\","
            + "\"organizationId\":\"" + secondOrganizationId + "\"}"
            + "] }";
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);
    }

    private String timelinePath() {
        return BASE_PATH + "/" + issueId + "/contractor-timeline";
    }

    @Test
    void getTimelineEntries_SUCCESS_containsQuotationRequestedEntriesInitially() {
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(timelinePath())
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("timelines", hasSize(2))
            .body("timelines.purpose", containsInAnyOrder("QUOTATION_REQUESTED", "QUOTATION_REQUESTED"))
            .body("timelines.organizationId", containsInAnyOrder(
                firstOrganizationId.toString(), secondOrganizationId.toString()));
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
    void createTimelineEntry_SUCCESS_targetsSelectedOrganization() {
        final String timelineJson = "{"
            + "\"organizationId\":\"" + firstOrganizationId + "\","
            + "\"purpose\":\"MESSAGE_SENT\","
            + "\"message\":\"An Bauservice GmbH\""
            + "}";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .multiPart("timeline", timelineJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(timelinePath())
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("organizationId", equalTo(firstOrganizationId.toString()))
            .body("senderRole", equalTo("MANAGER"))
            .body("message", equalTo("An Bauservice GmbH"));
    }

    @Test
    void createTimelineEntry_FAILED_missingOrganizationId() {
        final String timelineJson = "{"
            + "\"purpose\":\"MESSAGE_SENT\","
            + "\"message\":\"An Bauservice GmbH\""
            + "}";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .multiPart("timeline", timelineJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(timelinePath())
            .then()
            .statusCode(400);
    }

    @Test
    void createTimelineEntry_FAILED_unknownOrganization() {
        final String timelineJson = "{"
            + "\"organizationId\":\"" + UUID.randomUUID() + "\","
            + "\"purpose\":\"MESSAGE_SENT\","
            + "\"message\":\"An Bauservice GmbH\""
            + "}";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .multiPart("timeline", timelineJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(timelinePath())
            .then()
            .statusCode(404);
    }

    @Test
    void getTimelineEntries_SUCCESS_aggregatesEntriesFromBothContractors() {
        final String firstMessage = "{"
            + "\"organizationId\":\"" + firstOrganizationId + "\","
            + "\"purpose\":\"MESSAGE_SENT\","
            + "\"message\":\"An Bauservice GmbH\""
            + "}";
        final String secondMessage = "{"
            + "\"organizationId\":\"" + secondOrganizationId + "\","
            + "\"purpose\":\"MESSAGE_SENT\","
            + "\"message\":\"An Elektro Schmidt\""
            + "}";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .multiPart("timeline", firstMessage, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(timelinePath())
            .then()
            .statusCode(201);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .multiPart("timeline", secondMessage, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(timelinePath())
            .then()
            .statusCode(201);

        final List<Map<String, Object>> timelines = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(timelinePath())
            .then()
            .statusCode(200)
            .extract().jsonPath().getList("timelines");

        assertEquals(4, timelines.size());
        final List<Map<String, Object>> messagesSent = timelines.stream()
            .filter(t -> "MESSAGE_SENT".equals(t.get("purpose")))
            .toList();
        assertEquals(2, messagesSent.size());
        final List<String> messageSentOrgIds = messagesSent.stream()
            .map(t -> (String) t.get("organizationId"))
            .sorted()
            .toList();
        assertEquals(List.of(firstOrganizationId, secondOrganizationId).stream()
            .map(UUID::toString).sorted().toList(), messageSentOrgIds);
    }

}
