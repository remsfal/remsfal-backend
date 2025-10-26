package de.remsfal.ticketing.boundary;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.ticketing.TicketingTestData;
import de.remsfal.ticketing.entity.dao.ChatSessionRepository;
import de.remsfal.ticketing.entity.dao.ChatSessionRepository.ParticipantRole;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class ChatParticipantResourceTest extends AbstractResourceTest {

    @Inject
    CqlSession cqlSession;

    static final String BASE_PATH = "/ticketing/v1/issues";
    static final String CHAT_SESSION_PATH = BASE_PATH + "/{issueId}/chats";
    static final String CHAT_SESSION_ID_PATH = CHAT_SESSION_PATH + "/{sessionId}";
    static final String CHAT_PARTICIPANTS_PATH = CHAT_SESSION_ID_PATH + "/participants";

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
        logger.info("Setting up chat sessions and participants");
        String insertChatSessionCql = "INSERT INTO remsfal.chat_sessions " +
            "(project_id, issue_id, session_id, created_at, participants) " +
            "VALUES (?, ?, ?, ?, ?)";
        cqlSession.execute(insertChatSessionCql,
            TicketingTestData.PROJECT_ID_1, TicketingTestData.ISSUE_ID_1, TicketingTestData.CHAT_SESSION_ID_1_UUID,
            Instant.now(),
            Map.of(
                TicketingTestData.USER_ID_4, ParticipantRole.INITIATOR.name(),
                TicketingTestData.USER_ID_3, ParticipantRole.HANDLER.name()));
        cqlSession.execute(insertChatSessionCql,
            TicketingTestData.PROJECT_ID_1, TicketingTestData.ISSUE_ID_2, TicketingTestData.CHAT_SESSION_ID_2_UUID,
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
    void changeParticipantRole_UNPRIVILEGED() {
        String newRole = ChatSessionRepository.ParticipantRole.OBSERVER.toString();
        String jsonBody = "\"" + newRole + "\"";

        given()
            .body(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_3, TicketingTestData.USER_EMAIL_3,
                    nameOf(TicketingTestData.USER_FIRST_NAME_3, TicketingTestData.USER_LAST_NAME_3), true,
                    rolesNone(), rolesNone(), Duration.ofMinutes(10)))
            .put(CHAT_SESSION_ID_PATH + "/participants/{participantId}/role", TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1, TicketingTestData.USER_ID_4)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void getParticipant_FAILURE_INVALID_PARTICIPANT() {
        String nonExistingParticipantId = UUID.randomUUID().toString();
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .get(CHAT_SESSION_ID_PATH + "/participants/{participantId}", TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1, nonExistingParticipantId)
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void getParticipant_FAILURE_UNPRIVILEGED() {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_3, TicketingTestData.USER_EMAIL_3,
                    nameOf(TicketingTestData.USER_FIRST_NAME_3, TicketingTestData.USER_LAST_NAME_3), true,
                    rolesNone(), rolesNone(), Duration.ofMinutes(10)))
            .get(CHAT_SESSION_ID_PATH + "/participants/{participantId}", TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1, TicketingTestData.USER_ID_4)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void changeParticipantRole_FAILURE_INVALID_ROLE() {
        // invalid input - non-existing role in the database
        String nonExistingRole = "INVALID_ROLE";
        String jsonBody = "\"" + nonExistingRole + "\"";
        given()
            .body(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .put(CHAT_SESSION_ID_PATH + "/participants/{participantId}/role", TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1, TicketingTestData.USER_ID_3)
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void getParticipants_EMPTY_SESSION() {
        // Create an empty session
        String emptySessionId = UUID.randomUUID().toString();

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .get(CHAT_PARTICIPANTS_PATH, TicketingTestData.ISSUE_ID_1.toString(), emptySessionId)
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void removeParticipant_INVALID_INPUT() {
        String path_task1_session1 = (CHAT_SESSION_ID_PATH + "/participants/{participantId}")
            .replace("{issueId}", TicketingTestData.ISSUE_ID_1.toString())
            .replace("{sessionId}", TicketingTestData.CHAT_SESSION_ID_1.toString());
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .delete(path_task1_session1
                .replace("{participantId}", UUID.randomUUID().toString()))
            .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID_3, TicketingTestData.USER_EMAIL_3,
                    nameOf(TicketingTestData.USER_FIRST_NAME_3, TicketingTestData.USER_LAST_NAME_3), true,
                    rolesNone(), rolesNone(), Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .delete(path_task1_session1
                .replace("{participantId}", TicketingTestData.USER_ID_3.toString()))
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void getParticipants_SUCCESS() {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .get(CHAT_PARTICIPANTS_PATH, TicketingTestData.ISSUE_ID_2.toString(), TicketingTestData.CHAT_SESSION_ID_2)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("[1].userId", equalTo(TicketingTestData.USER_ID_4.toString()))
            .body("[1].userRole", equalTo("INITIATOR"));
    }

    @Test
    void getParticipant_SUCCESS() {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                    nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                    rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .get(CHAT_SESSION_ID_PATH + "/participants/{participantId}", TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1, TicketingTestData.USER_ID_4)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("[0].userId", equalTo(TicketingTestData.USER_ID_4.toString()))
            .body("[0].userRole", equalTo("INITIATOR"));
    }

    @Test
    void changeParticipantRole_SUCCESS() {
        String newRole = ChatSessionRepository.ParticipantRole.OBSERVER.toString();
        String jsonBody = "\"" + newRole + "\"";

        given()
            .body(jsonBody)
            .contentType(MediaType.APPLICATION_JSON)
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .put(CHAT_SESSION_ID_PATH + "/participants/{participantId}/role", TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1, TicketingTestData.USER_ID_4)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("[0].userId", equalTo(TicketingTestData.USER_ID_4.toString()))
            .body("[0].userRole", equalTo(newRole));
    }

    @Test
    void removeParticipant_SUCCESS() {
        given()
            .when()
            .cookie(buildCookie(TicketingTestData.USER_ID, TicketingTestData.USER_EMAIL,
                nameOf(TicketingTestData.USER_FIRST_NAME, TicketingTestData.USER_LAST_NAME), true,
                rolesManagerP1(), rolesNone(), Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .delete(CHAT_SESSION_ID_PATH + "/participants/{participantId}", TicketingTestData.ISSUE_ID_1.toString(), TicketingTestData.CHAT_SESSION_ID_1, TicketingTestData.USER_ID_3)
            .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

}