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

import de.remsfal.ticketing.entity.dao.IssueParticipantRepository;
import de.remsfal.ticketing.entity.dto.IssueParticipantEntity;
import de.remsfal.ticketing.entity.dto.IssueParticipantKey;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.json.ticketing.IssueAttachmentJson;
import de.remsfal.core.json.ticketing.IssueJson;
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

    @Inject
    IssueParticipantRepository issueParticipantRepository;

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
    void createIssue_SUCCESS_issueIsCreated() {
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
        final String json = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE_2 + "\","
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
            .body("projectId", equalTo(TicketingTestData.PROJECT_ID.toString()))
            .body("description", nullValue());
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
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
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
        // Main Issue
        String mainJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"Main\","
            + "\"type\":\"TASK\" }";

        String mainId = given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
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

    // Helper zum Anlegen eines einfachen Issues
    private String createSimpleIssue(String title) {
        String json = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + title + "\","
            + "\"type\":\"TASK\" }";

        return given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
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
        String sourceId = createSimpleIssue("Source");
        String targetId = createSimpleIssue("Target");

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
        String sourceId = createSimpleIssue("Source");
        String targetId = createSimpleIssue("Target");

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
    void createIssue_SUCCESS_withBlocksRelation_bidirectionalOnCreate() {
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
        String sourceId = createSimpleIssue("Blocked");
        String targetId = createSimpleIssue("Blocker");

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
        String aId = createSimpleIssue("A");
        String bId = createSimpleIssue("B");

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
        String sourceId = createSimpleIssue("Source");
        String targetId = createSimpleIssue("Target");

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + sourceId + "/unknown-type/" + targetId)
            .then()
            .statusCode(404); // Path pattern doesn't match, so 404
    }

    @Test
    void deleteRelation_duplicateOf_updatesBothSides() {
        String originalId = createSimpleIssue("Original");
        String duplicateId = createSimpleIssue("Duplicate");

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
        String parentId = createSimpleIssue("Parent");
        String childId = createSimpleIssue("Child");

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
        String parentId = createSimpleIssue("Parent 2");
        String childId = createSimpleIssue("Child 2");

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
    void getIssue_SUCCESS_participantViewsFilteredIssue() {

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

        UUID participantId = UUID.randomUUID();

        IssueParticipantKey key = new IssueParticipantKey();
        key.setUserId(participantId);
        key.setIssueId(UUID.fromString(issueId));
        key.setSessionId(UUID.randomUUID());

        IssueParticipantEntity participantEntity = new IssueParticipantEntity();
        participantEntity.setKey(key);
        participantEntity.setProjectId(TicketingTestData.PROJECT_ID);
        participantEntity.setRole("CONTRACTOR");

        issueParticipantRepository.insert(participantEntity);

        given()
            .when()
            .cookie(buildCookie(participantId, "participant@test.com",
                "Participant", Map.of(), Map.of(), Map.of()))
            .get(BASE_PATH + "/" + issueId)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(issueId))
            .body("title", equalTo(TicketingTestData.ISSUE_TITLE))
            .body("description", nullValue());
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
    void getIssues_SUCCESS_participantIssueAddedToCollection() {
        final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"Participant Issue\","
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

        UUID participantId = UUID.randomUUID();

        IssueParticipantKey key = new IssueParticipantKey();
        key.setUserId(participantId);
        key.setIssueId(UUID.fromString(issueId));
        key.setSessionId(UUID.randomUUID());

        IssueParticipantEntity participantEntity = new IssueParticipantEntity();
        participantEntity.setKey(key);
        participantEntity.setProjectId(TicketingTestData.PROJECT_ID);
        participantEntity.setRole("CONTRACTOR");

        issueParticipantRepository.insert(participantEntity);

        given()
            .when()
            .cookie(buildCookie(participantId, "participant@test.com",
                "Participant", Map.of(), Map.of(), Map.of()))
            .get(BASE_PATH)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("issues", hasSize(1));
    }

    @Test
    void getIssues_SUCCESS_deduplicationRemovesDuplicates() {
        final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"Duplicate Test Issue\","
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

        IssueParticipantKey key = new IssueParticipantKey();
        key.setUserId(TicketingTestData.USER_ID_1);
        key.setIssueId(UUID.fromString(issueId));
        key.setSessionId(UUID.randomUUID());

        IssueParticipantEntity participantEntity = new IssueParticipantEntity();
        participantEntity.setKey(key);
        participantEntity.setProjectId(TicketingTestData.PROJECT_ID);
        participantEntity.setRole("CONTRACTOR");

        issueParticipantRepository.insert(participantEntity);

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
    void createIssueWithAttachments_SUCCESS_issueWithAttachmentsIsCreated() {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"agreementId\":\"" + TicketingTestData.AGREEMENT_ID + "\","
            + "\"title\":\"Issue with attachments\","
            + "\"type\":\"DEFECT\""
            + "}";
        InputStream fileStream = getTestImageStream();

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
            .body("projectId", equalTo(TicketingTestData.PROJECT_ID.toString()))
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
    void createIssueWithAttachments_FAILED_noAuthentication() {
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
    void createIssueWithAttachments_FAILED_missingRequiredFields() {
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .multiPart("projectId", TicketingTestData.PROJECT_ID.toString())
            .post(BASE_PATH)
            .then()
            .statusCode(400);
    }

    @Test
    void downloadAttachment_SUCCESS_managerDownloadsAttachment() {
        IssueJson issueWithAttachment = createIssueWithAttachment();
        IssueAttachmentJson attachment = issueWithAttachment.getAttachments().get(0);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueWithAttachment.getId() + "/attachments/"
                + attachment.getAttachmentId() + "/" + attachment.getFileName())
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", containsString("attachment"))
            .header("Content-Disposition", containsString(attachment.getFileName()));
    }

    @Test
    void downloadAttachment_SUCCESS_tenantDownloadsAttachment() {
        IssueJson issueWithAttachment = createIssueWithAttachment();
        IssueAttachmentJson attachment = issueWithAttachment.getAttachments().get(0);

        // Use the same tenant user who created the issue
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                TicketingTestData.USER_FIRST_NAME, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueWithAttachment.getId() + "/attachments/"
                + attachment.getAttachmentId() + "/" + attachment.getFileName())
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", containsString("attachment"));
    }

    @Test
    @Disabled("Issue participants cannot download attachments until requirement is clarified")
    void downloadAttachment_SUCCESS_participantDownloadsAttachment() {
        IssueJson issueWithAttachment = createIssueWithAttachment();
        IssueAttachmentJson attachment = issueWithAttachment.getAttachments().get(0);
        UUID participantId = UUID.randomUUID();

        IssueParticipantKey key = new IssueParticipantKey();
        key.setUserId(participantId);
        key.setIssueId(attachment.getAttachmentId());
        key.setSessionId(issueWithAttachment.getId());

        IssueParticipantEntity participantEntity = new IssueParticipantEntity();
        participantEntity.setKey(key);
        participantEntity.setProjectId(TicketingTestData.PROJECT_ID);
        participantEntity.setRole("CONTRACTOR");

        issueParticipantRepository.insert(participantEntity);

        given()
            .when()
            .cookie(buildCookie(participantId, "participant@test.com",
                "Participant", Map.of(), Map.of(), Map.of()))
            .get(BASE_PATH + "/" + issueWithAttachment.getId() + "/attachments/"
                + attachment.getAttachmentId() + "/" + attachment.getFileName())
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_OCTET_STREAM);
    }

    @Test
    void downloadAttachment_FAILED_noAuthentication() {
        IssueJson issueWithAttachment = createIssueWithAttachment();
        IssueAttachmentJson attachment = issueWithAttachment.getAttachments().get(0);

        given()
            .when()
            .get(BASE_PATH + "/" + issueWithAttachment.getId() + "/attachments/"
                + attachment.getAttachmentId() + "/" + attachment.getFileName())
            .then()
            .statusCode(401);
    }

    @Test
    void downloadAttachment_FAILED_noPermission() {
        IssueJson issueWithAttachment = createIssueWithAttachment();
        IssueAttachmentJson attachment = issueWithAttachment.getAttachments().get(0);

        UUID unauthorizedUserId = UUID.randomUUID();

        given()
            .when()
            .cookie(buildCookie(unauthorizedUserId, "unauthorized@test.com",
                "Unauthorized", Map.of(), Map.of(), Map.of()))
            .get(BASE_PATH + "/" + issueWithAttachment.getId() + "/attachments/"
                + attachment.getAttachmentId() + "/" + attachment.getFileName())
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
        IssueJson issueWithAttachment = createIssueWithAttachment();
        UUID nonExistentAttachmentId = UUID.randomUUID();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueWithAttachment.getId() + "/attachments/"
                + nonExistentAttachmentId + "/test.png")
            .then()
            .statusCode(404);
    }

    @Test
    void deleteAttachment_SUCCESS_managerDeletesAttachment() {
        IssueJson issueWithAttachment = createIssueWithAttachment();
        IssueAttachmentJson attachment = issueWithAttachment.getAttachments().get(0);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + issueWithAttachment.getId() + "/attachments/"
                + attachment.getAttachmentId())
            .then()
            .statusCode(204);

        // Verify attachment is deleted - should return 404 when trying to download
        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + issueWithAttachment.getId() + "/attachments/"
                + attachment.getAttachmentId() + "/" + attachment.getFileName())
            .then()
            .statusCode(404);
    }

    @Test
    void deleteAttachment_FAILED_noAuthentication() {
        IssueJson issueWithAttachment = createIssueWithAttachment();
        IssueAttachmentJson attachment = issueWithAttachment.getAttachments().get(0);

        given()
            .when()
            .delete(BASE_PATH + "/" + issueWithAttachment.getId() + "/attachments/"
                + attachment.getAttachmentId())
            .then()
            .statusCode(401);
    }

    @Test
    void deleteAttachment_FAILED_tenantCannotDelete() {
        IssueJson issueWithAttachment = createIssueWithAttachment();
        IssueAttachmentJson attachment = issueWithAttachment.getAttachments().get(0);

        // Tenant who created the issue should not be able to delete attachments
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                TicketingTestData.USER_FIRST_NAME_1, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + issueWithAttachment.getId() + "/attachments/"
                + attachment.getAttachmentId())
            .then()
            .statusCode(403);
    }

    @Test
    void deleteAttachment_FAILED_participantCannotDelete() {
        IssueJson issueWithAttachment = createIssueWithAttachment();
        IssueAttachmentJson attachment = issueWithAttachment.getAttachments().get(0);
        UUID participantId = UUID.randomUUID();

        IssueParticipantKey key = new IssueParticipantKey();
        key.setUserId(participantId);
        key.setIssueId(attachment.getAttachmentId());
        key.setSessionId(UUID.randomUUID());

        IssueParticipantEntity participantEntity = new IssueParticipantEntity();
        participantEntity.setKey(key);
        participantEntity.setProjectId(TicketingTestData.PROJECT_ID);
        participantEntity.setRole("CONTRACTOR");

        issueParticipantRepository.insert(participantEntity);

        given()
            .when()
            .cookie(buildCookie(participantId, "participant@test.com",
                "Participant", Map.of(), Map.of(), Map.of()))
            .delete(BASE_PATH + "/" + issueWithAttachment.getId() + "/attachments/"
                + attachment.getAttachmentId())
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
        IssueJson issueWithAttachment = createIssueWithAttachment();
        UUID nonExistentAttachmentId = UUID.randomUUID();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + issueWithAttachment.getId() + "/attachments/"
                + nonExistentAttachmentId)
            .then()
            .statusCode(404);
    }

    @Test
    void deleteAttachment_FAILED_managerWithoutProjectPermission() {
        IssueJson issueWithAttachment = createIssueWithAttachment();
        IssueAttachmentJson attachment = issueWithAttachment.getAttachments().get(0);

        // Different manager without access to this project
        Map<String, String> otherProjectRoles = Map.of(UUID.randomUUID().toString(), "MANAGER");

        given()
            .when()
            .cookie(buildManagerCookie(otherProjectRoles))
            .delete(BASE_PATH + "/" + issueWithAttachment.getId() + "/attachments/"
                + attachment.getAttachmentId())
            .then()
            .statusCode(403);
    }

    // Helper method to create an issue with an attachment
    // Uses tenant cookie to create issue (similar to real-world scenario)
    private IssueJson createIssueWithAttachment() {
        final String issueJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"agreementId\":\"" + TicketingTestData.AGREEMENT_ID + "\","
            + "\"title\":\"Issue with attachment for testing\","
            + "\"type\":\"DEFECT\""
            + "}";
        InputStream fileStream = getTestImageStream();

        return given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                TicketingTestData.USER_FIRST_NAME, Map.of(), Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
            .multiPart("issue", issueJson, MediaType.APPLICATION_JSON)
            .multiPart("attachment", TicketingTestData.FILE_PNG_PATH, fileStream, TicketingTestData.FILE_PNG_TYPE)
            .post(BASE_PATH)
            .thenReturn()
            .as(IssueJson.class);
    }

}
