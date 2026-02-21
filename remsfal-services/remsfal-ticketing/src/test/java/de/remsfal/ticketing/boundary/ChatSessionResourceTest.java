package de.remsfal.ticketing.boundary;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.TicketingTestData;
import de.remsfal.ticketing.entity.dao.ChatSessionRepository.ParticipantRole;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response.Status;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class ChatSessionResourceTest extends AbstractTicketingTest {

    @Inject
    CqlSession cqlSession;

    static final String BASE_PATH = "/ticketing/v1/issues";
    static final String CHAT_SESSION_PATH = BASE_PATH + "/{issueId}/chats";
    static final String CHAT_SESSION_ID_PATH = CHAT_SESSION_PATH + "/{sessionId}";

    @BeforeEach
    protected void setup() throws Exception {
        logger.info("Setting up test data");
        super.setupTestFile();
        logger.info("Setting up issues for chat sessions");
        insertIssue(TicketingTestData.PROJECT_ID_1, TicketingTestData.ISSUE_ID_1,
            "Test Issue 1", IssueType.TASK, IssueStatus.OPEN, IssuePriority.MEDIUM,
            TicketingTestData.USER_ID, null, TicketingTestData.USER_ID,
            "Test issue for chat session 1");
        insertIssue(TicketingTestData.PROJECT_ID_1, TicketingTestData.ISSUE_ID_2,
            "Test Issue 2", IssueType.TASK, IssueStatus.OPEN, IssuePriority.MEDIUM,
            TicketingTestData.USER_ID, null, TicketingTestData.USER_ID,
            "Test issue for chat session 2");
        logger.info("Setting up chat sessions");
        String insertChatSessionCql = "INSERT INTO remsfal.chat_sessions " +
            "(project_id, issue_id, session_id, created_at, participants) " +
            "VALUES (?, ?, ?, ?, ?)";
        cqlSession.execute(insertChatSessionCql,
            TicketingTestData.PROJECT_ID_1, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1,
            Instant.now(),
            Map.of(
                TicketingTestData.USER_ID_4, ParticipantRole.INITIATOR.name(),
                TicketingTestData.USER_ID_3, ParticipantRole.HANDLER.name()));
        cqlSession.execute(insertChatSessionCql,
            TicketingTestData.PROJECT_ID_1, TicketingTestData.ISSUE_ID_2, TicketingTestData.CHAT_SESSION_ID_2,
            Instant.now(),
            Map.of(
                TicketingTestData.USER_ID_4, ParticipantRole.INITIATOR.name(),
                TicketingTestData.USER_ID_3, ParticipantRole.HANDLER.name()));
    }

    private Map<String, String> rolesManagerP1() {
        return Map.of(TicketingTestData.PROJECT_ID_1.toString(), "MANAGER");
    }

    private Map<String, String> rolesNone() {
        return Map.of();
    }

    private String nameOf(String first, String last) {
        return first + " " + last;
    }

    @Test
    void getChatSession_UNAUTHENTICATED() {
        given()
            .when()
            .get(CHAT_SESSION_ID_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getChatSession_UNPRIVILEGED() {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_2, TicketingTestData.USER_EMAIL_2,
                    nameOf(TicketingTestData.USER_FIRST_NAME_2, TicketingTestData.USER_LAST_NAME_2), true,
                    rolesNone(), rolesNone(), rolesNone(), Duration.ofMinutes(10)))
            .get(CHAT_SESSION_ID_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void deleteChatSession_INVALID_INPUT() {
        given()
            .when()
            .cookie(buildManagerCookie(rolesManagerP1()))
            .delete(CHAT_SESSION_ID_PATH, TicketingTestData.ISSUE_ID_1, UUID.randomUUID().toString())
            .then()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void getChatSession_FAILURE_INVALID_SESSION() {
        // invalid input - non-existing sessionId in the database
        String nonExistingSessionId = UUID.randomUUID().toString();
        given()
            .when()
            .cookie(buildManagerCookie(rolesManagerP1()))
            .get(CHAT_SESSION_ID_PATH, TicketingTestData.ISSUE_ID_1, nonExistingSessionId)
            .then()
            .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void createChatSession_OnTask_SUCCESS() {
        given()
            .when()
            .cookie(buildManagerCookie(rolesManagerP1()))
            .contentType(ContentType.JSON)
            .post(CHAT_SESSION_PATH, TicketingTestData.ISSUE_ID_1.toString())
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .contentType(ContentType.JSON)
            .header("Location", Matchers.containsString(CHAT_SESSION_PATH
                .replace("{issueId}", TicketingTestData.ISSUE_ID_1.toString())))
            .and().body("sessionId", notNullValue());
    }

    @Test
    void getChatSession_SUCCESS() {
        given()
            .when()
            .cookie(buildManagerCookie(rolesManagerP1()))
            .get(CHAT_SESSION_ID_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("sessionId", equalTo(TicketingTestData.CHAT_SESSION_ID_1.toString()));
    }

    @Test
    void deleteChatSession_SUCCESS() {
        given()
            .when()
            .cookie(buildManagerCookie(rolesManagerP1()))
            .delete(CHAT_SESSION_ID_PATH, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void getChatSessions_SUCCESS() {
        given()
            .when()
            .cookie(buildManagerCookie(rolesManagerP1()))
            .get(CHAT_SESSION_PATH, TicketingTestData.ISSUE_ID_1)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .body("size", equalTo(1))
            .body("chatSessions[0].sessionId", equalTo(TicketingTestData.CHAT_SESSION_ID_1.toString()));
    }

}
