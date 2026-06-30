package de.remsfal.ticketing.boundary;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    // --- Create Quotation Requests ---

    @Test
    void createRequestsForQuotation_SUCCESS_createsSeparateRowsPerContractor() {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\""
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
            + "{\"id\":\"" + contractorId1 + "\",\"companyName\":\"Contractor A\"},"
            + "{\"id\":\"" + contractorId2 + "\",\"companyName\":\"Contractor B\"}"
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
    void createRequestsForQuotation_SUCCESS_storesBillingAddress() {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\""
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
            + "\",\"companyName\":\"Bauservice GmbH\"}],"
            + "\"projectOwner\":\"Mustermann Verwaltung GmbH\","
            + "\"projectCareOf\":\"Max Mustermann\","
            + "\"billingAddress\":{"
            + "\"street\":\"Musterstraße 1\","
            + "\"city\":\"Berlin\","
            + "\"province\":\"Berlin\","
            + "\"zip\":\"10115\","
            + "\"countryCode\":\"DE\""
            + "}}";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);

        List<Row> rows = cqlSession.execute(
            "SELECT project_owner, project_care_of, project_billing_address_1,"
                + " project_billing_address_2, project_billing_address_3"
                + " FROM remsfal.quotation_requests WHERE issue_id = ?",
            UUID.fromString(issueId))
            .all();

        assertEquals(1, rows.size());
        assertEquals("Mustermann Verwaltung GmbH", rows.get(0).getString("project_owner"));
        assertEquals("Max Mustermann", rows.get(0).getString("project_care_of"));
        assertEquals("Musterstraße 1", rows.get(0).getString("project_billing_address_1"));
        assertEquals("10115 Berlin", rows.get(0).getString("project_billing_address_2"));
        assertEquals("Berlin, DE", rows.get(0).getString("project_billing_address_3"));
    }

    @Test
    void createRequestsForQuotation_FAILED_noPermission() {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\""
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
            + "\",\"companyName\":\"Test Contractor\"}] }";

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

    // --- Get Quotation Requests ---

    @Test
    void getRequestsForQuotation_SUCCESS_returnsListForManager() {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\""
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
            + "\",\"companyName\":\"Test GmbH\",\"organizationId\":\"" + organizationId + "\"}],"
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
            + "\"type\":\"TASK\""
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
            + "\"type\":\"TASK\""
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
            + "\",\"companyName\":\"Test GmbH\"}],\"scopeOfWork\":\"Anfrage.\" }";
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
            + "\"type\":\"TASK\""
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
            + "\",\"companyName\":\"Test GmbH\"}],\"scopeOfWork\":\"Original.\" }";
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
            + "\"type\":\"TASK\""
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
            .body("{ \"contractors\":[{\"id\":\"" + contractorId + "\",\"companyName\":\"Test GmbH\"}] }")
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

}
