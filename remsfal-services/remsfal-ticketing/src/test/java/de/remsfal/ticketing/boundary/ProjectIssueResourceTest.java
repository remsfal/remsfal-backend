package de.remsfal.ticketing.boundary;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;

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
class ProjectIssueResourceTest extends AbstractTicketingTest {

    static final String BASE_PATH = "/ticketing/v1/issues";

    // --- List Issues ---

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
            .body("size", equalTo(0));
    }

    @Test
    void getIssues_SUCCESS_withFilters() {
        final String issue1Json = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID_1 + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE_1 + "\","
            + "\"type\":\"TASK\""
            + "}";

        final String issue2Json = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID_2 + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE_2 + "\","
            + "\"type\":\"DEFECT\""
            + "}";

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issue1Json)
            .post(BASE_PATH)
            .then()
            .statusCode(201);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .contentType(ContentType.JSON)
            .body(issue2Json)
            .post(BASE_PATH)
            .then()
            .statusCode(201);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .queryParam("projectId", TicketingTestData.PROJECT_ID_1.toString())
            .get(BASE_PATH)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("issues", hasSize(1))
            .body("size", equalTo(1));
    }

    // --- Create Project Issue ---

    @Test
    void createProjectIssue_SUCCESS_issueIsCreated() {
        final String json = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
            + "\"title\":\"" + TicketingTestData.ISSUE_TITLE_5 + "\","
            + "\"description\":\"" + TicketingTestData.ISSUE_DESCRIPTION_5 + "\","
            + "\"type\":\"INQUIRY\""
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
            .body("title", equalTo(TicketingTestData.ISSUE_TITLE_5))
            .body("description", equalTo(TicketingTestData.ISSUE_DESCRIPTION_5.replace("\\n", "\n")))
            .body("type", equalTo("INQUIRY"))
            .body("status", equalTo("OPEN"))
            .body("projectId", equalTo(TicketingTestData.PROJECT_ID.toString()))
            .body("reporterId", equalTo(TicketingTestData.USER_ID.toString()))
            .body("assigneeId", nullValue())
            .body("category", nullValue())
            .body("priority", equalTo("UNCLASSIFIED"))
            .body("visibleToTenants", equalTo(false))
            .body("agreementId", nullValue())
            .body("rentalUnitId", nullValue())
            .body("rentalUnitType", nullValue())
            .body("location", nullValue());
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

    // --- Get Issue ---

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

        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "unauthorized@test.com",
                "Unauthorized", Map.of(), Map.of(), Map.of()))
            .get(BASE_PATH + "/" + issueId)
            .then()
            .statusCode(403);
    }

    // --- Update Issue ---

    @Test
    void updateIssue_SUCCESS_issueIsUpdated() {
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

    // --- Delete Issue ---

    @Test
    void deleteIssue_SUCCESS_issueIsDeleted() {
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

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + issueId)
            .then()
            .statusCode(204);

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

        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "unauthorized@test.com",
                "Unauthorized", Map.of(), Map.of(), Map.of()))
            .delete(BASE_PATH + "/" + issueId)
            .then()
            .statusCode(403);
    }

    // --- Issue Relations ---

    @Test
    void addBlockedByRelation_SUCCESS_mirroredAsBlocksOnTarget() {
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

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + sourceId + "/blocked-by/" + targetId)
            .then()
            .statusCode(200)
            .body("blockedBy", hasSize(1))
            .body("blockedBy[0]", equalTo(targetId));

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
    void addBlocksRelation_SUCCESS_noDuplicatesOnRepeat() {
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

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + sourceId + "/blocks/" + targetId)
            .then()
            .statusCode(200);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + sourceId + "/blocks/" + targetId)
            .then()
            .statusCode(200);

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
    void addRelatedToRelation_SUCCESS_isSymmetric() {
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
    void addDuplicateOfRelation_SUCCESS_isSymmetric() {
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
    void addBlocksRelation_SUCCESS_mirroredAsBlockedByOnTarget() {
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

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + sourceId + "/blocks/" + targetId)
            .then()
            .statusCode(200)
            .body("blocks", hasSize(1))
            .body("blocks[0]", equalTo(targetId));

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
    void addBlocksRelation_FAILED_nonExistingTargetIssue() {
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

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + sourceId + "/blocks/" + UUID.randomUUID())
            .then()
            .statusCode(404);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + sourceId)
            .then()
            .statusCode(200)
            .body("blocks", anyOf(nullValue(), hasSize(0)));
    }

    @Test
    void addBlocksRelation_FAILED_selfRelation() {
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

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + sourceId + "/blocks/" + sourceId)
            .then()
            .statusCode(400);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + sourceId)
            .then()
            .statusCode(200)
            .body("blocks", anyOf(nullValue(), hasSize(0)));
    }

    @Test
    void deleteIssue_SUCCESS_cleansUpAllRelationsOnOtherIssues() {
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

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + mainId + "/blocks/" + blockedId)
            .then()
            .statusCode(200);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + mainId + "/blocked-by/" + blockerId)
            .then()
            .statusCode(200);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + mainId + "/related-to/" + relatedId)
            .then()
            .statusCode(200);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + mainId + "/duplicate-of/" + duplicateId)
            .then()
            .statusCode(200);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + mainId + "/children/" + childId)
            .then()
            .statusCode(200);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .put(BASE_PATH + "/" + mainId + "/parent/" + parentId)
            .then()
            .statusCode(200);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + mainId)
            .then()
            .statusCode(204);

        assertNoRelationsContain(mainId, blockerId);
        assertNoRelationsContain(mainId, blockedId);
        assertNoRelationsContain(mainId, relatedId);
        assertNoRelationsContain(mainId, duplicateId);
        assertNoRelationsContain(mainId, parentId);
        assertNoRelationsContain(mainId, childId);
    }

    @Test
    void deleteBlocksRelation_SUCCESS_updatesBothSides() {
        setupTestIssues();
        String sourceId = TicketingTestData.ISSUE_ID_1.toString();
        String targetId = TicketingTestData.ISSUE_ID_2.toString();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + sourceId + "/blocks/" + targetId)
            .then()
            .statusCode(200);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + sourceId + "/blocks/" + targetId)
            .then()
            .statusCode(204);

        assertNoRelationsContain(targetId, sourceId);
        assertNoRelationsContain(sourceId, targetId);
    }

    @Test
    void deleteBlocksRelation_FAILED_nonExistingRelation() {
        setupTestIssues();
        String sourceId = TicketingTestData.ISSUE_ID_1.toString();
        String targetId = TicketingTestData.ISSUE_ID_2.toString();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + sourceId + "/blocks/" + targetId)
            .then()
            .statusCode(400);

        assertNoRelationsContain(sourceId, targetId);
        assertNoRelationsContain(targetId, sourceId);
    }

    @Test
    void deleteRelation_FAILED_forbiddenWhenNoProjectRole() {
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
    void deleteBlockedByRelation_SUCCESS_updatesBothSides() {
        setupTestIssues();
        String sourceId = TicketingTestData.ISSUE_ID_1.toString();
        String targetId = TicketingTestData.ISSUE_ID_2.toString();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + sourceId + "/blocked-by/" + targetId)
            .then()
            .statusCode(200);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + sourceId + "/blocked-by/" + targetId)
            .then()
            .statusCode(204);

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
    void deleteRelatedToRelation_SUCCESS_updatesBothSides() {
        setupTestIssues();
        String aId = TicketingTestData.ISSUE_ID_1.toString();
        String bId = TicketingTestData.ISSUE_ID_2.toString();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + aId + "/related-to/" + bId)
            .then()
            .statusCode(200);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + aId + "/related-to/" + bId)
            .then()
            .statusCode(204);

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
    void deleteRelation_FAILED_unknownRelationType() {
        setupTestIssues();
        String sourceId = TicketingTestData.ISSUE_ID_1.toString();
        String targetId = TicketingTestData.ISSUE_ID_2.toString();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + sourceId + "/unknown-type/" + targetId)
            .then()
            .statusCode(404);
    }

    @Test
    void deleteDuplicateOfRelation_SUCCESS_updatesBothSides() {
        setupTestIssues();
        String originalId = TicketingTestData.ISSUE_ID_1.toString();
        String duplicateId = TicketingTestData.ISSUE_ID_2.toString();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + originalId + "/duplicate-of/" + duplicateId)
            .then()
            .statusCode(200);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + originalId + "/duplicate-of/" + duplicateId)
            .then()
            .statusCode(204);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + originalId)
            .then()
            .statusCode(200)
            .body("duplicateOf", anyOf(nullValue(), hasSize(0)));

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + duplicateId)
            .then()
            .statusCode(200)
            .body("duplicateOf", anyOf(nullValue(), hasSize(0)));
    }

    @Test
    void deleteChildrenRelation_SUCCESS_updatesBothSides() {
        setupTestIssues();
        String parentId = TicketingTestData.ISSUE_ID_1.toString();
        String childId = TicketingTestData.ISSUE_ID_2.toString();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .post(BASE_PATH + "/" + parentId + "/children/" + childId)
            .then()
            .statusCode(200);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + parentId + "/children/" + childId)
            .then()
            .statusCode(204);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + parentId)
            .then()
            .statusCode(200)
            .body("childrenIssues", anyOf(nullValue(), hasSize(0)));

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + childId)
            .then()
            .statusCode(200)
            .body("parentIssue", nullValue());
    }

    @Test
    void deleteParentRelation_SUCCESS_updatesBothSides() {
        setupTestIssues();
        String parentId = TicketingTestData.ISSUE_ID_1.toString();
        String childId = TicketingTestData.ISSUE_ID_2.toString();

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .put(BASE_PATH + "/" + childId + "/parent/" + parentId)
            .then()
            .statusCode(200);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .delete(BASE_PATH + "/" + childId + "/parent/" + parentId)
            .then()
            .statusCode(204);

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + childId)
            .then()
            .statusCode(200)
            .body("parentIssue", nullValue());

        given()
            .when()
            .cookie(buildManagerCookie(TicketingTestData.MANAGER_PROJECT_ROLES))
            .get(BASE_PATH + "/" + parentId)
            .then()
            .statusCode(200)
            .body("childrenIssues", anyOf(nullValue(), hasSize(0)));
    }

    // --- Attachments (Manager) ---

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

    // --- Helper ---

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

}
