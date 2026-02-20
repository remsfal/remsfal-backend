package de.remsfal.ticketing.boundary;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.TicketingTestData;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class IssueResourceTest extends AbstractTicketingTest {

    static final String BASE_PATH = "/ticketing/v1/issues";

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
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
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
    void createProjectIssue_SUCCESS_issueIsCreated() {
        final String json = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"description\":\"" + TicketingTestData.ISSUE_DESCRIPTION + "\","
            + "\"type\":\"TASK\""
            + "}";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
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
        //TODO: FAILED
        final String json = "{ \"title\":\"" + TicketingTestData.ISSUE_TITLE_2 + "\","
            + "\"description\":\"" + TicketingTestData.ISSUE_DESCRIPTION_2 + "\","
            + "\"type\":\"DEFECT\""
            + "}";

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                TicketingTestData.USER_FIRST_NAME_1, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(json)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("title", equalTo(TicketingTestData.ISSUE_TITLE_2))
            .body("type", equalTo("DEFECT"))
            .body("status", equalTo("PENDING"))
            .body("projectId", nullValue())
            .body("description", notNullValue());
    }

    @Test
    void createProjectIssue_FAILED_noAuthentication() {
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
    void createProjectIssue_FAILED_noTitle() {
        final String json = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"type\":\"TASK\""
            + "}";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(json)
            .post(BASE_PATH)
            .then()
            .statusCode(400);
    }

    @Test
    void createProjectIssue_FAILED_noProjectPermission() {
        final String json = "{ \"projectId\":\"" + UUID.randomUUID() + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\""
            + "}";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(json)
            .post(BASE_PATH)
            .then()
            .statusCode(403);
    }

    @Test
    void getIssue_SUCCESS_issueIsReturned() {
        final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"description\":\"" + TicketingTestData.ISSUE_DESCRIPTION + "\","
            + "\"type\":\"TASK\""
            + "}";

        final Response createResponse = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
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
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
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
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
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
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
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
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
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
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
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
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + issueId)
            .then()
            .statusCode(204);

        // Verify issue is deleted
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
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
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issue1Json)
            .post(BASE_PATH);

        // Create second issue
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issue2Json)
            .post(BASE_PATH);

        // Test filtering by project ID
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
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
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
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
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(targetJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        // POST: Source wird von Target geblockt
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + sourceId + "/blocked-by/" + targetId)
            .then()
            .statusCode(200)
            .body("blockedBy", hasSize(1))
            .body("blockedBy[0]", equalTo(targetId));

        // Target muss blocks = Source enthalten
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
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
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
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
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(targetJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        // erster POST
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + sourceId + "/blocks/" + targetId)
            .then()
            .statusCode(200);

        // zweiter POST mit gleicher Relation
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + sourceId + "/blocks/" + targetId)
            .then()
            .statusCode(200);

        // prüfen, dass nichts dupliziert wurde
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + sourceId)
            .then()
            .statusCode(200)
            .body("blocks", hasSize(1));

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
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
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(aJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        String bId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(bJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + aId + "/related-to/" + bId)
            .then()
            .statusCode(200)
            .body("relatedTo", hasSize(1))
            .body("relatedTo[0]", equalTo(bId));

        // B muss auch A verlinken
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
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
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(aJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        String bId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(bJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + aId + "/duplicate-of/" + bId)
            .then()
            .statusCode(200)
            .body("duplicateOf", hasSize(1))
            .body("duplicateOf[0]", equalTo(bId));

        // B muss A als duplicateOf haben
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + bId)
            .then()
            .statusCode(200)
            .body("duplicateOf", hasSize(1))
            .body("duplicateOf[0]", equalTo(aId));
    }

    @Test
    void deleteIssue_cleansUpAllRelationsOnOtherIssues() {
        setupTestIssues();
        String mainId = TicketingTestData.ISSUE_ID_1.toString();
        String blockerId = TicketingTestData.ISSUE_ID_2.toString();
        String blockedId = TicketingTestData.ISSUE_ID_3.toString();
        String relatedId = TicketingTestData.ISSUE_ID_4.toString();
        String duplicateId = TicketingTestData.ISSUE_ID_5.toString();

        String parentId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body("{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Parent\","
                + "\"type\":\"TASK\" }")
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        String childId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body("{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"Child\","
                + "\"type\":\"TASK\" }")
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        // Main blockt blockedId
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + mainId + "/blocks/" + blockedId)
            .then()
            .statusCode(200);

        // Main wird geblockt von blockerId
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + mainId + "/blocked-by/" + blockerId)
            .then()
            .statusCode(200);

        // Main ist related zu relatedId
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + mainId + "/related-to/" + relatedId)
            .then()
            .statusCode(200);

        // Main ist duplicate of duplicateId
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + mainId + "/duplicate-of/" + duplicateId)
            .then()
            .statusCode(200);

        // Main hat childId als Child
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + mainId + "/children/" + childId)
            .then()
            .statusCode(200);

        // Main hat parentId als Parent
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .put(BASE_PATH + "/" + mainId + "/parent/" + parentId)
            .then()
            .statusCode(200);

        // Jetzt Main löschen
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
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

    // Helper zum prüfen, dass ein bestimmtes Issue mainId in KEINEM Relationsfeld hat
    private void assertNoRelationsContain(String mainId, String issueId) {
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueId)
            .then()
            .statusCode(200)
            .body("blocks", not(hasItem(mainId)))
            .body("blockedBy", not(hasItem(mainId)))
            .body("relatedTo", not(hasItem(mainId)))
            .body("duplicateOf", not(hasItem(mainId)))
            .body("parentIssue", not(equalTo(mainId)))
            .body("childrenIssues", not(hasItem(mainId)));
    }

    @Test
    void deleteRelation_blocks_updatesBothSides() {
        setupTestIssues();
        String sourceId = TicketingTestData.ISSUE_ID_1.toString();
        String targetId = TicketingTestData.ISSUE_ID_2.toString();

        // blocks setzen
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + sourceId + "/blocks/" + targetId)
            .then()
            .statusCode(200);

        // Relation löschen
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + sourceId + "/blocks/" + targetId)
            .then()
            .statusCode(204);

        // Prüfen: keine Relation mehr
        assertNoRelationsContain(targetId, sourceId);
        assertNoRelationsContain(sourceId, targetId);
    }

    @Test
    void deleteRelation_onNonExistingRelation_isNoOp() {
        setupTestIssues();
        String sourceId = TicketingTestData.ISSUE_ID_1.toString();
        String targetId = TicketingTestData.ISSUE_ID_2.toString();

        // Keine Relation gesetzt, direkt deleteRelation aufrufen
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + sourceId + "/blocks/" + targetId)
            .then()
            .statusCode(400); // BadRequestException: "Issue does not block the specified issue"

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
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
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
                Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + issueId + "/blocks/" + UUID.randomUUID())
            .then()
            .statusCode(403);
    }

    @Test
    void createRelation_SUCCESS_withBlocksRelation_bidirectionalOnCreate() {
        // Target-Issue zuerst anlegen
        String targetJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"Target\","
            + "\"type\":\"TASK\" }";

        String targetId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(targetJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        // Source erstellen
        String sourceJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"Source with Relation\","
            + "\"type\":\"TASK\" }";

        String sourceId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(sourceJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        // Relation hinzufügen
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + sourceId + "/blocks/" + targetId)
            .then()
            .statusCode(200)
            .body("blocks", hasSize(1))
            .body("blocks[0]", equalTo(targetId));

        // Target muss blockedBy = Source haben (Spiegelung)
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + targetId)
            .then()
            .statusCode(200)
            .body("blockedBy", hasSize(1))
            .body("blockedBy[0]", equalTo(sourceId));
    }

    @Test
    void relation_blocks_rejectsNonExistingIssue() {
        String sourceJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"Source\","
            + "\"type\":\"TASK\" }";

        String sourceId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(sourceJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        // Versuche Relation zu nicht-existierendem Issue zu setzen
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + sourceId + "/blocks/" + UUID.randomUUID())
            .then()
            .statusCode(404); // Blocked issue not found

        // prüfen: keine blocks gesetzt
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + sourceId)
            .then()
            .statusCode(200)
            .body("blocks", anyOf(nullValue(), hasSize(0)));
    }

    @Test
    void relation_blocks_rejectsSelfRelation() {
        String sourceJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"Self\","
            + "\"type\":\"TASK\" }";

        String sourceId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(sourceJson)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .extract().path("id");

        // blocks = [sourceId] → wird abgelehnt
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + sourceId + "/blocks/" + sourceId)
            .then()
            .statusCode(400); // An issue cannot block itself

        // keine Self-Relation entstanden
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + sourceId)
            .then()
            .statusCode(200)
            .body("blocks", anyOf(nullValue(), hasSize(0)));
    }

    @Test
    void deleteRelation_blockedBy_updatesBothSides() {
        setupTestIssues();
        String sourceId = TicketingTestData.ISSUE_ID_1.toString();
        String targetId = TicketingTestData.ISSUE_ID_2.toString();

        // blockedBy setzen → target.blocks wird gespiegelt
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + sourceId + "/blocked-by/" + targetId)
            .then()
            .statusCode(200);

        // Relation löschen
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + sourceId + "/blocked-by/" + targetId)
            .then()
            .statusCode(204);

        // Prüfen: beide Seiten leer
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + sourceId)
            .then()
            .statusCode(200)
            .body("blockedBy", anyOf(nullValue(), hasSize(0)));

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + targetId)
            .then()
            .statusCode(200)
            .body("blocks", anyOf(nullValue(), hasSize(0)));
    }

    @Test
    void deleteRelation_relatedTo_updatesBothSides() {
        setupTestIssues();
        String aId = TicketingTestData.ISSUE_ID_1.toString();
        String bId = TicketingTestData.ISSUE_ID_2.toString();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + aId + "/related-to/" + bId)
            .then()
            .statusCode(200);

        // Löschen
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + aId + "/related-to/" + bId)
            .then()
            .statusCode(204);

        // A und B haben keine relatedTo mehr
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + aId)
            .then()
            .statusCode(200)
            .body("relatedTo", anyOf(nullValue(), hasSize(0)));

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + bId)
            .then()
            .statusCode(200)
            .body("relatedTo", anyOf(nullValue(), hasSize(0)));
    }

    @Test
    void deleteRelation_FAILED_unknownType_returnsBadRequest() {
        setupTestIssues();
        String sourceId = TicketingTestData.ISSUE_ID_1.toString();
        String targetId = TicketingTestData.ISSUE_ID_2.toString();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + sourceId + "/unknown-type/" + targetId)
            .then()
            .statusCode(404); // Path pattern doesn't match, so 404
    }

    @Test
    void deleteRelation_duplicateOf_updatesBothSides() {
        setupTestIssues();
        String originalId = TicketingTestData.ISSUE_ID_1.toString();
        String duplicateId = TicketingTestData.ISSUE_ID_2.toString();

        // duplicateOf setzen: Original -> Duplicate
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + originalId + "/duplicate-of/" + duplicateId)
            .then()
            .statusCode(200);

        // Relation löschen: duplicate-of
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + originalId + "/duplicate-of/" + duplicateId)
            .then()
            .statusCode(204);

        // Original darf keine duplicateOf mehr haben
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + originalId)
            .then()
            .statusCode(200)
            .body("duplicateOf", anyOf(nullValue(), hasSize(0)));

        // Duplicate darf keine duplicateOf mehr auf Original haben
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + duplicateId)
            .then()
            .statusCode(200)
            .body("duplicateOf", anyOf(nullValue(), hasSize(0)));
    }

    @Test
    void deleteRelation_children_updatesBothSides() {
        setupTestIssues();
        String parentId = TicketingTestData.ISSUE_ID_1.toString();
        String childId = TicketingTestData.ISSUE_ID_2.toString();

        // childrenIssues setzen: Parent has Child
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + parentId + "/children/" + childId)
            .then()
            .statusCode(200);

        // Relation löschen: children
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + parentId + "/children/" + childId)
            .then()
            .statusCode(204);

        // Parent darf keine childrenIssues mehr haben
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + parentId)
            .then()
            .statusCode(200)
            .body("childrenIssues", anyOf(nullValue(), hasSize(0)));

        // Child darf keine parentIssue mehr haben
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + childId)
            .then()
            .statusCode(200)
            .body("parentIssue", nullValue());
    }

    @Test
    void deleteRelation_parent_updatesBothSides() {
        setupTestIssues();
        String parentId = TicketingTestData.ISSUE_ID_1.toString();
        String childId = TicketingTestData.ISSUE_ID_2.toString();

        // parentIssue setzen: Child -> Parent
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .put(BASE_PATH + "/" + childId + "/parent/" + parentId)
            .then()
            .statusCode(200);

        // Relation löschen: parent
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + childId + "/parent/" + parentId)
            .then()
            .statusCode(204);

        // Child darf keine parentIssue mehr haben
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + childId)
            .then()
            .statusCode(200)
            .body("parentIssue", nullValue());

        // Parent darf keine childrenIssues mehr haben
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + parentId)
            .then()
            .statusCode(200)
            .body("childrenIssues", anyOf(nullValue(), hasSize(0)));
    }

    @Test
    void getIssues_FAILED_tenantWithUnauthorizedTenancyId() {
        UUID unauthorizedAgreementId = UUID.randomUUID();

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                TicketingTestData.USER_FIRST_NAME_1, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .queryParam("agreementId", unauthorizedAgreementId.toString())
            .get(BASE_PATH)
            .then()
            .statusCode(403);
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
    void getIssues_SUCCESS_contractorAsParticipantWithDeletedIssue() {
        final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"Contractor Issue\","
            + "\"type\":\"TASK\""
            + "}";

        Response createResponse = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(createJson)
            .post(BASE_PATH)
            .thenReturn();

        String issueId = createResponse.then()
            .contentType(MediaType.APPLICATION_JSON)
            .extract().path("id");

        UUID contractorId = UUID.randomUUID();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + issueId)
            .then()
            .statusCode(204);

        given()
            .when()
            .cookie(buildCookie(contractorId, "contractor@test.com",
                "Contractor", Map.of(), Map.of(), Map.of()))
            .get(BASE_PATH)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    void getIssues_SUCCESS_tenantFiltersByStatus() {
        final String createJson1 = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"Open Issue\","
            + "\"type\":\"TASK\""
            + "}";

        Response response1 = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(createJson1)
            .post(BASE_PATH)
            .thenReturn();

        String issueId1 = response1.then()
            .contentType(MediaType.APPLICATION_JSON)
            .extract().path("id");

        final String updateJson = "{ \"status\":\"IN_PROGRESS\" }";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(updateJson)
            .patch(BASE_PATH + "/" + issueId1);

        final String createJson2 = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"Pending Issue\","
            + "\"type\":\"DEFECT\""
            + "}";

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                TicketingTestData.USER_FIRST_NAME_1, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(createJson2)
            .post(BASE_PATH);

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                TicketingTestData.USER_FIRST_NAME_1, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .queryParam("status", "PENDING")
            .get(BASE_PATH)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
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
    void getIssues_SUCCESS_contractorWithNoParticipations() {
        UUID contractorId = UUID.randomUUID();

        given()
            .when()
            .cookie(buildCookie(contractorId, "contractor@test.com",
                "Contractor", Map.of(), Map.of(), Map.of()));

    }

    void getIssues_FAILED_noAgreementMembership() {
        given()
            .when()
            .cookie(buildCookie(
                TicketingTestData.USER_ID,
                TicketingTestData.USER_EMAIL,
                TicketingTestData.USER_FIRST_NAME,
                Map.of(), Map.of(),
                Map.of()))
            .get(BASE_PATH)
            .then()
            .statusCode(404);
    }

    @Test
    void getIssues_FAILED_invalidAgreementIdForbidden() {
        UUID unknownAgreement = UUID.randomUUID();

        given()
            .when()
            .cookie(buildCookie(
                TicketingTestData.USER_ID,
                TicketingTestData.USER_EMAIL,
                TicketingTestData.USER_FIRST_NAME,
                Map.of(), Map.of(),
                TicketingTestData.TENANT_PROJECT_ROLES))
            .queryParam("agreementId", unknownAgreement.toString())
            .get(BASE_PATH)
            .then()
            .statusCode(403);
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
            .body("total", equalTo(0));
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
    void getIssue_FAILED_noPermission() {

        final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\""
            + "}";

        final Response createResponse = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(createJson)
            .post(BASE_PATH)
            .thenReturn();

        final String issueId = createResponse.then()
            .contentType(MediaType.APPLICATION_JSON)
            .extract().path("id");

        UUID unauthorizedUserId = UUID.randomUUID();

        given()
            .when()
            .cookie(buildCookie(unauthorizedUserId, "unauthorized@test.com",
                "Unauthorized", Map.of(), Map.of(), Map.of()))
            .get(BASE_PATH + "/" + issueId)
            .then()
            .statusCode(403);
    }

    @Test
    void deleteIssue_FAILED_noPermissionToDelete() {
        final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
            + "\"type\":\"TASK\""
            + "}";

        final Response createResponse = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(createJson)
            .post(BASE_PATH)
            .thenReturn();

        final String issueId = createResponse.then()
            .contentType(MediaType.APPLICATION_JSON)
            .extract().path("id");

        UUID unauthorizedUserId = UUID.randomUUID();

        given()
            .when()
            .cookie(buildCookie(unauthorizedUserId, "unauthorized@test.com",
                "Unauthorized", Map.of(), Map.of(), Map.of()))
            .delete(BASE_PATH + "/" + issueId)
            .then()
            .statusCode(403);
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

    @Test
    void createTenancyIssueWithAttachments_SUCCESS_issueWithAttachmentsIsCreated() {
        final String issueJson = "{ \"agreementId\":\"" + TicketingTestData.AGREEMENT_ID + "\","
            + "\"title\":\"Issue with attachments\","
            + "\"type\":\"DEFECT\""
            + "}";
        InputStream fileStream = getTestFileStream(TicketingTestData.FILE_PNG_PATH);

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                TicketingTestData.USER_NAME, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .multiPart("issue", issueJson, MediaType.APPLICATION_JSON)
            .multiPart("attachment", TicketingTestData.FILE_PNG_PATH, fileStream, TicketingTestData.FILE_PNG_TYPE)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("title", equalTo("Issue with attachments"))
            .body("type", equalTo("DEFECT"))
            .body("status", equalTo("PENDING"))
            .body("projectId", nullValue())
            .body("attachments", hasSize(1))
            .body("attachments[0].issueId", notNullValue())
            .body("attachments[0].attachmentId", notNullValue())
            .body("attachments[0].fileName", equalTo(TicketingTestData.FILE_PNG_PATH))
            .body("attachments[0].contentType", startsWith(TicketingTestData.FILE_PNG_TYPE))
            .body("attachments[0].objectName", startsWith("/issues/"))
            .body("attachments[0].objectName", endsWith(TicketingTestData.FILE_PNG_PATH))
            .body("attachments[0].uploadedBy", equalTo(TicketingTestData.USER_ID.toString()))
            .body("attachments[0].createdAt", notNullValue());
    }

    @Test
    void createTenancyIssueWithAttachments_SUCCESS_issueWithoutAttachmentsIsCreated() {
        final String issueJson = "{ \"agreementId\":\"" + TicketingTestData.AGREEMENT_ID + "\","
            + "\"title\":\"Issue without attachments\","
            + "\"type\":\"TERMINATION\""
            + "}";

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                TicketingTestData.USER_NAME, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .multiPart("issue", issueJson, MediaType.APPLICATION_JSON)
            .post(BASE_PATH)
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("title", equalTo("Issue without attachments"))
            .body("type", equalTo("TERMINATION"))
            .body("status", equalTo("PENDING"))
            .body("isVisibleForTenant", nullValue())
            .body("projectId", nullValue())
            .body("attachments", nullValue());
    }

    @Test
    void createTenancyIssueWithAttachments_FAILED_noAuthentication() {
        given()
            .when()
            .multiPart("projectId", TicketingTestData.PROJECT_ID.toString())
            .multiPart("title", "Issue with attachments")
            .multiPart("type", "TASK")
            .post(BASE_PATH)
            .then()
            .statusCode(401);
    }

    @Test
    void createTenancyIssueWithAttachments_FAILED_missingRequiredFields() {
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .multiPart("projectId", TicketingTestData.PROJECT_ID.toString())
            .post(BASE_PATH)
            .then()
            .statusCode(400);
    }

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
        UUID unauthorizedUserId = UUID.randomUUID();

        given()
            .when()
            .cookie(buildCookie(unauthorizedUserId, "unauthorized@test.com",
                "Unauthorized", Map.of(), Map.of(), Map.of()))
            .get(BASE_PATH + "/" + TicketingTestData.ISSUE_ID_2 + "/attachments/"
                + TicketingTestData.ATTACHMENT_ID_1 + "/" + TicketingTestData.ATTACHMENT_FILE_PATH_1)
            .then()
            .statusCode(403);
    }

    @Test
    void downloadAttachment_FAILED_issueNotFound() {
        UUID nonExistentIssueId = UUID.randomUUID();
        UUID attachmentId = UUID.randomUUID();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + nonExistentIssueId + "/attachments/"
                + attachmentId + "/test.png")
            .then()
            .statusCode(404);
    }

    @Test
    void downloadAttachment_FAILED_attachmentNotFound() {
        setupTestIssues();
        UUID nonExistentAttachmentId = UUID.randomUUID();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + TicketingTestData.ISSUE_ID_2 + "/attachments/"
                + nonExistentAttachmentId + "/test.png")
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

        // Verify attachment is deleted - should return 404 when trying to download
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
        UUID unauthorizedUserId = UUID.randomUUID();

        given()
            .when()
            .cookie(buildCookie(unauthorizedUserId, "unauthorized@test.com",
                "Unauthorized", Map.of(), Map.of(), Map.of()))
            .delete(BASE_PATH + "/" + TicketingTestData.ISSUE_ID_2 + "/attachments/"
                + TicketingTestData.ATTACHMENT_ID_1)
            .then()
            .statusCode(403);
    }

    @Test
    void deleteAttachment_FAILED_issueNotFound() {
        UUID nonExistentIssueId = UUID.randomUUID();
        UUID attachmentId = UUID.randomUUID();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + nonExistentIssueId + "/attachments/" + attachmentId)
            .then()
            .statusCode(404);
    }

    @Test
    void deleteAttachment_FAILED_attachmentNotFound() {
        setupTestIssues();
        UUID nonExistentAttachmentId = UUID.randomUUID();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + TicketingTestData.ISSUE_ID_1 + "/attachments/"
                + nonExistentAttachmentId)
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
