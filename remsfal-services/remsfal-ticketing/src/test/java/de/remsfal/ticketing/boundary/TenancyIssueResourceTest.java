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
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.TicketingTestData;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class TenancyIssueResourceTest extends AbstractTicketingTest {

    static final String BASE_PATH = "/ticketing/v1/issues";

    // --- Create Tenancy Issue ---

    @Test
    void createTenancyIssueWithAttachments_SUCCESS_issueWithAttachmentsIsCreated() {
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
            .body("agreementId", equalTo(TicketingTestData.AGREEMENT_ID.toString()))
            .body("attachments", hasSize(3))
            .body("attachments[0].issueId", notNullValue())
            .body("attachments[0].attachmentId", notNullValue())
            .body("attachments[0].fileName", equalTo(TicketingTestData.ATTACHMENT_FILE_PATH_2))
            .body("attachments[0].contentType", startsWith(TicketingTestData.ATTACHMENT_FILE_TYPE_2))
            .body("attachments[0].objectName", startsWith("/issues/"))
            .body("attachments[0].objectName", endsWith(TicketingTestData.ATTACHMENT_FILE_PATH_2))
            .body("attachments[0].uploadedBy", equalTo(TicketingTestData.USER_ID.toString()))
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
    void createTenancyIssueWithAttachments_SUCCESS_issueWithoutAttachmentsIsCreated() {
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
            .body("isVisibleForTenant", nullValue())
            .body("projectId", nullValue())
            .body("attachments", hasSize(0));
    }

    @Test
    void createTenancyIssueWithAttachments_FAILED_noAuthentication() {
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
    void createTenancyIssueWithAttachments_FAILED_missingTypeFields() {
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

    // --- List Issues (Tenant) ---

    @Test
    void getIssues_SUCCESS_tenantWithUnauthorizedTenancyIdIsIgnored() {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                TicketingTestData.USER_FIRST_NAME_1, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .queryParam("agreementId", UUID.randomUUID().toString())
            .get(BASE_PATH)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("issues", hasSize(0))
            .body("size", equalTo(0));
    }

    @Test
    void getIssues_SUCCESS_tenantWithoutSpecificTenancyId() {
        final String createJson1 = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"Tenant Issue 1\","
            + "\"type\":\"TASK\""
            + "}";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(createJson1)
            .post(BASE_PATH);

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                TicketingTestData.USER_FIRST_NAME_1, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .get(BASE_PATH)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    void getIssues_SUCCESS_tenantFiltersByStatus() {
        final String createJson1 = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"Open Issue\","
            + "\"type\":\"DEFECT\","
            + "\"agreementId\":\"" + TicketingTestData.AGREEMENT_ID + "\""
            + "}";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(createJson1)
            .post(BASE_PATH)
            .then()
            .statusCode(201);

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                TicketingTestData.USER_FIRST_NAME_1, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .queryParam("status", "OPEN")
            .get(BASE_PATH)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("issues", hasSize(1))
            .body("issues[0].title", equalTo("Open Issue"));
    }

    @Test
    void getIssues_SUCCESS_handlesNullIssuesInDeduplication() {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                TicketingTestData.USER_FIRST_NAME_1, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .get(BASE_PATH)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    void getIssues_SUCCESS_handlesIssueWithNullId() {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                TicketingTestData.USER_FIRST_NAME_1, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .get(BASE_PATH)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    void getIssues_SUCCESS_agreementIssuesForMember() {
        given()
            .when()
            .cookie(buildCookie(
                TicketingTestData.USER_ID,
                TicketingTestData.USER_EMAIL,
                TicketingTestData.USER_FIRST_NAME,
                Map.of(), Map.of(),
                TicketingTestData.TENANT_PROJECT_ROLES))
            .queryParam("agreementId", TicketingTestData.AGREEMENT_ID.toString())
            .get(BASE_PATH)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("issues", hasSize(0))
            .body("size", equalTo(0));
    }

    @Test
    void getIssues_SUCCESS_tenantWithSpecificAgreementIdGetsIssues() {
        final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"Agreement Specific Issue\","
            + "\"type\":\"TASK\""
            + "}";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(createJson)
            .post(BASE_PATH);

        UUID agreementId = UUID.fromString(TicketingTestData.TENANT_PROJECT_ROLES.keySet().iterator().next());

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                TicketingTestData.USER_FIRST_NAME_1, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .queryParam("agreementId", agreementId.toString())
            .get(BASE_PATH)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @ParameterizedTest
    @MethodSource("provideIssueTestCases")
    void getIssues_SUCCESS_variousScenarios(String title, String description) {
        final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + title + "\","
            + "\"type\":\"TASK\""
            + "}";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(createJson)
            .post(BASE_PATH);

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                TicketingTestData.USER_FIRST_NAME_1, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .get(BASE_PATH)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    private static Stream<Arguments> provideIssueTestCases() {
        return Stream.of(
            Arguments.of("Dedupe Issue", "Deduplication of issues"),
            Arguments.of("Combined Role Issue", "User with both tenant and contractor roles"),
            Arguments.of("No Filter Issue", "Without status filter"));
    }

    // --- Contractor Access ---

    @Test
    void getIssues_SUCCESS_contractorAsParticipantWithDeletedIssue() {
        final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"Contractor Issue\","
            + "\"type\":\"TASK\""
            + "}";

        String issueId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(createJson)
            .post(BASE_PATH)
            .thenReturn()
            .then()
            .contentType(MediaType.APPLICATION_JSON)
            .extract().path("id");

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + issueId)
            .then()
            .statusCode(204);

        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "contractor@test.com",
                "Contractor", Map.of(), Map.of(), Map.of()))
            .get(BASE_PATH)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    void getIssues_SUCCESS_contractorWithNoParticipations() {
        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "contractor@test.com",
                "Contractor", Map.of(), Map.of(), Map.of()))
            .get(BASE_PATH)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    // --- Download Attachment (Tenant) ---

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

}
