package de.remsfal.ticketing.boundary;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.TicketingTestData;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class TenantIssueResourceTest extends AbstractTicketingTest {

    static final String BASE_PATH = "/ticketing/v1/tenant/issues";

    private static final String MANAGER_ISSUE_PATH = "/ticketing/v1/issues";

    // --- Create Issue With Attachments ---

    @Test
    void createIssueWithAttachments_SUCCESS_issueWithAttachmentsIsCreated() {
        final String issueJson = "{ \"agreementId\":\"" + TicketingTestData.AGREEMENT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE_3 + "\","
            + "\"type\":\"DEFECT\","
            + "\"category\":\"WATER_DAMAGE\","
            + "\"description\":\"" + TicketingTestData.ISSUE_DESCRIPTION_3 + "\""
            + "}";
        InputStream attachmentStream2 = getTestFileStream(TicketingTestData.ATTACHMENT_FILE_PATH_2);
        InputStream attachmentStream3 = getTestFileStream(TicketingTestData.ATTACHMENT_FILE_PATH_3);
        InputStream attachmentStream4 = getTestFileStream(TicketingTestData.ATTACHMENT_FILE_PATH_4);

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                TicketingTestData.USER_NAME, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .multiPart("issue", issueJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .multiPart("attachment", TicketingTestData.ATTACHMENT_FILE_PATH_2,
                attachmentStream2, TicketingTestData.ATTACHMENT_FILE_TYPE_2)
            .multiPart("attachment", TicketingTestData.ATTACHMENT_FILE_PATH_3,
                attachmentStream3, TicketingTestData.ATTACHMENT_FILE_TYPE_3)
            .multiPart("attachment", TicketingTestData.ATTACHMENT_FILE_PATH_4,
                attachmentStream4, TicketingTestData.ATTACHMENT_FILE_TYPE_4)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("projectId", nullValue())
            .body("title", equalTo(TicketingTestData.ISSUE_TITLE_3))
            .body("type", equalTo("DEFECT"))
            .body("status", equalTo("PENDING"))
            .body("category", equalTo("WATER_DAMAGE"))
            .body("priority", nullValue())
            .body("assigneeId", nullValue())
            .body("agreementId", equalTo(TicketingTestData.AGREEMENT_ID.toString()))
            .body("attachments", hasSize(3))
            .body("attachments[0].issueId", notNullValue())
            .body("attachments[0].attachmentId", notNullValue())
            .body("attachments[0].fileName", equalTo(TicketingTestData.ATTACHMENT_FILE_PATH_2))
            .body("attachments[0].contentType", startsWith(TicketingTestData.ATTACHMENT_FILE_TYPE_2))
            .body("attachments[0].objectName", startsWith("/issues/"))
            .body("attachments[0].objectName", endsWith(TicketingTestData.ATTACHMENT_FILE_PATH_2))
            .body("attachments[0].uploaderId", equalTo(TicketingTestData.USER_ID.toString()))
            .body("attachments[0].uploadedBy", equalTo(TicketingTestData.USER_NAME))
            .body("attachments[0].createdAt", notNullValue())
            .body("attachments[1].issueId", notNullValue())
            .body("attachments[1].attachmentId", notNullValue())
            .body("attachments[1].fileName", equalTo(TicketingTestData.ATTACHMENT_FILE_PATH_3))
            .body("attachments[1].contentType", startsWith(TicketingTestData.ATTACHMENT_FILE_TYPE_3))
            .body("attachments[2].issueId", notNullValue())
            .body("attachments[2].attachmentId", notNullValue())
            .body("attachments[2].fileName", equalTo(TicketingTestData.ATTACHMENT_FILE_PATH_4))
            .body("attachments[2].contentType", startsWith(TicketingTestData.ATTACHMENT_FILE_TYPE_4));
    }

    @Test
    void createIssueWithAttachments_SUCCESS_issueWithoutAttachmentsIsCreated() {
        final String issueJson = "{ \"agreementId\":\"" + TicketingTestData.AGREEMENT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE_4 + "\","
            + "\"type\":\"TERMINATION\","
            + "\"description\":\"" + TicketingTestData.ISSUE_DESCRIPTION_4 + "\""
            + "}";

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                TicketingTestData.USER_NAME, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .multiPart("issue", issueJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("title", equalTo(TicketingTestData.ISSUE_TITLE_4))
            .body("type", equalTo("TERMINATION"))
            .body("status", equalTo("PENDING"))
            .body("projectId", nullValue())
            .body("attachments", hasSize(0));
    }

    @Test
    void createIssueWithAttachments_FAILED_noAuthentication() {
        final String issueJson = "{ \"agreementId\":\"" + TicketingTestData.AGREEMENT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE_4 + "\","
            + "\"type\":\"TERMINATION\","
            + "\"description\":\"" + TicketingTestData.ISSUE_DESCRIPTION_4 + "\""
            + "}";

        given()
            .when()
            .multiPart("issue", issueJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(BASE_PATH)
            .then()
            .statusCode(401);
    }

    @Test
    void createIssueWithAttachments_FAILED_missingTypeField() {
        final String issueJson = "{ \"agreementId\":\"" + TicketingTestData.AGREEMENT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE_4 + "\","
            + "\"description\":\"" + TicketingTestData.ISSUE_DESCRIPTION_4 + "\""
            + "}";

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                TicketingTestData.USER_NAME, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .multiPart("issue", issueJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(BASE_PATH)
            .then()
            .statusCode(400);
    }

    @Test
    void createIssueWithAttachments_FAILED_unrelatedAgreement() {
        final String issueJson = "{ \"agreementId\":\"" + UUID.randomUUID() + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE_4 + "\","
            + "\"type\":\"TERMINATION\","
            + "\"description\":\"" + TicketingTestData.ISSUE_DESCRIPTION_4 + "\""
            + "}";

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                TicketingTestData.USER_NAME, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .multiPart("issue", issueJson, MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8").toString())
            .post(BASE_PATH)
            .then()
            .statusCode(403);
    }

    // --- List Issues ---

    @Test
    void getIssues_SUCCESS_aggregatesVisibleIssuesAcrossTenantAgreements() {
        final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"Agreement Specific Issue\","
            + "\"type\":\"TASK\","
            + "\"agreementId\":\"" + TicketingTestData.AGREEMENT_ID + "\","
            + "\"visibleToTenants\":true"
            + "}";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(createJson)
            .post(MANAGER_ISSUE_PATH)
            .then()
            .statusCode(201);

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                TicketingTestData.USER_FIRST_NAME_1, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .get(BASE_PATH)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("issues", hasSize(1))
            .body("issues[0].title", equalTo("Agreement Specific Issue"))
            .body("nextCursor", nullValue());
    }

    @Test
    void getIssues_SUCCESS_returnsEmptyListForUserWithNoTenancies() {
        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "contractor@test.com",
                "Contractor", Map.of(), Map.of(), Map.of()))
            .get(BASE_PATH)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("issues", hasSize(0))
            .body("size", equalTo(0));
    }

    @Test
    void getIssues_FAILED_noAuthentication() {
        given()
            .when()
            .get(BASE_PATH)
            .then()
            .statusCode(401);
    }

    // --- Get Issue ---

    @Test
    void getIssue_SUCCESS_tenantGetsOwnIssue() {
        final String issueId = createManagerVisibleIssueForTenant("Tenant Detail Issue");

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                TicketingTestData.USER_FIRST_NAME_1, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueId)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(issueId))
            .body("title", equalTo("Tenant Detail Issue"))
            .body("projectId", nullValue())
            .body("priority", nullValue())
            .body("assigneeId", nullValue());
    }

    @Test
    void getIssue_FAILED_tenantCannotAccessIssueOfUnrelatedAgreement() {
        final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"Unrelated Agreement Issue\","
            + "\"type\":\"TASK\","
            + "\"agreementId\":\"" + UUID.randomUUID() + "\","
            + "\"visibleToTenants\":true"
            + "}";

        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(createJson)
            .post(MANAGER_ISSUE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                TicketingTestData.USER_FIRST_NAME_1, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueId)
            .then()
            .statusCode(403);
    }

    @Test
    void getIssue_FAILED_managerCannotUseTenantEndpoint() {
        final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"Manager Owned Issue\","
            + "\"type\":\"TASK\""
            + "}";

        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(createJson)
            .post(MANAGER_ISSUE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueId)
            .then()
            .statusCode(403);
    }

    // --- Close Issue ---

    @Test
    void closeIssue_SUCCESS_setsStatusToClosed() {
        final String issueId = createManagerVisibleIssueForTenant("Issue To Close");

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                TicketingTestData.USER_FIRST_NAME_1, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + issueId)
            .then()
            .statusCode(204);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(MANAGER_ISSUE_PATH + "/" + issueId)
            .then()
            .statusCode(200)
            .body("status", equalTo("CLOSED"));
    }

    @Test
    void closeIssue_FAILED_tenantCannotCloseIssueOfUnrelatedAgreement() {
        final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"Unrelated Agreement Issue\","
            + "\"type\":\"TASK\","
            + "\"agreementId\":\"" + UUID.randomUUID() + "\","
            + "\"visibleToTenants\":true"
            + "}";

        final String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(createJson)
            .post(MANAGER_ISSUE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                TicketingTestData.USER_FIRST_NAME_1, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + issueId)
            .then()
            .statusCode(403);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(MANAGER_ISSUE_PATH + "/" + issueId)
            .then()
            .statusCode(200)
            .body("status", equalTo("OPEN"));
    }

    @Test
    void closeIssue_FAILED_noAuthentication() {
        given()
            .when()
            .delete(BASE_PATH + "/" + UUID.randomUUID())
            .then()
            .statusCode(401);
    }

    // --- Download Attachment ---

    @Test
    void downloadAttachment_SUCCESS_tenantDownloadsAttachment() throws Exception {
        setupTestIssuesWithAttachment();

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                TicketingTestData.USER_FIRST_NAME, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .get(BASE_PATH + "/" + TicketingTestData.ISSUE_ID_2 + "/attachments/"
                + TicketingTestData.ATTACHMENT_ID_1 + "/" + TicketingTestData.ATTACHMENT_FILE_PATH_1)
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", containsString("attachment"));
    }

    @Test
    void downloadAttachment_FAILED_managerCannotUseTenantEndpoint() throws Exception {
        setupTestIssuesWithAttachment();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + TicketingTestData.ISSUE_ID_2 + "/attachments/"
                + TicketingTestData.ATTACHMENT_ID_1 + "/" + TicketingTestData.ATTACHMENT_FILE_PATH_1)
            .then()
            .statusCode(403);
    }

    // --- Helper ---

    private String createManagerVisibleIssueForTenant(final String title) {
        final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + title + "\","
            + "\"type\":\"TASK\","
            + "\"agreementId\":\"" + TicketingTestData.AGREEMENT_ID + "\","
            + "\"visibleToTenants\":true"
            + "}";

        final Response createResponse = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(createJson)
            .post(MANAGER_ISSUE_PATH)
            .thenReturn();

        return createResponse.then()
            .statusCode(201)
            .extract().path("id");
    }

}
