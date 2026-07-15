package de.remsfal.ticketing.boundary.tenant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.model.ticketing.tenant.MessagePurpose;
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

    static final String BASE_PATH = "/ticketing/v1/tenant-relations/issues";

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

        final String issueId = given()
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
            .extract().path("id");

        // TenantIssueJson never carries attachments (issue #801) — they're only surfaced through the
        // issue-creation timeline entry, which the resource must link them into. Attachment order
        // within an entry isn't guaranteed, so assert membership rather than positions.
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                TicketingTestData.USER_NAME, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueId + "/timeline")
            .then()
            .statusCode(200)
            .body("timelines", hasSize(1))
            .body("timelines[0].purpose", equalTo("ISSUE_CREATED"))
            .body("timelines[0].attachments", hasSize(3))
            .body("timelines[0].attachments.fileName", containsInAnyOrder(
                TicketingTestData.ATTACHMENT_FILE_PATH_2, TicketingTestData.ATTACHMENT_FILE_PATH_3,
                TicketingTestData.ATTACHMENT_FILE_PATH_4))
            .body("timelines[0].attachments.uploaderId", everyItem(equalTo(TicketingTestData.USER_ID.toString())));
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
            .body("projectId", nullValue());
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
        // only attachments referenced by a TenantTimelineEntity are visible to the tenant (issue #801)
        insertTimelineEntry(TicketingTestData.ISSUE_ID_2, TicketingTestData.PROJECT_ID_1,
            TicketingTestData.AGREEMENT_ID_1, UUID.randomUUID(), MessagePurpose.ISSUE_CREATED,
            List.of(TicketingTestData.ATTACHMENT_ID_1));

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
    void downloadAttachment_FAILED_attachmentNotVisibleToTenant() throws Exception {
        setupTestIssuesWithAttachment();
        // no TenantTimelineEntity references ATTACHMENT_ID_1 here, so it stays invisible to the tenant

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                TicketingTestData.USER_FIRST_NAME, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .get(BASE_PATH + "/" + TicketingTestData.ISSUE_ID_2 + "/attachments/"
                + TicketingTestData.ATTACHMENT_ID_1 + "/" + TicketingTestData.ATTACHMENT_FILE_PATH_1)
            .then()
            .statusCode(403);
    }

    @Test
    void downloadAttachment_FAILED_managerCannotUseTenantEndpoint() throws Exception {
        setupTestIssuesWithAttachment();
        insertTimelineEntry(TicketingTestData.ISSUE_ID_2, TicketingTestData.PROJECT_ID_1,
            TicketingTestData.AGREEMENT_ID_1, UUID.randomUUID(), MessagePurpose.ISSUE_CREATED,
            List.of(TicketingTestData.ATTACHMENT_ID_1));

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
