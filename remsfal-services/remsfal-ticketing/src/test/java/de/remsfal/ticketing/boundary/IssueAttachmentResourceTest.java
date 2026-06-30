package de.remsfal.ticketing.boundary;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
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
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class IssueAttachmentResourceTest extends AbstractTicketingTest {

    static final String BASE_PATH = "/ticketing/v1/issues";

    @Test
    void downloadAttachment_SUCCESS_managerDownloadsAttachment() throws Exception {
        setupTestIssuesWithAttachment();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + TicketingTestData.ISSUE_ID_2 + "/attachments/"
                + TicketingTestData.ATTACHMENT_ID_1 + "/" + TicketingTestData.ATTACHMENT_FILE_PATH_1)
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", containsString("attachment"))
            .header("Content-Disposition", containsString(TicketingTestData.ATTACHMENT_FILE_PATH_1));
    }

    @Test
    void downloadAttachment_FAILED_noAuthentication() {
        setupTestIssues();

        given()
            .when()
            .get(BASE_PATH + "/" + TicketingTestData.ISSUE_ID_2 + "/attachments/"
                + TicketingTestData.ATTACHMENT_ID_1 + "/" + TicketingTestData.ATTACHMENT_FILE_PATH_1)
            .then()
            .statusCode(401);
    }

    @Test
    void downloadAttachment_FAILED_noPermission() {
        setupTestIssues();

        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "unauthorized@test.com",
                "Unauthorized", Map.of(), Map.of(), Map.of()))
            .get(BASE_PATH + "/" + TicketingTestData.ISSUE_ID_2 + "/attachments/"
                + TicketingTestData.ATTACHMENT_ID_1 + "/" + TicketingTestData.ATTACHMENT_FILE_PATH_1)
            .then()
            .statusCode(403);
    }

    @Test
    void downloadAttachment_FAILED_issueNotFound() {
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + UUID.randomUUID() + "/attachments/"
                + UUID.randomUUID() + "/test.png")
            .then()
            .statusCode(404);
    }

    @Test
    void downloadAttachment_FAILED_attachmentNotFound() {
        setupTestIssues();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + TicketingTestData.ISSUE_ID_2 + "/attachments/"
                + UUID.randomUUID() + "/test.png")
            .then()
            .statusCode(404);
    }

    @Test
    void deleteAttachment_SUCCESS_managerDeletesAttachment() throws Exception {
        setupTestIssuesWithAttachment();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + TicketingTestData.ISSUE_ID_2 + "/attachments/"
                + TicketingTestData.ATTACHMENT_ID_1)
            .then()
            .statusCode(204);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + TicketingTestData.ISSUE_ID_2 + "/attachments/"
                + TicketingTestData.ATTACHMENT_ID_1 + "/" + TicketingTestData.ATTACHMENT_FILE_PATH_1)
            .then()
            .statusCode(404);
    }

    @Test
    void deleteAttachment_FAILED_noAuthentication() throws Exception {
        setupTestIssuesWithAttachment();

        given()
            .when()
            .delete(BASE_PATH + "/" + TicketingTestData.ISSUE_ID_2 + "/attachments/"
                + TicketingTestData.ATTACHMENT_ID_1)
            .then()
            .statusCode(401);
    }

    @Test
    void deleteAttachment_FAILED_tenantCannotDelete() throws Exception {
        setupTestIssuesWithAttachment();

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                TicketingTestData.USER_FIRST_NAME_1, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + TicketingTestData.ISSUE_ID_2 + "/attachments/"
                + TicketingTestData.ATTACHMENT_ID_1)
            .then()
            .statusCode(403);
    }

    @Test
    void deleteAttachment_FAILED_noPermission() throws Exception {
        setupTestIssuesWithAttachment();

        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "unauthorized@test.com",
                "Unauthorized", Map.of(), Map.of(), Map.of()))
            .delete(BASE_PATH + "/" + TicketingTestData.ISSUE_ID_2 + "/attachments/"
                + TicketingTestData.ATTACHMENT_ID_1)
            .then()
            .statusCode(403);
    }

    @Test
    void deleteAttachment_FAILED_issueNotFound() {
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + UUID.randomUUID() + "/attachments/" + UUID.randomUUID())
            .then()
            .statusCode(404);
    }

    @Test
    void deleteAttachment_FAILED_attachmentNotFound() {
        setupTestIssues();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + TicketingTestData.ISSUE_ID_1 + "/attachments/"
                + UUID.randomUUID())
            .then()
            .statusCode(404);
    }

    @Test
    void deleteAttachment_FAILED_managerWithoutProjectPermission() throws Exception {
        setupTestIssuesWithAttachment();
        Map<String, String> otherProjectRoles = Map.of(UUID.randomUUID().toString(), "MANAGER");

        given()
            .when()
            .cookie(buildManagerCookie(otherProjectRoles))
            .delete(BASE_PATH + "/" + TicketingTestData.ISSUE_ID_2 + "/attachments/"
                + TicketingTestData.ATTACHMENT_ID_1)
            .then()
            .statusCode(403);
    }

    @Test
    void uploadAttachments_SUCCESS_managerUploadsSingleAttachment() {
        setupTestIssues();
        String issueId = TicketingTestData.ISSUE_ID_1.toString();
        InputStream fileStream = getTestFileStream(TicketingTestData.ATTACHMENT_FILE_PATH_1);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .multiPart("attachment", TicketingTestData.ATTACHMENT_FILE_PATH_1, fileStream,
                TicketingTestData.ATTACHMENT_FILE_TYPE_1)
            .post(BASE_PATH + "/" + issueId + "/attachments")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", hasSize(1))
            .body("[0].issueId", equalTo(issueId))
            .body("[0].attachmentId", notNullValue())
            .body("[0].fileName", equalTo(TicketingTestData.ATTACHMENT_FILE_PATH_1))
            .body("[0].contentType", startsWith(TicketingTestData.ATTACHMENT_FILE_TYPE_1))
            .body("[0].objectName", startsWith("/issues/"))
            .body("[0].uploaderId", notNullValue())
            .body("[0].uploadedBy", notNullValue())
            .body("[0].createdAt", notNullValue());
    }

    @Test
    void uploadAttachments_SUCCESS_managerUploadsMultipleAttachments() {
        setupTestIssues();
        String issueId = TicketingTestData.ISSUE_ID_3.toString();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .multiPart("attachment", TicketingTestData.ATTACHMENT_FILE_PATH_2,
                getTestFileStream(TicketingTestData.ATTACHMENT_FILE_PATH_2),
                TicketingTestData.ATTACHMENT_FILE_TYPE_2)
            .multiPart("attachment", TicketingTestData.ATTACHMENT_FILE_PATH_3,
                getTestFileStream(TicketingTestData.ATTACHMENT_FILE_PATH_3),
                TicketingTestData.ATTACHMENT_FILE_TYPE_3)
            .multiPart("attachment", TicketingTestData.ATTACHMENT_FILE_PATH_4,
                getTestFileStream(TicketingTestData.ATTACHMENT_FILE_PATH_4),
                TicketingTestData.ATTACHMENT_FILE_TYPE_4)
            .post(BASE_PATH + "/" + issueId + "/attachments")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", hasSize(3));
    }

    @Test
    void uploadAttachments_FAILED_noAuthentication() {
        given()
            .when()
            .multiPart("attachment", TicketingTestData.ATTACHMENT_FILE_PATH_1,
                getTestFileStream(TicketingTestData.ATTACHMENT_FILE_PATH_1),
                TicketingTestData.ATTACHMENT_FILE_TYPE_1)
            .post(BASE_PATH + "/" + UUID.randomUUID() + "/attachments")
            .then()
            .statusCode(401);
    }

    @Test
    void uploadAttachments_FAILED_tenantForbidden() {
        setupTestIssues();
        String issueId = TicketingTestData.ISSUE_ID_1.toString();

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                TicketingTestData.USER_NAME, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .multiPart("attachment", TicketingTestData.ATTACHMENT_FILE_PATH_1,
                getTestFileStream(TicketingTestData.ATTACHMENT_FILE_PATH_1),
                TicketingTestData.ATTACHMENT_FILE_TYPE_1)
            .post(BASE_PATH + "/" + issueId + "/attachments")
            .then()
            .statusCode(403);
    }

    @Test
    void uploadAttachments_FAILED_issueNotFound() {
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .multiPart("attachment", TicketingTestData.ATTACHMENT_FILE_PATH_1,
                getTestFileStream(TicketingTestData.ATTACHMENT_FILE_PATH_1),
                TicketingTestData.ATTACHMENT_FILE_TYPE_1)
            .post(BASE_PATH + "/" + UUID.randomUUID() + "/attachments")
            .then()
            .statusCode(404);
    }

}
