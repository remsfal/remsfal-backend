package de.remsfal.ticketing.boundary;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import de.remsfal.ticketing.entity.dao.IssueParticipantRepository;
import de.remsfal.ticketing.entity.dto.IssueParticipantEntity;
import de.remsfal.ticketing.entity.dto.IssueParticipantKey;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class IssueResourceTest extends AbstractResourceTest {

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

    @Test
    void getIssues_FAILED_tenantWithUnauthorizedTenancyId() {
        UUID unauthorizedTenancyId = UUID.randomUUID();

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                        TicketingTestData.USER_FIRST_NAME_1, Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
                .queryParam("tenancyId", unauthorizedTenancyId.toString())
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
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(createJson1)
                .post(BASE_PATH);

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                        TicketingTestData.USER_FIRST_NAME_1, Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
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
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
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
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .delete(BASE_PATH + "/" + issueId)
                .then()
                .statusCode(204);

        given()
                .when()
                .cookie(buildCookie(contractorId, "contractor@test.com",
                        "Contractor", Map.of(), Map.of()))
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
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
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
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
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
                        TicketingTestData.USER_FIRST_NAME_1, Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
                .contentType(ContentType.JSON)
                .body(createJson2)
                .post(BASE_PATH);

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                        TicketingTestData.USER_FIRST_NAME_1, Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
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
                        TicketingTestData.USER_FIRST_NAME_1, Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
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
                        "Contractor", Map.of(), Map.of()));

    }
        void getIssues_FAILED_noTenancyMembership () {
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
        void getIssues_FAILED_invalidTenancyIdForbidden () {
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
        void getIssues_SUCCESS_tenancyIssuesForMember () {
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

    @ParameterizedTest
    @MethodSource("provideIssueTestCases")
    void getIssues_SUCCESS_variousScenarios(String title, String description) {

        final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                + "\"title\":\"" + title + "\","
                + "\"type\":\"TASK\""
                + "}";

        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                        TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                .contentType(ContentType.JSON)
                .body(createJson)
                .post(BASE_PATH);


        given()
                .when()
                .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                        TicketingTestData.USER_FIRST_NAME_1, Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
                .get(BASE_PATH)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    private static Stream<Arguments> provideIssueTestCases() {
        return Stream.of(
                Arguments.of("Dedupe Issue", "Deduplication of issues"),
                Arguments.of("Combined Role Issue", "User with both tenant and contractor roles"),
                Arguments.of("No Filter Issue", "Without status filter")
        );
    }

        @Test
        void getIssues_SUCCESS_handlesIssueWithNullId () {

            given()
                    .when()
                    .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                            TicketingTestData.USER_FIRST_NAME_1, Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
                    .get(BASE_PATH)
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON);
        }


        @Test
        void getIssue_SUCCESS_participantViewsFilteredIssue () {

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
                            "Participant", Map.of(), Map.of()))
                    .get(BASE_PATH + "/" + issueId)
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("id", equalTo(issueId))
                    .body("title", equalTo(TicketingTestData.ISSUE_TITLE))
                    .body("description", nullValue());
        }

        @Test
        void getIssue_FAILED_noPermission () {

            final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                    + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
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

            UUID unauthorizedUserId = UUID.randomUUID();


            given()
                    .when()
                    .cookie(buildCookie(unauthorizedUserId, "unauthorized@test.com",
                            "Unauthorized", Map.of(), Map.of()))
                    .get(BASE_PATH + "/" + issueId)
                    .then()
                    .statusCode(403);
        }


        @Test
        void deleteIssue_FAILED_noPermissionToDelete () {
            final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                    + "\"title\":\"" + TicketingTestData.ISSUE_TITLE + "\","
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

            UUID unauthorizedUserId = UUID.randomUUID();

            given()
                    .when()
                    .cookie(buildCookie(unauthorizedUserId, "unauthorized@test.com",
                            "Unauthorized", Map.of(), Map.of()))
                    .delete(BASE_PATH + "/" + issueId)
                    .then()
                    .statusCode(403);
        }


        @Test
        void getIssues_SUCCESS_tenantWithSpecificTenancyIdGetsIssues () {
            final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                    + "\"title\":\"Tenancy Specific Issue\","
                    + "\"type\":\"TASK\""
                    + "}";

            given()
                    .when()
                    .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                            TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
                    .contentType(ContentType.JSON)
                    .body(createJson)
                    .post(BASE_PATH);

            UUID tenancyId = UUID.fromString(TicketingTestData.TENANT_PROJECT_ROLES.keySet().iterator().next());

            given()
                    .when()
                    .cookie(buildCookie(TicketingTestData.USER_ID_1, TicketingTestData.USER_EMAIL_1,
                            TicketingTestData.USER_FIRST_NAME_1, Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
                    .queryParam("tenancyId", tenancyId.toString())
                    .get(BASE_PATH)
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON);
        }

        @Test
        void getIssues_SUCCESS_participantIssueAddedToCollection () {
            final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                    + "\"title\":\"Participant Issue\","
                    + "\"type\":\"TASK\""
                    + "}";

            Response createResponse = given()
                    .when()
                    .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                            TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
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
                            "Participant", Map.of(), Map.of()))
                    .get(BASE_PATH)
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("issues", hasSize(1));
        }

        @Test
        void getIssues_SUCCESS_deduplicationRemovesDuplicates () {
            final String createJson = "{ \"projectId\":\"" + TicketingTestData.PROJECT_ID + "\","
                    + "\"title\":\"Duplicate Test Issue\","
                    + "\"type\":\"TASK\""
                    + "}";

            Response createResponse = given()
                    .when()
                    .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                            TicketingTestData.USER_FIRST_NAME, TicketingTestData.MANAGER_PROJECT_ROLES, Map.of()))
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
                            TicketingTestData.USER_FIRST_NAME_1, Map.of(), TicketingTestData.TENANT_PROJECT_ROLES))
                    .get(BASE_PATH)
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON);
        }

}
