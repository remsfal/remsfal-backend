package de.remsfal.ticketing.boundary.contractor;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

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

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class ContractorIssueRequestResourceTest extends AbstractTicketingTest {

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
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(requestsPath())
            .then()
            .statusCode(201);

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
    void createRequest_FAILED_missingMessageField_characterizeActualStatusCode() {
        final String requestJson = "{ }";

        given()
            .when()
            .cookie(contractorCookie())
            .contentType(ContentType.JSON)
            .body(requestJson)
            .post(requestsPath())
            .then()
            .statusCode(201);
    }

    @Test
    void createRequest_FAILED_missingBody() {
        given()
            .when()
            .cookie(contractorCookie())
            .contentType(ContentType.JSON)
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
            .contentType(ContentType.JSON)
            .body(requestJson)
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
            .contentType(ContentType.JSON)
            .body(requestJson)
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
            .contentType(ContentType.JSON)
            .body(requestJson)
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
            .contentType(ContentType.JSON)
            .body(ownRequestJson)
            .post(requestsPath())
            .then()
            .statusCode(201);

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
            .contentType(ContentType.JSON)
            .body("{ \"message\":\"Fremde Anfrage\" }")
            .post(requestsPath())
            .then()
            .statusCode(201);

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
