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
    // Testfälle für das Hinzufügen der N:M-Relationen
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
    void deleteRelation_onNonExistingRelation_isNoOp() {
        String sourceId = createSimpleIssue("Source");
        String targetId = createSimpleIssue("Target");

        // Keine Relation gesetzt, direkt deleteRelation aufrufen
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

    @Test
    void deleteRelation_FAILED_forbiddenWhenNoProjectRole() {
        // Issue als Manager anlegen, damit es ein valides Project hat
        String json = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Relation Test\","
                + "\"type\":\"TASK\" }";

        String issueId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(json)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        // Anderer User, der KEINE projectRole für dieses Projekt hat (z.B. Tenant)
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                        TicketingTestData.USER_FIRST_NAME_1,
                        Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
                .delete(BASE_PATH + "/" + issueId + "/relations/blocks/" + UUID.randomUUID())
                .then()
                .statusCode(403);
    }

    @Test
    void createIssue_SUCCESS_withBlocksRelation_bidirectionalOnCreate() {
        // Target-Issue zuerst anlegen
        String targetJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Target\","
                + "\"type\":\"TASK\" }";

        String targetId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(targetJson)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        // Source direkt mit blocks-Relation erstellen
        String sourceJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Source with Relation\","
                + "\"type\":\"TASK\","
                + "\"blocks\":[\"" + targetId + "\"] }";

        String sourceId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(sourceJson)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .body("blocks", hasSize(1))
                .body("blocks[0]", equalTo(targetId))
                .extract().path("id");

        // Target muss blockedBy = Source haben (Spiegelung)
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + targetId)
                .then()
                .statusCode(200)
                .body("blockedBy", hasSize(1))
                .body("blockedBy[0]", equalTo(sourceId));
    }

    @Test
    void relation_blocks_ignoresNullEntries() {
        String sourceJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Source\","
                + "\"type\":\"TASK\" }";

        String sourceId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(sourceJson)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        // blocks = [null] → darf nichts tun
        String patchJson = "{ \"blocks\":[null] }";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(patchJson)
                .patch(BASE_PATH + "/" + sourceId)
                .then()
                .statusCode(200);

        // prüfen: keine blocks gesetzt
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + sourceId)
                .then()
                .statusCode(200)
                .body("blocks", anyOf(nullValue(), hasSize(0)));
    }

    @Test
    void relation_blocks_ignoresSelfRelation() {
        String sourceJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Self\","
                + "\"type\":\"TASK\" }";

        String sourceId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(sourceJson)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        // blocks = [sourceId] → soll ignoriert werden
        String patchJson = "{ \"blocks\":[\"" + sourceId + "\"] }";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(patchJson)
                .patch(BASE_PATH + "/" + sourceId)
                .then()
                .statusCode(200);

        // keine Self-Relation entstanden
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + sourceId)
                .then()
                .statusCode(200)
                .body("blocks", anyOf(nullValue(), hasSize(0)));
    }

    @Test
    void deleteRelation_blockedBy_updatesBothSides() {
        String sourceId = createSimpleIssue("Blocked");
        String targetId = createSimpleIssue("Blocker");

        // blockedBy setzen → target.blocks wird gespiegelt
        String patchJson = "{ \"blockedBy\":[\"" + targetId + "\"] }";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(patchJson)
                .patch(BASE_PATH + "/" + sourceId)
                .then()
                .statusCode(200);

        // Relation löschen
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .delete(BASE_PATH + "/" + sourceId + "/relations/blocked_by/" + targetId)
                .then()
                .statusCode(204);

        // Prüfen: beide Seiten leer
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + sourceId)
                .then()
                .statusCode(200)
                .body("blockedBy", anyOf(nullValue(), hasSize(0)));

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + targetId)
                .then()
                .statusCode(200)
                .body("blocks", anyOf(nullValue(), hasSize(0)));
    }

    @Test
    void deleteRelation_relatedTo_updatesBothSides() {
        String aId = createSimpleIssue("A");
        String bId = createSimpleIssue("B");

        String patchJson = "{ \"relatedTo\":[\"" + bId + "\"] }";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(patchJson)
                .patch(BASE_PATH + "/" + aId)
                .then()
                .statusCode(200);

        // Löschen
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .delete(BASE_PATH + "/" + aId + "/relations/related_to/" + bId)
                .then()
                .statusCode(204);

        // A und B haben keine relatedTo mehr
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + aId)
                .then()
                .statusCode(200)
                .body("relatedTo", anyOf(nullValue(), hasSize(0)));

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + bId)
                .then()
                .statusCode(200)
                .body("relatedTo", anyOf(nullValue(), hasSize(0)));
    }

    @Test
    void deleteRelation_FAILED_unknownType_returnsBadRequest() {
        String sourceId = createSimpleIssue("Source");
        String targetId = createSimpleIssue("Target");

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .delete(BASE_PATH + "/" + sourceId + "/relations/unknown_type/" + targetId)
                .then()
                .statusCode(400);
    }

    @Test
    void deleteRelation_duplicateOf_updatesBothSides() {
        String originalId = createSimpleIssue("Original");
        String duplicateId = createSimpleIssue("Duplicate");

        // duplicateOf setzen: Original -> Duplicate
        String patchJson = "{ \"duplicateOf\":[\"" + duplicateId + "\"] }";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(patchJson)
                .patch(BASE_PATH + "/" + originalId)
                .then()
                .statusCode(200);

        // Relation löschen: duplicate_of
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .delete(BASE_PATH + "/" + originalId + "/relations/duplicate_of/" + duplicateId)
                .then()
                .statusCode(204);

        // Original darf keine duplicateOf mehr haben
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + originalId)
                .then()
                .statusCode(200)
                .body("duplicateOf", anyOf(nullValue(), hasSize(0)));

        // Duplicate darf keine duplicateOf mehr auf Original haben
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + duplicateId)
                .then()
                .statusCode(200)
                .body("duplicateOf", anyOf(nullValue(), hasSize(0)));
    }

    @Test
    void deleteRelation_parentOf_updatesBothSides() {
        String parentId = createSimpleIssue("Parent");
        String childId = createSimpleIssue("Child");

        // parentOf setzen: Parent -> Child
        String patchJson = "{ \"parentOf\":[\"" + childId + "\"] }";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(patchJson)
                .patch(BASE_PATH + "/" + parentId)
                .then()
                .statusCode(200);

        // Relation löschen: parent_of
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .delete(BASE_PATH + "/" + parentId + "/relations/parent_of/" + childId)
                .then()
                .statusCode(204);

        // Parent darf keine parentOf mehr haben
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + parentId)
                .then()
                .statusCode(200)
                .body("parentOf", anyOf(nullValue(), hasSize(0)));

        // Child darf keine childOf mehr haben
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + childId)
                .then()
                .statusCode(200)
                .body("childOf", anyOf(nullValue(), hasSize(0)));
    }

    @Test
    void deleteRelation_childOf_updatesBothSides() {
        String parentId = createSimpleIssue("Parent 2");
        String childId = createSimpleIssue("Child 2");

        // childOf setzen: Child -> Parent
        String patchJson = "{ \"childOf\":[\"" + parentId + "\"] }";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(patchJson)
                .patch(BASE_PATH + "/" + childId)
                .then()
                .statusCode(200);

        // Relation löschen: child_of
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .delete(BASE_PATH + "/" + childId + "/relations/child_of/" + parentId)
                .then()
                .statusCode(204);

        // Child darf keine childOf mehr haben
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + childId)
                .then()
                .statusCode(200)
                .body("childOf", anyOf(nullValue(), hasSize(0)));

        // Parent darf keine parentOf mehr haben
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME,
                        TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + parentId)
                .then()
                .statusCode(200)
                .body("parentOf", anyOf(nullValue(), hasSize(0)));
    }

    @Test
    void getIssues_FAILED_noTenancyMembership() {
        given()
                .when()
                .cookie(buildCookie(
                    TicketingTestData.USER_ID,
                    TicketingTestData.USER_EMAIL,
                    TicketingTestData.USER_FIRST_NAME,
                    Map.of(),
                    Map.of()))
                .get(BASE_PATH)
                .then()
                .statusCode(404);
    }

    @Test
    void getIssues_FAILED_invalidTenancyIdForbidden() {
        UUID unknownTenancy = UUID.randomUUID();

        given()
                .when()
                .cookie(buildCookie(
                    TicketingTestData.USER_ID,
                    TicketingTestData.USER_EMAIL,
                    TicketingTestData.USER_FIRST_NAME,
                    Map.of(),
                    TicketingTestData.TENANT_PROJECT_ROLES))
                .queryParam("tenancyId", unknownTenancy.toString())
                .get(BASE_PATH)
                .then()
                .statusCode(403);
    }

    @Test
    void getIssues_SUCCESS_tenancyIssuesForMember() {
        given()
                .when()
                .cookie(buildCookie(
                    TicketingTestData.USER_ID,
                    TicketingTestData.USER_EMAIL,
                    TicketingTestData.USER_FIRST_NAME,
                    Map.of(),
                    TicketingTestData.TENANT_PROJECT_ROLES))
                .queryParam("tenancyId", TicketingTestData.TENANCY_ID.toString())
                .get(BASE_PATH)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("issues", hasSize(0))
                .body("total", equalTo(0));
    }

}
