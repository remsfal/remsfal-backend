package de.remsfal.ticketing.boundary.contractor;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

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
import io.restassured.http.Cookie;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class ContractorIssueRequestResourceTest extends AbstractTicketingTest {

    static final String ISSUE_BASE_PATH = "/ticketing/v1/issues";
    static final String ORDER_MANAGEMENT_PATH = "/ticketing/v1/order-management";
    static final String JSON_PART = MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString();

    final UUID organizationId = TicketingTestData.ORGANIZATION_ID;
    final UUID contractorUserId = UUID.randomUUID();

    String issueId;

    @BeforeEach
    void setUpIssueAndQuotationRequest() {
        issueId = createIssueWithQuotationRequest(true);
    }

    /**
     * Creates an issue (visible to a tenant depending on {@code visibleToTenants}) and registers a
     * quotation request for it on behalf of {@link #organizationId}, so {@link #contractorCookie()}
     * is an eligible contractor for it.
     */
    private String createIssueWithQuotationRequest(final boolean visibleToTenants) {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\","
            + (visibleToTenants ? "\"agreementId\":\"" + UUID.randomUUID() + "\"," : "")
            + "\"visibleToTenants\":" + visibleToTenants
            + "}";
        final String createdIssueId = given()
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
            .post(ISSUE_BASE_PATH + "/" + createdIssueId + "/quotation-request")
            .then()
            .statusCode(201);

        return createdIssueId;
    }

    private Cookie contractorCookie() {
        return buildCookie(contractorUserId, "contractor@test.com", "Contractor Manager",
            Map.of(), Map.of(organizationId.toString(), "MANAGER"), Map.of());
    }

    private String requestsPath() {
        return ORDER_MANAGEMENT_PATH + "/" + issueId + "/requests";
    }

    @Test
    void getRequests_SUCCESS_afterCreatingRequest() {
        final String requestJson = "{ \"message\":\"Bitte um Rueckmeldung\" }";
        given()
            .when()
            .cookie(contractorCookie())
            .multiPart("request", requestJson, JSON_PART)
            .post(requestsPath())
            .then()
            .statusCode(200);

        given()
            .when()
            .cookie(contractorCookie())
            .get(requestsPath())
            .then()
            .statusCode(200)
            .body("requests", hasSize(1))
            .body("requests[0].message", equalTo("Bitte um Rueckmeldung"))
            .body("requests[0].organizationId", equalTo(organizationId.toString()));
    }

    @Test
    void createRequest_SUCCESS_writesRequestCreatedContractorTimelineEntry() {
        final String requestJson = "{ \"message\":\"Bitte um Rueckmeldung\" }";
        given()
            .when()
            .cookie(contractorCookie())
            .multiPart("request", requestJson, JSON_PART)
            .post(requestsPath())
            .then()
            .statusCode(200);

        given()
            .when()
            .cookie(contractorCookie())
            .get(ORDER_MANAGEMENT_PATH + "/" + issueId + "/timeline")
            .then()
            .statusCode(200)
            .body("timelines", hasSize(2))
            .body("timelines[0].purpose", equalTo("QUOTATION_REQUESTED"))
            .body("timelines[1].purpose", equalTo("REQUEST_CREATED"))
            .body("timelines[1].message", equalTo("Bitte um Rueckmeldung"))
            .body("timelines[1].senderRole", equalTo("CONTRACTOR"));
    }

    @Test
    void createRequest_SUCCESS_withAttachment_isVisibleInBothTimelines() {
        final String requestJson = "{ \"message\":\"Bitte Plan pruefen\" }";
        final List<String> attachmentIds = given()
            .when()
            .cookie(contractorCookie())
            .multiPart("request", requestJson, JSON_PART)
            .multiPart("attachment", "plan.pdf", "fake-pdf-bytes".getBytes(), "application/pdf")
            .post(requestsPath())
            .then()
            .statusCode(200)
            .body("attachmentIds", hasSize(1))
            .extract().path("attachmentIds");

        given()
            .when()
            .cookie(contractorCookie())
            .get(ORDER_MANAGEMENT_PATH + "/" + issueId + "/timeline")
            .then()
            .statusCode(200)
            .body("timelines", hasSize(1))
            .body("timelines[0].attachments", hasSize(1))
            .body("timelines[0].attachments[0].fileName", equalTo("plan.pdf"))
            .body("timelines[0].attachments[0].attachmentId", not(equalTo(attachmentIds.get(0))));
    }

    @Test
    void createRequest_FAILED_attachmentIdsInRequestJson_returns400() {
        final String requestJson = "{ \"message\":\"Bitte Plan pruefen\","
            + " \"attachmentIds\":[\"" + UUID.randomUUID() + "\"] }";

        given()
            .when()
            .cookie(contractorCookie())
            .multiPart("request", requestJson, JSON_PART)
            .post(requestsPath())
            .then()
            .statusCode(400);
    }

    @Test
    void deleteRequest_SUCCESS_removesRequest() {
        final String requestJson = "{ \"message\":\"Bitte um Rueckmeldung\" }";
        final String issueRequestId = given()
            .when()
            .cookie(contractorCookie())
            .multiPart("request", requestJson, JSON_PART)
            .post(requestsPath())
            .then()
            .statusCode(200)
            .extract().path("issueRequestId");

        given()
            .when()
            .cookie(contractorCookie())
            .delete(requestsPath() + "/" + issueRequestId)
            .then()
            .statusCode(204);

        given()
            .when()
            .cookie(contractorCookie())
            .get(requestsPath())
            .then()
            .statusCode(200)
            .body("requests", hasSize(0));
    }

    @Test
    void deleteRequest_FAILED_unknownIssueRequestId_returns404() {
        given()
            .when()
            .cookie(contractorCookie())
            .delete(requestsPath() + "/" + UUID.randomUUID())
            .then()
            .statusCode(404);
    }

    @Test
    void deleteRequest_FAILED_staffOrgRole_returns403() {
        final String requestJson = "{ \"message\":\"Bitte um Rueckmeldung\" }";
        final String issueRequestId = given()
            .when()
            .cookie(contractorCookie())
            .multiPart("request", requestJson, JSON_PART)
            .post(requestsPath())
            .then()
            .statusCode(200)
            .extract().path("issueRequestId");

        given()
            .when()
            .cookie(buildCookie(contractorUserId, "contractor@test.com", "Contractor Staff",
                Map.of(), Map.of(organizationId.toString(), "STAFF"), Map.of()))
            .delete(requestsPath() + "/" + issueRequestId)
            .then()
            .statusCode(403);
    }

    @Test
    void deleteRequest_FAILED_organizationHasNoQuotationRequestForIssue_returns404() {
        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "other@test.com", "Other Contractor",
                Map.of(), Map.of(UUID.randomUUID().toString(), "MANAGER"), Map.of()))
            .delete(requestsPath() + "/" + UUID.randomUUID())
            .then()
            .statusCode(404);
    }

    @Test
    void deleteRequest_FAILED_unauthenticated_returns401() {
        given()
            .when()
            .delete(requestsPath() + "/" + UUID.randomUUID())
            .then()
            .statusCode(401);
    }

    @Test
    void createRequest_FAILED_missingMessageField_returns400() {
        final String requestJson = "{ }";

        given()
            .when()
            .cookie(contractorCookie())
            .multiPart("request", requestJson, JSON_PART)
            .post(requestsPath())
            .then()
            .statusCode(400);
    }

    @Test
    void createRequest_FAILED_issueNotVisibleToTenant_returns400() {
        final String notVisibleIssueId = createIssueWithQuotationRequest(false);
        final String requestJson = "{ \"message\":\"Bitte um Rueckmeldung\" }";

        given()
            .when()
            .cookie(contractorCookie())
            .multiPart("request", requestJson, JSON_PART)
            .post(ORDER_MANAGEMENT_PATH + "/" + notVisibleIssueId + "/requests")
            .then()
            .statusCode(400);
    }

    @Test
    void createRequest_FAILED_missingRequestPart_returns400() {
        given()
            .when()
            .cookie(contractorCookie())
            .multiPart("attachment", "plan.pdf", "fake-pdf-bytes".getBytes(), "application/pdf")
            .post(requestsPath())
            .then()
            .statusCode(400);
    }

    @Test
    void getRequests_FAILED_staffOrgRole_returns403() {
        given()
            .when()
            .cookie(buildCookie(contractorUserId, "contractor@test.com", "Contractor Staff",
                Map.of(), Map.of(organizationId.toString(), "STAFF"), Map.of()))
            .get(requestsPath())
            .then()
            .statusCode(403);
    }

    @Test
    void createRequest_FAILED_staffOrgRole_returns403() {
        final String requestJson = "{ \"message\":\"Bitte um Rueckmeldung\" }";

        given()
            .when()
            .cookie(buildCookie(contractorUserId, "contractor@test.com", "Contractor Staff",
                Map.of(), Map.of(organizationId.toString(), "STAFF"), Map.of()))
            .multiPart("request", requestJson, JSON_PART)
            .post(requestsPath())
            .then()
            .statusCode(403);
    }

    @Test
    void getRequests_FAILED_organizationHasNoQuotationRequestForIssue_returns404() {
        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "other@test.com", "Other Contractor",
                Map.of(), Map.of(UUID.randomUUID().toString(), "MANAGER"), Map.of()))
            .get(requestsPath())
            .then()
            .statusCode(404);
    }

    @Test
    void createRequest_FAILED_organizationHasNoQuotationRequestForIssue_returns404() {
        final String requestJson = "{ \"message\":\"Bitte um Rueckmeldung\" }";

        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "other@test.com", "Other Contractor",
                Map.of(), Map.of(UUID.randomUUID().toString(), "MANAGER"), Map.of()))
            .multiPart("request", requestJson, JSON_PART)
            .post(requestsPath())
            .then()
            .statusCode(404);
    }

    @Test
    void getRequests_FAILED_unauthenticated_returns401() {
        given()
            .when()
            .get(requestsPath())
            .then()
            .statusCode(401);
    }

    @Test
    void createRequest_FAILED_unauthenticated_returns401() {
        final String requestJson = "{ \"message\":\"Bitte um Rueckmeldung\" }";

        given()
            .when()
            .multiPart("request", requestJson, JSON_PART)
            .post(requestsPath())
            .then()
            .statusCode(401);
    }

    @Test
    void getRequests_SUCCESS_scopedToCallingOrganizationOnly() {
        final String ownRequestJson = "{ \"message\":\"Eigene Anfrage\" }";
        given()
            .when()
            .cookie(contractorCookie())
            .multiPart("request", ownRequestJson, JSON_PART)
            .post(requestsPath())
            .then()
            .statusCode(200);

        final UUID otherOrganizationId = UUID.randomUUID();
        final String otherRequestJson = "{ \"contractors\":[{\"id\":\"" + UUID.randomUUID()
            + "\",\"name\":\"Andere Firma\",\"organizationId\":\"" + otherOrganizationId + "\"}] }";
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(otherRequestJson)
            .post(ISSUE_BASE_PATH + "/" + issueId + "/quotation-request")
            .then()
            .statusCode(201);
        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "other@test.com", "Other Contractor",
                Map.of(), Map.of(otherOrganizationId.toString(), "MANAGER"), Map.of()))
            .multiPart("request", "{ \"message\":\"Fremde Anfrage\" }", JSON_PART)
            .post(requestsPath())
            .then()
            .statusCode(200);

        given()
            .when()
            .cookie(contractorCookie())
            .get(requestsPath())
            .then()
            .statusCode(200)
            .body("requests", hasSize(1))
            .body("requests[0].message", equalTo("Eigene Anfrage"));
    }

}
