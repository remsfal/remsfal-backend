package de.remsfal.ticketing.boundary;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.ticketing.AbstractTicketingTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class ChatResourceTest extends AbstractTicketingTest {

    static final String CHAT_PATH = "/ticketing/v1/issues/{issueId}/chat";

    static final UUID PROJECT_ID = UUID.randomUUID();
    static final UUID ISSUE_ID = UUID.randomUUID();

    @BeforeEach
    void setUpIssue() {
        insertIssue(PROJECT_ID, ISSUE_ID,
            "Internal issue", IssueType.TASK, IssueStatus.OPEN, IssuePriority.MEDIUM,
            UUID.randomUUID(), null, null, "Issue for internal chat tests");
    }

    @Test
    void getChatMessages_SUCCESS_forProjectMember() {
        insertChatMessage(PROJECT_ID, ISSUE_ID, UUID.randomUUID(), UUID.randomUUID(), "Alice", "Hi team");
        insertChatMessage(PROJECT_ID, ISSUE_ID, UUID.randomUUID(), UUID.randomUUID(), "Bob", "Looking into it");
        insertChatMessage(PROJECT_ID, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Alice", "Other issue");

        given()
            .when()
            .cookie(buildManagerCookie(Map.of(PROJECT_ID.toString(), "MANAGER")))
            .get(CHAT_PATH, ISSUE_ID)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("messages", hasSize(2));
    }

    @Test
    void getChatMessages_FORBIDDEN_forNonProjectMember() {
        given()
            .when()
            .cookie(buildManagerCookie(Map.of(UUID.randomUUID().toString(), "MANAGER")))
            .get(CHAT_PATH, ISSUE_ID)
            .then()
            .statusCode(403);
    }

    @Test
    void getChatMessages_FORBIDDEN_forTenant() {
        final UUID agreementId = UUID.randomUUID();
        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "tenant@example.com", "Tenant", Map.of(), Map.of(),
                Map.of(agreementId.toString(), PROJECT_ID.toString())))
            .get(CHAT_PATH, ISSUE_ID)
            .then()
            .statusCode(403);
    }

    @Test
    void createChatMessage_SUCCESS() {
        final String chatJson = "{\"message\":\"Handwerker beauftragt\"}";

        given()
            .when()
            .cookie(buildManagerCookie(Map.of(PROJECT_ID.toString(), "MANAGER")))
            .contentType(ContentType.JSON)
            .body(chatJson)
            .post(CHAT_PATH, ISSUE_ID)
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .header("location", containsString("/ticketing/v1/issues/" + ISSUE_ID + "/chat/"))
            .body("messageId", notNullValue())
            .body("issueId", equalTo(ISSUE_ID.toString()))
            .body("message", equalTo("Handwerker beauftragt"));
    }

    @Test
    void createChatMessage_FORBIDDEN_forTenant() {
        final UUID agreementId = UUID.randomUUID();
        final String chatJson = "{\"message\":\"Handwerker beauftragt\"}";

        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "tenant@example.com", "Tenant", Map.of(), Map.of(),
                Map.of(agreementId.toString(), PROJECT_ID.toString())))
            .contentType(ContentType.JSON)
            .body(chatJson)
            .post(CHAT_PATH, ISSUE_ID)
            .then()
            .statusCode(403);
    }

}
