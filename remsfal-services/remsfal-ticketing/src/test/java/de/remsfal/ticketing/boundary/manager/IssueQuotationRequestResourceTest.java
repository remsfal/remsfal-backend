package de.remsfal.ticketing.boundary.manager;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.TicketingTestData;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class IssueQuotationRequestResourceTest extends AbstractTicketingTest {

    static final String BASE_PATH = "/ticketing/v1/issues";
    static final String QUOTATION_PATH = "/ticketing/v1/order-management/quotation-requests";

    // --- Create Quotation Requests ---

    @Test
    void createRequestsForQuotation_SUCCESS_createsSeparateRowsPerContractor() {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\","
            + "\"visibleToTenants\":false"
            + "}";
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        UUID contractorId1 = UUID.randomUUID();
        UUID contractorId2 = UUID.randomUUID();
        String requestJson = "{ \"contractors\":["
            + "{\"id\":\"" + contractorId1 + "\",\"name\":\"Contractor A\"},"
            + "{\"id\":\"" + contractorId2 + "\",\"name\":\"Contractor B\"}"
            + "],\"scopeOfWork\":\"Please submit your quotation.\" }";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        List<Row> rows = cqlSession.execute(
            "SELECT project_id, issue_id, initiator_id, contractor_id, scope_of_work, status "
                + "FROM remsfal.quotation_requests WHERE issue_id = ?",
            UUID.fromString(issueId))
            .all();

        Set<UUID> contractorIds = rows.stream()
            .map(row -> row.getUuid("contractor_id"))
            .collect(Collectors.toSet());

        assertEquals(2, rows.size());
        assertEquals(Set.of(contractorId1, contractorId2), contractorIds);
        assertTrue(rows.stream().allMatch(
            row -> TicketingTestData.PROJECT_ID.equals(row.getUuid("project_id"))));
        assertTrue(rows.stream().allMatch(
            row -> UUID.fromString(issueId).equals(row.getUuid("issue_id"))));
        assertTrue(rows.stream().allMatch(
            row -> TicketingTestData.USER_ID.equals(row.getUuid("initiator_id"))));
        assertTrue(rows.stream().allMatch(
            row -> "Please submit your quotation.".equals(row.getString("scope_of_work"))));
        assertTrue(rows.stream().allMatch(
            row -> "REQUESTED".equals(row.getString("status"))));
    }

    @Test
    void createRequestsForQuotation_SUCCESS_copiesIssueAttachments() throws Exception {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\","
            + "\"visibleToTenants\":false"
            + "}";
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        final UUID attachmentId = UUID.randomUUID();
        final String objectName = "/issues/" + issueId + "/attachments/" + attachmentId + "/"
            + TicketingTestData.ATTACHMENT_FILE_PATH_1;
        uploadTestFile(TicketingTestData.ATTACHMENT_FILE_PATH_1, TicketingTestData.ATTACHMENT_FILE_TYPE_1,
            objectName);
        insertAttachment(UUID.fromString(issueId), attachmentId, TicketingTestData.ATTACHMENT_FILE_PATH_1,
            TicketingTestData.ATTACHMENT_FILE_TYPE_1, objectName, TicketingTestData.USER_ID);

        String requestJson = "{ \"contractors\":["
            + "{\"id\":\"" + UUID.randomUUID() + "\",\"name\":\"Contractor A\"},"
            + "{\"id\":\"" + UUID.randomUUID() + "\",\"name\":\"Contractor B\"}"
            + "],\"attachmentIds\":[\"" + attachmentId + "\"] }";

        final List<String> requestIds = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201)
            .extract().jsonPath().getList("items.id");

        assertEquals(2, requestIds.size());
        for (final String requestId : requestIds) {
            given()
                .when()
                .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
                .get(BASE_PATH + "/" + issueId + "/quotation-request/" + requestId)
                .then()
                .statusCode(200)
                .body("attachments", hasSize(1))
                .body("attachments[0].fileName", equalTo(TicketingTestData.ATTACHMENT_FILE_PATH_1))
                .body("attachments[0].processId", equalTo(requestId));
        }
    }

    @Test
    void createRequestsForQuotation_FAILED_unknownAttachment() {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\","
            + "\"visibleToTenants\":false"
            + "}";
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        String requestJson = "{ \"contractors\":[{\"id\":\"" + UUID.randomUUID()
            + "\",\"name\":\"Bauservice GmbH\"}],"
            + "\"attachmentIds\":[\"" + UUID.randomUUID() + "\"] }";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(404);

        assertEquals(0, cqlSession.execute(
            "SELECT request_id FROM remsfal.quotation_requests WHERE issue_id = ?",
            UUID.fromString(issueId)).all().size());
    }

    @Test
    void createRequestsForQuotation_SUCCESS_leavesEnrichedFieldsEmpty() {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\","
            + "\"visibleToTenants\":false"
            + "}";
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        String requestJson = "{ \"contractors\":[{\"id\":\"" + UUID.randomUUID()
            + "\",\"name\":\"Bauservice GmbH\"}] }";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        List<Row> rows = cqlSession.execute(
            "SELECT place_of_performance_address_1, place_of_performance_address_2,"
                + " place_of_performance_address_3, rental_unit_type, rental_unit_title,"
                + " rental_unit_location, tenants"
                + " FROM remsfal.quotation_requests WHERE issue_id = ?",
            UUID.fromString(issueId))
            .all();

        assertEquals(1, rows.size());
        assertNull(rows.get(0).getString("place_of_performance_address_1"));
        assertNull(rows.get(0).getString("place_of_performance_address_2"));
        assertNull(rows.get(0).getString("place_of_performance_address_3"));
        assertNull(rows.get(0).getString("rental_unit_type"));
        assertNull(rows.get(0).getString("rental_unit_title"));
        assertNull(rows.get(0).getString("rental_unit_location"));
        assertTrue(rows.get(0).getList("tenants", String.class).isEmpty());
    }

    @Test
    void createRequestsForQuotation_FAILED_noPermission() {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\","
            + "\"visibleToTenants\":false"
            + "}";
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        UUID contractorId = UUID.randomUUID();
        String requestJson = "{ \"contractors\":[{\"id\":\"" + contractorId
            + "\",\"name\":\"Test Contractor\"}] }";

        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "unauthorized@test.com",
                "Unauthorized", Map.of(), Map.of(), Map.of()))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(403);
    }

    @Test
    void createRequestsForQuotation_SUCCESS_closesPreviousOpenRequestToSameContractor() {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\","
            + "\"visibleToTenants\":false"
            + "}";
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        UUID contractorId = UUID.randomUUID();
        String requestJson = "{ \"contractors\":[{\"id\":\"" + contractorId
            + "\",\"name\":\"Bauservice GmbH\"}],\"scopeOfWork\":\"Erste Anfrage.\" }";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        List<Row> rows = cqlSession.execute(
            "SELECT request_id, status FROM remsfal.quotation_requests WHERE issue_id = ? AND contractor_id = ?"
                + " ALLOW FILTERING",
            UUID.fromString(issueId), contractorId)
            .all();

        assertEquals(2, rows.size());
        List<String> statuses = rows.stream().map(row -> row.getString("status")).sorted().toList();
        assertEquals(List.of("REQUESTED", "WITHDRAWN"), statuses);
    }

    @Test
    void createRequestsForQuotation_SUCCESS_doesNotCloseRequestToDifferentContractor() {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\","
            + "\"visibleToTenants\":false"
            + "}";
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        UUID contractorIdA = UUID.randomUUID();
        UUID contractorIdB = UUID.randomUUID();
        String requestJsonA = "{ \"contractors\":[{\"id\":\"" + contractorIdA
            + "\",\"name\":\"Contractor A\"}] }";
        String requestJsonB = "{ \"contractors\":[{\"id\":\"" + contractorIdB
            + "\",\"name\":\"Contractor B\"}] }";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJsonA)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJsonB)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        List<Row> rows = cqlSession.execute(
            "SELECT contractor_id, status FROM remsfal.quotation_requests WHERE issue_id = ?",
            UUID.fromString(issueId))
            .all();

        assertEquals(2, rows.size());
        assertTrue(rows.stream().allMatch(row -> "REQUESTED".equals(row.getString("status"))));
    }

    @Test
    void createRequestsForQuotation_SUCCESS_doesNotCloseAlreadySubmittedRequest() {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\","
            + "\"visibleToTenants\":false"
            + "}";
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        UUID contractorId = UUID.randomUUID();
        String requestJson = "{ \"contractors\":[{\"id\":\"" + contractorId
            + "\",\"name\":\"Bauservice GmbH\"}] }";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        UUID firstRequestId = cqlSession.execute(
            "SELECT request_id FROM remsfal.quotation_requests WHERE issue_id = ?",
            UUID.fromString(issueId))
            .one().getUuid("request_id");

        cqlSession.execute("UPDATE remsfal.quotation_requests SET status = 'SUBMITTED'"
            + " WHERE issue_id = ? AND request_id = ?",
            UUID.fromString(issueId), firstRequestId);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        List<Row> rows = cqlSession.execute(
            "SELECT request_id, status FROM remsfal.quotation_requests WHERE issue_id = ?",
            UUID.fromString(issueId))
            .all();

        assertEquals(2, rows.size());
        String firstRequestStatus = rows.stream()
            .filter(row -> firstRequestId.equals(row.getUuid("request_id")))
            .findFirst().orElseThrow().getString("status");
        assertEquals("SUBMITTED", firstRequestStatus);
    }

    @Test
    void createRequestsForQuotation_SUCCESS_writesQuotationRequestedContractorTimelineEntry() {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\","
            + "\"visibleToTenants\":false"
            + "}";
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
        final UUID contractorId = UUID.randomUUID();
        final String requestJson = "{ \"contractors\":[{\"id\":\"" + contractorId
            + "\",\"name\":\"Test Betrieb\",\"organizationId\":\"" + organizationId + "\"}] }";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueId + "/contractor-timeline")
            .then()
            .statusCode(200)
            .body("timelines", hasSize(1))
            .body("timelines[0].purpose", equalTo("QUOTATION_REQUESTED"))
            .body("timelines[0].organizationId", equalTo(organizationId.toString()))
            .body("timelines[0].senderRole", equalTo("MANAGER"));
    }

    @Test
    void createRequestsForQuotation_SUCCESS_writesStatusChangedEntryForWithdrawnPreviousRequest() {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\","
            + "\"visibleToTenants\":false"
            + "}";
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
        final UUID contractorId = UUID.randomUUID();
        final String requestJson = "{ \"contractors\":[{\"id\":\"" + contractorId
            + "\",\"name\":\"Bauservice GmbH\",\"organizationId\":\"" + organizationId + "\"}] }";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        final List<Map<String, Object>> timelines = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueId + "/contractor-timeline")
            .then()
            .statusCode(200)
            .extract().jsonPath().getList("timelines");

        assertEquals(3, timelines.size());
        final List<String> purposes = timelines.stream()
            .map(t -> (String) t.get("purpose"))
            .sorted()
            .toList();
        assertEquals(List.of("QUOTATION_REQUESTED", "QUOTATION_REQUESTED", "STATUS_CHANGED"), purposes);

        final Map<String, Object> statusChanged = timelines.stream()
            .filter(t -> "STATUS_CHANGED".equals(t.get("purpose")))
            .findFirst().orElseThrow();
        assertEquals("WITHDRAWN", statusChanged.get("message"));
        assertEquals(organizationId.toString(), statusChanged.get("organizationId"));
        assertEquals("MANAGER", statusChanged.get("senderRole"));
    }

    // --- Get Quotation Requests ---

    @Test
    void getRequestsForQuotation_SUCCESS_returnsListForManager() {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\","
            + "\"visibleToTenants\":false"
            + "}";
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        UUID contractorId = UUID.randomUUID();
        UUID organizationId = TicketingTestData.ORGANIZATION_ID;
        String requestJson = "{ \"contractors\":[{\"id\":\"" + contractorId
            + "\",\"name\":\"Test GmbH\",\"organizationId\":\"" + organizationId + "\"}],"
            + "\"scopeOfWork\":\"Bitte Angebot einreichen.\" }";
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(200)
            .body("items", hasSize(1))
            .body("items[0].contractorId", equalTo(contractorId.toString()))
            .body("items[0].scopeOfWork", equalTo("Bitte Angebot einreichen."))
            .body("items[0].status", equalTo("REQUESTED"));
    }

    @Test
    void getRequestsForQuotation_FAILED_noPermission() {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\","
            + "\"visibleToTenants\":false"
            + "}";
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "unauthorized@test.com",
                "Unauthorized", Map.of(), Map.of(), Map.of()))
            .get(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(403);
    }

    @Test
    void getRequestForQuotation_SUCCESS_returnsSingleRequest() {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\","
            + "\"visibleToTenants\":false"
            + "}";
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        UUID contractorId = UUID.randomUUID();
        String requestJson = "{ \"contractors\":[{\"id\":\"" + contractorId
            + "\",\"name\":\"Test GmbH\"}],\"scopeOfWork\":\"Anfrage.\" }";
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        final String requestId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(200)
            .extract().path("items[0].id");

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueId + "/quotation-request/" + requestId)
            .then()
            .statusCode(200)
            .body("id", equalTo(requestId))
            .body("contractorId", equalTo(contractorId.toString()))
            .body("status", equalTo("REQUESTED"));
    }

    @Test
    void updateRequestForQuotation_SUCCESS_updatesStatus() {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\","
            + "\"visibleToTenants\":false"
            + "}";
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        UUID contractorId = UUID.randomUUID();
        String requestJson = "{ \"contractors\":[{\"id\":\"" + contractorId
            + "\",\"name\":\"Test GmbH\"}],\"scopeOfWork\":\"Original.\" }";
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        final String requestId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(200)
            .extract().path("items[0].id");

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body("{ \"status\":\"WITHDRAWN\", \"scopeOfWork\":\"Aktualisiert.\" }")
            .patch(BASE_PATH + "/" + issueId + "/quotation-request/" + requestId)
            .then()
            .statusCode(200)
            .body("status", equalTo("WITHDRAWN"))
            .body("scopeOfWork", equalTo("Aktualisiert."));
    }

    @Test
    void updateRequestForQuotation_FAILED_managerSetsInvalidStatus() {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\","
            + "\"visibleToTenants\":false"
            + "}";
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        UUID contractorId = UUID.randomUUID();
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body("{ \"contractors\":[{\"id\":\"" + contractorId + "\",\"name\":\"Test GmbH\"}] }")
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        final String requestId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(200)
            .extract().path("items[0].id");

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body("{ \"status\":\"SUBMITTED\" }")
            .patch(BASE_PATH + "/" + issueId + "/quotation-request/" + requestId)
            .then()
            .statusCode(400);
    }

    @Test
    void updateRequestForQuotation_SUCCESS_writesStatusChangedContractorTimelineEntry() {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\","
            + "\"visibleToTenants\":false"
            + "}";
        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issueJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
        final UUID contractorId = UUID.randomUUID();
        final String requestJson = "{ \"contractors\":[{\"id\":\"" + contractorId
            + "\",\"name\":\"Test GmbH\",\"organizationId\":\"" + organizationId + "\"}] }";
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        final String requestId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(200)
            .extract().path("items[0].id");

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body("{ \"status\":\"WITHDRAWN\" }")
            .patch(BASE_PATH + "/" + issueId + "/quotation-request/" + requestId)
            .then()
            .statusCode(200);

        final List<Map<String, Object>> timelines = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueId + "/contractor-timeline")
            .then()
            .statusCode(200)
            .extract().jsonPath().getList("timelines");

        assertEquals(2, timelines.size());
        final Map<String, Object> statusChanged = timelines.stream()
            .filter(t -> "STATUS_CHANGED".equals(t.get("purpose")))
            .findFirst().orElseThrow();
        assertEquals("WITHDRAWN", statusChanged.get("message"));
        assertEquals(organizationId.toString(), statusChanged.get("organizationId"));
        assertEquals("MANAGER", statusChanged.get("senderRole"));
    }

    @Test
    void placeOrder_SUCCESS_createsOrderPlacement() {
        final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
        final UUID contractorId = UUID.randomUUID();
        final UUID contractorUserId = UUID.randomUUID();

        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body("{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
                + "\"type\":\"TASK\","
                + "\"visibleToTenants\":false"
                + "}")
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body("{ \"contractors\":[{\"id\":\"" + contractorId
                + "\",\"name\":\"Test Betrieb\",\"organizationId\":\"" + organizationId + "\"}] }")
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        final String requestId = given()
            .when()
            .cookie(buildCookie(contractorUserId, "contractor@test.com", "Contractor Manager",
                Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of()))
            .get(QUOTATION_PATH)
            .then()
            .statusCode(200)
            .extract().path("items[0].id");

        final String quotationId = given()
            .when()
            .cookie(buildCookie(contractorUserId, "contractor@test.com", "Contractor Manager",
                Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of()))
            .contentType(ContentType.JSON)
            .body("{ \"status\":\"VALID\" }")
            .post(QUOTATION_PATH + "/" + requestId + "/quotation")
            .then()
            .statusCode(200)
            .extract().path("id");

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + issueId + "/quotations/" + quotationId + "/orders")
            .then()
            .statusCode(201);

        List<Row> rows = cqlSession.execute(
            "SELECT issue_id, quotation_id, project_id, orderer_id, ordered_by, contractor_id,"
                + " organization_id, status "
                + "FROM remsfal.order_placements WHERE issue_id = ?",
            UUID.fromString(issueId))
            .all();

        assertEquals(1, rows.size());
        Row row = rows.get(0);
        assertEquals(UUID.fromString(issueId), row.getUuid("issue_id"));
        assertEquals(UUID.fromString(quotationId), row.getUuid("quotation_id"));
        assertEquals(TicketingTestData.PROJECT_ID, row.getUuid("project_id"));
        assertEquals(TicketingTestData.USER_ID, row.getUuid("orderer_id"));
        assertEquals(TicketingTestData.USER_NAME, row.getString("ordered_by"));
        assertEquals(contractorId, row.getUuid("contractor_id"));
        assertEquals(organizationId, row.getUuid("organization_id"));
        assertEquals("PLACED", row.getString("status"));
    }

}
