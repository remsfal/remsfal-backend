package de.remsfal.ticketing.boundary;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.json.ticketing.ImmutableIssueJson;
import de.remsfal.ticketing.TicketingTestData;
import de.remsfal.ticketing.control.IssueController;
import de.remsfal.ticketing.control.QuotationController;
import de.remsfal.ticketing.entity.dao.QuotationRepository;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.inject.Inject;

/**
 * Test for QuotationResource.
 * 
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class QuotationResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/ticketing/v1/issues";
    static final String QUOTATION_TEXT = "Dear Sir/Madam, we hereby submit our quotation for the requested work. The total cost is 1500 EUR. Best regards";

    @Inject
    IssueController issueController;

    @Inject
    QuotationController quotationController;

    @Inject
    QuotationRepository quotationRepository;

    private UUID createTestIssue() {
        // Create issue via REST API
        final String json = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Request for quotation\","
                + "\"description\":\"We need a quotation for heating repair\","
                + "\"type\":\"TASK\""
                + "}";

        Response response = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, 
                    TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(json)
                .post(BASE_PATH);

        return UUID.fromString(response.jsonPath().getString("id"));
    }

    @Test
    void createQuotation_FAILED_noAuthentication() {
        UUID issueId = createTestIssue();
        final String json = "{ \"text\":\"" + QUOTATION_TEXT + "\" }";

        given()
            .when()
            .contentType(ContentType.JSON)
            .body(json)
            .post(BASE_PATH + "/" + issueId + "/quotation")
            .then()
            .statusCode(401);
    }

    @Test
    void createQuotation_SUCCESS_quotationIsCreated() {
        UUID issueId = createTestIssue();
        final String json = "{ \"text\":\"" + QUOTATION_TEXT + "\" }";

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1, 
                TicketingTestData.USER_FIRST_NAME_1, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
            .contentType(ContentType.JSON)
            .body(json)
            .post(BASE_PATH + "/" + issueId + "/quotation")
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .header("location", containsString("/quotation/"))
            .body("id", notNullValue())
            .body("projectId", equalTo(TicketingTestData.PROJECT_ID.toString()))
            .body("issueId", equalTo(issueId.toString()))
            .body("contractorId", equalTo(TicketingTestData.USER_ID_1.toString()))
            .body("requesterId", equalTo(TicketingTestData.USER_ID.toString()))
            .body("text", equalTo(QUOTATION_TEXT))
            .body("status", equalTo("VALID"))
            .body("createdAt", notNullValue());
    }

    @Test
    void createQuotation_FAILED_noText() {
        UUID issueId = createTestIssue();
        final String json = "{ }";

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1, 
                TicketingTestData.USER_FIRST_NAME_1, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
            .contentType(ContentType.JSON)
            .body(json)
            .post(BASE_PATH + "/" + issueId + "/quotation")
            .then()
            .statusCode(400);
    }

    @Test
    void createQuotation_FAILED_issueDoesNotExist() {
        final String json = "{ \"text\":\"" + QUOTATION_TEXT + "\" }";
        UUID nonExistentIssueId = UUID.randomUUID();

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1, 
                TicketingTestData.USER_FIRST_NAME_1, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
            .contentType(ContentType.JSON)
            .body(json)
            .post(BASE_PATH + "/" + nonExistentIssueId + "/quotation")
            .then()
            .statusCode(404);
    }

}
