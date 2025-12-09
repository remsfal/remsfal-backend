package de.remsfal.ticketing.boundary;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;


import java.util.Map;
import java.util.UUID;

import de.remsfal.ticketing.entity.dto.IssueEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.ticketing.TicketingTestData;
import de.remsfal.ticketing.control.IssueController;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import de.remsfal.ticketing.entity.dto.IssueKey;
import de.remsfal.core.model.ticketing.IssueModel.Status;


@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class IssueResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/ticketing/v1/issues";

    @Inject
    IssueController issueController;

    @Inject
    IssueRepository issueRepository;

    @Test
    void getIssues_FAILED_noAuthentication() {
        given()
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(401);
    }

    @Test
    void getIssues_SUCCESS_emptyListWhenNoIssues() {
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, 
                    TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("issues", hasSize(0))
                .body("first", equalTo(0))
                .body("size", equalTo(0))
                .body("total", equalTo(0));
    }

    @Test
    void createIssue_SUCCESS_issueIsCreated() {
        final String json = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
                + "\"description\":\"" + TicketingTestData.ISSUE_DESCRIPTION + "\","
                + "\"type\":\"TASK\""
                + "}";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, 
                    TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(json)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .header("location", containsString(BASE_PATH + "/"))
                .body("id", notNullValue())
                .body("title", equalTo(TicketingTestData.ISSUE_TITLE))
                .body("description", equalTo(TicketingTestData.ISSUE_DESCRIPTION.replace("\\n", "\n")))
                .body("type", equalTo("TASK"))
                .body("status", equalTo("OPEN"))
                .body("projectId", equalTo(TicketingTestData.PROJECT_ID.toString()));
    }

    @Test
    void createIssue_SUCCESS_tenantCreatesPendingIssue() {
        final String json = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"" + TicketingTestData.ISSUE_TITLE_2 + "\","
                + "\"description\":\"" + TicketingTestData.ISSUE_DESCRIPTION_2 + "\","
                + "\"type\":\"DEFECT\""
                + "}";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1, 
                    TicketingTestData.USER_FIRST_NAME_1, Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
                .contentType(ContentType.JSON)
                .body(json)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("title", equalTo(TicketingTestData.ISSUE_TITLE_2))
                .body("type", equalTo("DEFECT"))
                .body("status", equalTo("PENDING"))
                .body("projectId", equalTo(TicketingTestData.PROJECT_ID.toString()))
                .body("description", nullValue()); // Filtered for tenant view
    }

    @Test
    void createIssue_FAILED_noAuthentication() {
        final String json = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
                + "\"type\":\"TASK\""
                + "}";

        given()
                .when()
                .contentType(ContentType.JSON)
                .body(json)
                .post(BASE_PATH)
                .then()
                .statusCode(401);
    }

    @Test
    void createIssue_FAILED_noTitle() {
        final String json = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"type\":\"TASK\""
                + "}";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, 
                    TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(json)
                .post(BASE_PATH)
                .then()
                .statusCode(400);
    }

    @Test
    void createIssue_FAILED_noProjectPermission() {
        final String json = "{ \"projectId\":\"" + UUID.randomUUID() + "\","
                + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
                + "\"type\":\"TASK\""
                + "}";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, 
                    TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(json)
                .post(BASE_PATH)
                .then()
                .statusCode(403);
    }

    @Test
    void getIssue_SUCCESS_issueIsReturned() {
        // First create an issue
        final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
                + "\"description\":\"" + TicketingTestData.ISSUE_DESCRIPTION + "\","
                + "\"type\":\"TASK\""
                + "}";

        final Response createResponse = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, 
                    TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(createJson)
                .post(BASE_PATH)
                .thenReturn();

        final String issueId = createResponse.then()
                .contentType(MediaType.APPLICATION_JSON)
                .extract().path("id");

        // Then retrieve the issue
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, 
                    TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + issueId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(issueId))
                .body("title", equalTo(TicketingTestData.ISSUE_TITLE))
                .body("description", equalTo(TicketingTestData.ISSUE_DESCRIPTION.replace("\\n", "\n")))
                .body("type", equalTo("TASK"))
                .body("status", equalTo("OPEN"));
    }

    @Test
    void getIssue_FAILED_noAuthentication() {
        given()
                .when()
                .get(BASE_PATH + "/" + TicketingTestData.ISSUE_ID_1)
                .then()
                .statusCode(401);
    }

    @Test
    void getIssue_FAILED_issueNotFound() {
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, 
                    TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + UUID.randomUUID())
                .then()
                .statusCode(404);
    }

    @Test
    void updateIssue_SUCCESS_issueIsUpdated() {
        // First create an issue
        final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
                + "\"description\":\"" + TicketingTestData.ISSUE_DESCRIPTION + "\","
                + "\"type\":\"TASK\""
                + "}";

        final Response createResponse = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, 
                    TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(createJson)
                .post(BASE_PATH)
                .thenReturn();

        final String issueId = createResponse.then()
                .contentType(MediaType.APPLICATION_JSON)
                .extract().path("id");

        // Then update the issue
        final String updateJson = "{ \"title\":\"" + TicketingTestData.ISSUE_TITLE_2 + "\","
                + "\"status\":\"IN_PROGRESS\""
                + "}";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, 
                    TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(updateJson)
                .patch(BASE_PATH + "/" + issueId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(issueId))
                .body("title", equalTo(TicketingTestData.ISSUE_TITLE_2))
                .body("status", equalTo("IN_PROGRESS"));
    }

    @Test
    void updateIssue_FAILED_noAuthentication() {
        final String updateJson = "{ \"title\":\"Updated Title\" }";

        given()
                .when()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .patch(BASE_PATH + "/" + TicketingTestData.ISSUE_ID_1)
                .then()
                .statusCode(401);
    }

    @Test
    void deleteIssue_SUCCESS_issueIsDeleted() {
        // First create an issue
        final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
                + "\"description\":\"" + TicketingTestData.ISSUE_DESCRIPTION + "\","
                + "\"type\":\"TASK\""
                + "}";

        final Response createResponse = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, 
                    TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(createJson)
                .post(BASE_PATH)
                .thenReturn();

        final String issueId = createResponse.then()
                .contentType(MediaType.APPLICATION_JSON)
                .extract().path("id");

        // Then delete the issue
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, 
                    TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .delete(BASE_PATH + "/" + issueId)
                .then()
                .statusCode(204);

        // Verify issue is deleted
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, 
                    TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + issueId)
                .then()
                .statusCode(404);
    }

    @Test
    void deleteIssue_FAILED_noAuthentication() {
        given()
                .when()
                .delete(BASE_PATH + "/" + TicketingTestData.ISSUE_ID_1)
                .then()
                .statusCode(401);
    }

    @Test
    void getIssues_SUCCESS_withFilters() {
        // Create multiple issues with different statuses
        final String issue1Json = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"" + TicketingTestData.ISSUE_TITLE_1 + "\","
                + "\"type\":\"TASK\""
                + "}";

        final String issue2Json = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"" + TicketingTestData.ISSUE_TITLE_2 + "\","
                + "\"type\":\"DEFECT\""
                + "}";

        // Create first issue
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, 
                    TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(issue1Json)
                .post(BASE_PATH);

        // Create second issue
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, 
                    TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(issue2Json)
                .post(BASE_PATH);

        // Test filtering by project ID
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL, 
                    TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .queryParam("projectId", TicketingTestData.PROJECT_ID.toString())
                .get(BASE_PATH)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("issues", hasSize(2))
                .body("total", equalTo(2));
    }
    // Testfälle für das hinzufügen der N:M Relationen
    @Test
    void relation_blockedBy_blocks_bidirectional() {
        // Source (wird geblockt)
        String sourceJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Blocked Source\","
                + "\"type\":\"TASK\" }";

        String sourceId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(sourceJson)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        // Target (blockt)
        String targetJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Blocking Target\","
                + "\"type\":\"TASK\" }";

        String targetId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(targetJson)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        // Patch: Source wird von Target geblockt
        String patchJson = "{ \"blockedBy\":[\"" + targetId + "\"] }";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(patchJson)
                .patch(BASE_PATH + "/" + sourceId)
                .then()
                .statusCode(200)
                .body("blockedBy", hasSize(1))
                .body("blockedBy[0]", equalTo(targetId));

        // Target muss blocks = Source enthalten
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + targetId)
                .then()
                .statusCode(200)
                .body("blocks", hasSize(1))
                .body("blocks[0]", equalTo(sourceId));
    }

    @Test
    void relation_blocks_secondPatchDoesNotDuplicate() {
        // Source
        String sourceJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Source\","
                + "\"type\":\"TASK\" }";

        String sourceId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(sourceJson)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        // Target
        String targetJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Target\","
                + "\"type\":\"TASK\" }";

        String targetId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(targetJson)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        String patchJson = "{ \"blocks\":[\"" + targetId + "\"] }";

        // erster Patch
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(patchJson)
                .patch(BASE_PATH + "/" + sourceId)
                .then()
                .statusCode(200);

        // zweiter Patch mit gleicher Relation
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(patchJson)
                .patch(BASE_PATH + "/" + sourceId)
                .then()
                .statusCode(200);

        // prüfen, dass nichts dupliziert wurde
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + sourceId)
                .then()
                .statusCode(200)
                .body("blocks", hasSize(1));

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + targetId)
                .then()
                .statusCode(200)
                .body("blockedBy", hasSize(1));
    }

    @Test
    void relation_relatedTo_isSymmetric() {
        String aJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Issue A\","
                + "\"type\":\"TASK\" }";

        String bJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Issue B\","
                + "\"type\":\"TASK\" }";

        String aId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(aJson)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        String bId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(bJson)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        String patchJson = "{ \"relatedTo\":[\"" + bId + "\"] }";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(patchJson)
                .patch(BASE_PATH + "/" + aId)
                .then()
                .statusCode(200)
                .body("relatedTo", hasSize(1))
                .body("relatedTo[0]", equalTo(bId));

        // B muss auch A verlinken
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + bId)
                .then()
                .statusCode(200)
                .body("relatedTo", hasSize(1))
                .body("relatedTo[0]", equalTo(aId));
    }

    @Test
    void relation_duplicateOf_isSymmetric() {
        String aJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Original\","
                + "\"type\":\"TASK\" }";

        String bJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Duplicate\","
                + "\"type\":\"TASK\" }";

        String aId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(aJson)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        String bId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(bJson)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        String patchJson = "{ \"duplicateOf\":[\"" + bId + "\"] }";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(patchJson)
                .patch(BASE_PATH + "/" + aId)
                .then()
                .statusCode(200)
                .body("duplicateOf", hasSize(1))
                .body("duplicateOf[0]", equalTo(bId));

        // B muss A als duplicateOf haben
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + bId)
                .then()
                .statusCode(200)
                .body("duplicateOf", hasSize(1))
                .body("duplicateOf[0]", equalTo(aId));
    }

    @Test
    void deleteIssue_cleansUpAllRelationsOnOtherIssues() {
        // Main Issue
        String mainJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Main\","
                + "\"type\":\"TASK\" }";

        String mainId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(mainJson)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        // Andere Issues
        String blockerId = createSimpleIssue("Blocker");
        String blockedId = createSimpleIssue("Blocked");
        String relatedId = createSimpleIssue("Related");
        String duplicateId = createSimpleIssue("Duplicate");
        String parentId = createSimpleIssue("Parent");
        String childId = createSimpleIssue("Child");

        // Main blockt blockedId, wird geblockt von blockerId, ist related/duplicate, ist Parent/Child
        String patchJson = "{"
                + "\"blocks\":[\"" + blockedId + "\"],"
                + "\"blockedBy\":[\"" + blockerId + "\"],"
                + "\"relatedTo\":[\"" + relatedId + "\"],"
                + "\"duplicateOf\":[\"" + duplicateId + "\"],"
                + "\"parentOf\":[\"" + childId + "\"],"
                + "\"childOf\":[\"" + parentId + "\"]"
                + "}";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(patchJson)
                .patch(BASE_PATH + "/" + mainId)
                .then()
                .statusCode(200);

        // Jetzt Main löschen
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .delete(BASE_PATH + "/" + mainId)
                .then()
                .statusCode(204);

        // Prüfen, dass alle anderen Issues ihre Rückreferenzen NICHT mehr enthalten

        assertNoRelationsContain(mainId, blockerId);
        assertNoRelationsContain(mainId, blockedId);
        assertNoRelationsContain(mainId, relatedId);
        assertNoRelationsContain(mainId, duplicateId);
        assertNoRelationsContain(mainId, parentId);
        assertNoRelationsContain(mainId, childId);
    }

    // Helper zum Anlegen eines einfachen Issues
    private String createSimpleIssue(String title) {
        String json = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"" + title + "\","
                + "\"type\":\"TASK\" }";

        return given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(json)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    // Helper zum prüfen, dass ein bestimmtes Issue mainId in KEINEM Relationsfeld hat
    private void assertNoRelationsContain(String mainId, String issueId) {
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + issueId)
                .then()
                .statusCode(200)
                .body("blocks", not(hasItem(mainId)))
                .body("blockedBy", not(hasItem(mainId)))
                .body("relatedTo", not(hasItem(mainId)))
                .body("duplicateOf", not(hasItem(mainId)))
                .body("parentOf", not(hasItem(mainId)))
                .body("childOf", not(hasItem(mainId)));
    }

    @Test
    void deleteRelation_blocks_updatesBothSides() {
        String sourceId = createSimpleIssue("Source");
        String targetId = createSimpleIssue("Target");

        // blocks setzen
        String patchJson = "{ \"blocks\":[\"" + targetId + "\"] }";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(patchJson)
                .patch(BASE_PATH + "/" + sourceId)
                .then()
                .statusCode(200);

        // Relation löschen
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .delete(BASE_PATH + "/" + sourceId + "/relations/blocks/" + targetId)
                .then()
                .statusCode(204);

        // Prüfen: keine Relation mehr
        assertNoRelationsContain(targetId, sourceId);
        assertNoRelationsContain(sourceId, targetId);
    }

    @Test
    void deleteRelation_FAILED_unknownType() {
        String sourceId = createSimpleIssue("Source");
        String targetId = createSimpleIssue("Target");

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .delete(BASE_PATH + "/" + sourceId + "/relations/unknown_type/" + targetId)
                .then()
                .statusCode(400);
    }

    @Test
    void deleteRelation_onNonExistingRelation_isNoOp() {
        String sourceId = createSimpleIssue("Source");
        String targetId = createSimpleIssue("Target");

        // Kein Relation gesetzt, direkt deleteRelation aufrufen
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .delete(BASE_PATH + "/" + sourceId + "/relations/blocks/" + targetId)
                .then()
                .statusCode(anyOf(is(204), is(200))); // je nach Implementierung

        // Sicherstellen, dass weiterhin keine Relation existiert
        assertNoRelationsContain(sourceId, targetId);
        assertNoRelationsContain(targetId, sourceId);
    }


}