package de.remsfal.ticketing.boundary;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

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
    // Testfälle für das hinzufügen der N:M Relationen

    //Testen jedes Relationstypen
    @Test
    void relation_blocks_blockedBy_bidirectional() {
        // Source-Issue
        String sourceJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Source Blocks\","
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

        // Target-Issue
        String targetJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Target Blocked\","
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

        // Patch: Source blockt Target
        String patchJson = "{ \"blocks\":[\"" + targetId + "\"] }";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(patchJson)
                .patch(BASE_PATH + "/" + sourceId)
                .then()
                .statusCode(200)
                .body("blocks", hasSize(1))
                .body("blocks[0]", equalTo(targetId));

        // Target muss blockedBy = Source enthalten
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + targetId)
                .then()
                .statusCode(200)
                .body("blockedBy", hasSize(1))
                .body("blockedBy[0]", equalTo(sourceId));
    }

    @Test
    void relation_relatedTo_bidirectional() {
        String issue1Json = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Issue 1 related\","
                + "\"type\":\"TASK\" }";

        String issue1Id = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(issue1Json)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        String issue2Json = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Issue 2 related\","
                + "\"type\":\"TASK\" }";

        String issue2Id = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(issue2Json)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        // Issue 1 wird relatedTo Issue 2 gepatcht
        String patchJson = "{ \"relatedTo\":[\"" + issue2Id + "\"] }";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(patchJson)
                .patch(BASE_PATH + "/" + issue1Id)
                .then()
                .statusCode(200)
                .body("relatedTo", hasSize(1))
                .body("relatedTo[0]", equalTo(issue2Id));

        // Issue 2 muss ebenfalls relatedTo Issue 1 haben
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + issue2Id)
                .then()
                .statusCode(200)
                .body("relatedTo", hasSize(1))
                .body("relatedTo[0]", equalTo(issue1Id));
    }


    @Test
    void relation_duplicateOf_bidirectional() {
        String issue1Json = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Original Issue\","
                + "\"type\":\"TASK\" }";

        String originalId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(issue1Json)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        String issue2Json = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Duplicate Issue\","
                + "\"type\":\"TASK\" }";

        String duplicateId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(issue2Json)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        // Duplicate zeigt auf Original
        String patchJson = "{ \"duplicateOf\":[\"" + originalId + "\"] }";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(patchJson)
                .patch(BASE_PATH + "/" + duplicateId)
                .then()
                .statusCode(200)
                .body("duplicateOf", hasSize(1))
                .body("duplicateOf[0]", equalTo(originalId));

        // Original muss ebenfalls duplicateOf = Duplicate enthalten
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + originalId)
                .then()
                .statusCode(200)
                .body("duplicateOf", hasSize(1))
                .body("duplicateOf[0]", equalTo(duplicateId));
    }


    @Test
    void relation_parentOf_childOf_bidirectional() {
        String parentJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Parent Issue\","
                + "\"type\":\"TASK\" }";

        String parentId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(parentJson)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        String childJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Child Issue\","
                + "\"type\":\"TASK\" }";

        String childId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(childJson)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        // Parent zeigt auf Child
        String patchJson = "{ \"parentOf\":[\"" + childId + "\"] }";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(patchJson)
                .patch(BASE_PATH + "/" + parentId)
                .then()
                .statusCode(200)
                .body("parentOf", hasSize(1))
                .body("parentOf[0]", equalTo(childId));

        // Child muss childOf = Parent enthalten
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + childId)
                .then()
                .statusCode(200)
                .body("childOf", hasSize(1))
                .body("childOf[0]", equalTo(parentId));
    }

    // Testen der Funktionalität gleich beim erstellen eines Issues die Relationen mit zu erstellen
    @Test
    void createIssueWithRelations_SUCCESS_bidirectionalRelationsCreated() {
        // 1) Zwei Basis-Issues erstellen, die referenziert werden sollen
        final String baseJson1 = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Base Issue 1\","
                + "\"type\":\"TASK\""
                + "}";

        final Response baseResponse1 = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(baseJson1)
                .post(BASE_PATH)
                .thenReturn();

        final String baseIssueId1 = baseResponse1.then()
                .statusCode(201)
                .extract().path("id");

        final String baseJson2 = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Base Issue 2\","
                + "\"type\":\"TASK\""
                + "}";

        final Response baseResponse2 = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(baseJson2)
                .post(BASE_PATH)
                .thenReturn();

        final String baseIssueId2 = baseResponse2.then()
                .statusCode(201)
                .extract().path("id");

        // 2) Issue erstellen, das baseIssueId1 blockt und mit baseIssueId2 related ist
        final String issueWithRelationsJson = "{"
                + "\"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Issue with relations\","
                + "\"type\":\"TASK\","
                + "\"blocks\":[\"" + baseIssueId1 + "\"],"
                + "\"relatedTo\":[\"" + baseIssueId2 + "\"]"
                + "}";

        final Response createdWithRelationsResponse = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(issueWithRelationsJson)
                .post(BASE_PATH)
                .thenReturn();

        final String issueWithRelationsId = createdWithRelationsResponse.then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("blocks[0]", equalTo(baseIssueId1))
                .body("relatedTo[0]", equalTo(baseIssueId2))
                .extract().path("id");

        // 3) Prüfen: Base Issue 1 wurde in blockedBy aktualisiert
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + baseIssueId1)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("blockedBy", hasSize(1))
                .body("blockedBy[0]", equalTo(issueWithRelationsId));

        // 4) Prüfen: Base Issue 2 wurde in relatedTo aktualisiert
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + baseIssueId2)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("relatedTo", hasSize(1))
                .body("relatedTo[0]", equalTo(issueWithRelationsId));
    }

    //Testen der Löschen funktionalitäten
    //Löschen einer einzelnen Relation
    @Test
    void deleteRelation_SUCCESS_blocksRelationRemovedBidirectional() {
        // 1) Zwei Issues erstellen
        final String json1 = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Source Issue for deleteRelation\","
                + "\"type\":\"TASK\""
                + "}";

        final String json2 = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Target Issue for deleteRelation\","
                + "\"type\":\"TASK\""
                + "}";

        final String sourceId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(json1)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        final String targetId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(json2)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        // 2) blocks-Relation via PATCH hinzufügen
        final String patchJson = "{ \"blocks\":[\"" + targetId + "\"] }";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(patchJson)
                .patch(BASE_PATH + "/" + sourceId)
                .then()
                .statusCode(200);

        // Sicherstellen, dass die Relation vorhanden ist
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + sourceId)
                .then()
                .statusCode(200)
                .body("blocks", hasSize(1))
                .body("blocks[0]", equalTo(targetId));

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + targetId)
                .then()
                .statusCode(200)
                .body("blockedBy", hasSize(1))
                .body("blockedBy[0]", equalTo(sourceId));

        // 3) Relation löschen
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .delete(BASE_PATH + "/" + sourceId + "/relations/blocks/" + targetId)
                .then()
                .statusCode(204);

        // 4) Prüfen, dass beide Seiten kein Relationseintrag mehr haben
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + sourceId)
                .then()
                .statusCode(200)
                .body("blocks", hasSize(0));

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + targetId)
                .then()
                .statusCode(200)
                .body("blockedBy", hasSize(0));
    }
    //Löschen eines Issues mit seinen Relationseinträgen bei anderen Issues
    @Test
    void deleteIssue_SUCCESS_relationsAreCleanedUpInOtherIssues() {
        // 1) A, B, C erstellen
        String issueAJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Issue A\","
                + "\"type\":\"TASK\""
                + "}";

        String issueBJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Issue B\","
                + "\"type\":\"TASK\""
                + "}";

        String issueCJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Issue C\","
                + "\"type\":\"TASK\""
                + "}";

        String issueAId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(issueAJson)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        String issueBId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(issueBJson)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        String issueCId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(issueCJson)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        // 2) A blocks B
        String patchABlocksB = "{ \"blocks\":[\"" + issueBId + "\"] }";
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(patchABlocksB)
                .patch(BASE_PATH + "/" + issueAId)
                .then()
                .statusCode(200);

        // 3) B relatedTo C
        String patchBRelatedC = "{ \"relatedTo\":[\"" + issueCId + "\"] }";
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(patchBRelatedC)
                .patch(BASE_PATH + "/" + issueBId)
                .then()
                .statusCode(200);

        // Sicherheitscheck: A.blocks enthält B, B.blockedBy enthält A
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + issueAId)
                .then()
                .statusCode(200)
                .body("blocks[0]", equalTo(issueBId));

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + issueBId)
                .then()
                .statusCode(200)
                .body("blockedBy[0]", equalTo(issueAId))
                .body("relatedTo[0]", equalTo(issueCId));

        // 4) B löschen
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .delete(BASE_PATH + "/" + issueBId)
                .then()
                .statusCode(204);

        // 5) B darf nicht mehr existieren
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + issueBId)
                .then()
                .statusCode(404);

        // 6) A.blocks hat B nicht mehr
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + issueAId)
                .then()
                .statusCode(200)
                .body("blocks", hasSize(0));

        // 7) C.relatedTo hat B nicht mehr
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + issueCId)
                .then()
                .statusCode(200)
                .body("relatedTo", hasSize(0));
    }

    @Test
    void deleteIssue_SUCCESS_multipleRelationTypesCleanedUp() {
        // X
        String xJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Issue X\","
                + "\"type\":\"TASK\" }";

        String xId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(xJson)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        // Y
        String yJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Issue Y\","
                + "\"type\":\"TASK\" }";

        String yId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(yJson)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        // Z
        String zJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Issue Z\","
                + "\"type\":\"TASK\" }";

        String zId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(zJson)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        // D
        String dJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Issue D\","
                + "\"type\":\"TASK\" }";

        String dId = given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(dJson)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .extract().path("id");

        // D.blocks = X, D.relatedTo = Y, D.parentOf = Z
        String patchDRelations = "{"
                + "\"blocks\":[\"" + xId + "\"],"
                + "\"relatedTo\":[\"" + yId + "\"],"
                + "\"parentOf\":[\"" + zId + "\"]"
                + "}";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(patchDRelations)
                .patch(BASE_PATH + "/" + dId)
                .then()
                .statusCode(200)
                .body("blocks", hasSize(1))
                .body("relatedTo", hasSize(1))
                .body("parentOf", hasSize(1));

        // Sicherheitscheck: X/Y/Z haben die Gegenseite
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + xId)
                .then()
                .statusCode(200)
                .body("blockedBy", hasSize(1))
                .body("blockedBy[0]", equalTo(dId));

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + yId)
                .then()
                .statusCode(200)
                .body("relatedTo", hasSize(1))
                .body("relatedTo[0]", equalTo(dId));

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + zId)
                .then()
                .statusCode(200)
                .body("childOf", hasSize(1))
                .body("childOf[0]", equalTo(dId));

        // Jetzt D löschen
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .delete(BASE_PATH + "/" + dId)
                .then()
                .statusCode(204);

        // D existiert nicht mehr
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + dId)
                .then()
                .statusCode(404);

        // X hat kein blockedBy mehr
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + xId)
                .then()
                .statusCode(200)
                .body("blockedBy", hasSize(0));

        // Y hat kein relatedTo mehr
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + yId)
                .then()
                .statusCode(200)
                .body("relatedTo", hasSize(0));

        // Z hat kein childOf mehr
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .get(BASE_PATH + "/" + zId)
                .then()
                .statusCode(200)
                .body("childOf", hasSize(0));
    }

    @Test
    void deleteRelation_FAILED_issueNotFound() {
        UUID randomIssueId = UUID.randomUUID();
        UUID randomRelatedId = UUID.randomUUID();

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .delete(BASE_PATH + "/" + randomIssueId + "/relations/blocks/" + randomRelatedId)
                .then()
                .statusCode(404);
    }

    @Test
    void deleteRelation_FAILED_forbiddenForUserWithoutProjectRole() {
        // Erst zwei Issues als Manager anlegen + Relation herstellen
        String sourceJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Source for forbidden delete\","
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

        String targetJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Target for forbidden delete\","
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

        // Relation hinzufügen (blocks)
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

        // Tenant (ohne Projektrolle) versucht Relation zu löschen -> 403
        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                        TicketingTestData.USER_FIRST_NAME_1, Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
                .delete(BASE_PATH + "/" + sourceId + "/relations/blocks/" + targetId)
                .then()
                .statusCode(403);
    }

}