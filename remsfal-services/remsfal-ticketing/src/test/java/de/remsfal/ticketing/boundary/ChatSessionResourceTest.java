package de.remsfal.ticketing.boundary;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.ticketing.TicketingTestData;
import de.remsfal.ticketing.entity.dao.ChatSessionRepository.ParticipantRole;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class ChatSessionResourceTest extends AbstractResourceTest {

    @Inject
    CqlSession cqlSession;

    static final String BASE_PATH = "/ticketing/v1/issues";
    static final String CHAT_SESSION_PATH = BASE_PATH + "/{issueId}/chats";
    static final String CHAT_SESSION_ID_PATH = CHAT_SESSION_PATH + "/{sessionId}";

    @BeforeEach
    protected void setup() throws Exception {
        logger.info("Setting up test data");
        super.setupTestFiles();
        logger.info("Setting up issues for chat sessions");
        String insertIssueCql = "INSERT INTO remsfal.issues " +
            "(project_id, issue_id, type, title, status, reporter_id, owner_id, description, created_by, created_at, modified_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        cqlSession.execute(insertIssueCql,
            TicketingTestData.PROJECT_ID_1, TicketingTestData.ISSUE_ID_1,
            "TASK", "Test Issue 1", "OPEN",
            TicketingTestData.USER_ID, TicketingTestData.USER_ID,
            "Test issue for chat session 1",
            TicketingTestData.USER_ID, Instant.now(), Instant.now());
        cqlSession.execute(insertIssueCql,
            TicketingTestData.PROJECT_ID_1, TicketingTestData.ISSUE_ID_2,
            "TASK", "Test Issue 2", "OPEN",
            TicketingTestData.USER_ID, TicketingTestData.USER_ID,
            "Test issue for chat session 2",
            TicketingTestData.USER_ID, Instant.now(), Instant.now());
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
            .get(CHAT_SESSION_ID_PATH, TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getChatSession_UNPRIVILEGED() {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_3, TicketingTestData.USER_EMAIL_3,
                    nameOf(TicketingTestData.USER_FIRST_NAME_3, TicketingTestData.USER_LAST_NAME_3), true,
                    rolesNone(), rolesNone(), Duration.ofMinutes(10)))
            .get(CHAT_SESSION_ID_PATH, TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void deleteChatSession_INVALID_INPUT() {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .delete(CHAT_SESSION_ID_PATH, TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .delete(CHAT_SESSION_ID_PATH, TicketingTestData.ISSUE_ID_1.toString(), UUID.randomUUID().toString())
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void getChatSession_FAILURE_INVALID_SESSION() {
        // invalid input - non-existing sessionId in the database
        String nonExistingSessionId = UUID.randomUUID().toString();
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .get(CHAT_SESSION_ID_PATH, TicketingTestData.ISSUE_ID_1.toString(), nonExistingSessionId)
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void createChatSession_OnTask_SUCCESS() {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .post(CHAT_SESSION_PATH, TicketingTestData.ISSUE_ID_1.toString())
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .contentType(ContentType.JSON)
            .header("Location", Matchers.containsString(CHAT_SESSION_PATH
                .replace("{issueId}", TicketingTestData.ISSUE_ID_1.toString())))
            .and().body("sessionId", notNullValue());
    }

    @Test
    void getChatSession_SUCCESS() {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .get(CHAT_SESSION_ID_PATH, TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("sessionId", equalTo(TicketingTestData.CHAT_SESSION_ID_1));
    }

    @Test
    void deleteChatSession_SUCCESS() {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .delete(CHAT_SESSION_ID_PATH, TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1)
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void getChatSessions_SUCCESS() {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .get(CHAT_SESSION_PATH, TicketingTestData.ISSUE_ID_1.toString())
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .body("size", equalTo(1))
            .body("chatSessions[0].sessionId", equalTo(TicketingTestData.CHAT_SESSION_ID_1));
    }

}
