package de.remsfal.ticketing.boundary.manager;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

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
    void createTimelineEntry_SUCCESS_targetsSelectedOrganization() {
        final String timelineJson = "{"
            + "\"purpose\":\"MESSAGE_SENT\","
            + "\"message\":\"An Bauservice GmbH\""
            + "}";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .multiPart("timeline", timelineJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .queryParam("organizationId", firstOrganizationId)
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
            + "\"purpose\":\"MESSAGE_SENT\","
            + "\"message\":\"An Bauservice GmbH\""
            + "}";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .multiPart("timeline", timelineJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .queryParam("organizationId", UUID.randomUUID())
            .post(timelinePath())
            .then()
            .statusCode(404);
    }

    @Test
    void getTimelineEntries_SUCCESS_aggregatesEntriesFromBothContractors() {
        final String firstMessage = "{"
            + "\"purpose\":\"MESSAGE_SENT\","
            + "\"message\":\"An Bauservice GmbH\""
            + "}";
        final String secondMessage = "{"
            + "\"purpose\":\"MESSAGE_SENT\","
            + "\"message\":\"An Elektro Schmidt\""
            + "}";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .multiPart("timeline", firstMessage, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .queryParam("organizationId", firstOrganizationId)
            .post(timelinePath())
            .then()
            .statusCode(201);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .multiPart("timeline", secondMessage, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .queryParam("organizationId", secondOrganizationId)
            .post(timelinePath())
            .then()
            .statusCode(201);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(timelinePath())
            .then()
            .statusCode(200)
            .body("timelines", hasSize(2))
            .body("timelines.organizationId", containsInAnyOrder(
                firstOrganizationId.toString(), secondOrganizationId.toString()));
    }

}
