package de.remsfal.ticketing.entity;

import com.datastax.oss.driver.api.core.CqlSession;


import com.datastax.oss.quarkus.test.CassandraTestResource;
import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.TicketingTestData;
import de.remsfal.ticketing.entity.dao.ChatSessionRepository;
import de.remsfal.ticketing.entity.dto.ChatSessionEntity;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
public class ChatSessionRepositoryTest extends AbstractTicketingTest {

    @Inject
    ChatSessionRepository chatSessionRepository;



    @Inject
    CqlSession cqlSession;

    static final UUID TASK_ID = UUID.randomUUID();
    static final UUID SESSION_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        logger.info("Setting up test data for REST tests");

        String insertSessionCql = "INSERT INTO remsfal.chat_sessions " +
                "(project_id, issue_id, session_id, created_at, participants) " +
                "VALUES (?, ?, ?, ?, ?)";

        cqlSession.execute(insertSessionCql,
                TicketingTestData.PROJECT_ID, TASK_ID, SESSION_ID, Instant.now(),
                Map.of(TicketingTestData.USER_ID_1, "INITIATOR",
                        TicketingTestData.USER_ID_2, "HANDLER"));

        String insertParticipantCql = "INSERT INTO remsfal.issue_participants " +
                "(user_id, issue_id, session_id, project_id, role, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        Instant now = Instant.now();

        cqlSession.execute(insertParticipantCql,
                TicketingTestData.USER_ID_1, TASK_ID, SESSION_ID,
                TicketingTestData.PROJECT_ID, "INITIATOR", now);

        cqlSession.execute(insertParticipantCql,
                TicketingTestData.USER_ID_2, TASK_ID, SESSION_ID,
                TicketingTestData.PROJECT_ID, "HANDLER", now);

        logger.info("Test setup completed");
    }

    @Test
    void createChatSession_SUCCESS() {
        logger.info("Testing createChatSession with valid task type");
        ChatSessionEntity session = chatSessionRepository.createChatSession(
            TicketingTestData.PROJECT_ID, TASK_ID,
                Map.of(UUID.fromString(TicketingTestData.USER_ID_1.toString()), "INITIATOR",
                    UUID.fromString(TicketingTestData.USER_ID_2.toString()), "HANDLER"));
        assertNotNull(session, "Session should be created");
        assertEquals(TicketingTestData.PROJECT_ID, session.getProjectId(), "Project ID should match");
        assertEquals(TASK_ID, session.getIssueId(), "Task ID should match");
        assertEquals(2, session.getParticipants().size(),
                "Participants should match the initial value");
        assertNotNull(session.getCreatedAt(), "Created at should not be null");
        assertNotNull(session.getModifiedAt(), "Modified at should not be null");
    }

    @Test
    void findSessionById_SUCCESS() {
        logger.info("Testing findById");
        Optional<ChatSessionEntity> session = chatSessionRepository
                .findSessionById(TicketingTestData.PROJECT_ID, SESSION_ID, TASK_ID);
        logger.info("test");
        assertTrue(session.isPresent(), "Session should exist in the database");
        assertEquals(SESSION_ID, session.get().getSessionId(), "Session ID should match");
    }

    @Test
    void findSessionById_FAILURE() {
        logger.info("Testing findById with random IDs to test failure scenario");
        UUID randomProjectId = UUID.randomUUID();
        UUID randomSessionId = UUID.randomUUID();
        UUID randomTaskId = UUID.randomUUID();
        Optional<ChatSessionEntity> session = chatSessionRepository.findSessionById(randomProjectId,
                randomSessionId,
                randomTaskId);
        assertFalse(session.isPresent(), "Session should not exist in the database");
    }

    @Test
    void findSessionParticipantsById_SUCCESS() {
        logger.info("Testing findParticipantsById");
        Map<UUID, String> participants = chatSessionRepository
                .findParticipantsById(TicketingTestData.PROJECT_ID, SESSION_ID, TASK_ID);
        for (Map.Entry<UUID, String> entry : participants.entrySet()) {
            logger.info("Participant: " + entry.getKey() + " - " + entry.getValue());
        }
        assertEquals(2, participants.size(), "Participants should match the initial value");
    }

    @Test
    void findParticipantRole_SUCCESS() {
        logger.info("Testing findParticipantRole with valid data");
        String role = chatSessionRepository
                .findParticipantRole(TicketingTestData.PROJECT_ID,
                    SESSION_ID, TASK_ID, UUID.fromString(TicketingTestData.USER_ID_1.toString()));
        assertNotNull(role, "Role should not be null");
        assertEquals("INITIATOR", role, "Role should match the expected value");
    }

    @Test
    void findParticipantRole_NO_PARTICIPANTS() {
        logger.info("Testing findParticipantRole with no participants");
        assertNull(chatSessionRepository.findParticipantRole(
            TicketingTestData.PROJECT_ID,
                SESSION_ID,
                TASK_ID,
                UUID.randomUUID()
        ));
    }

    @Test
    void addParticipant_SUCCESS() {
        logger.info("Testing addParticipant with valid data");
        UUID userId = UUID.fromString(TicketingTestData.USER_ID_3.toString());
        chatSessionRepository.addParticipant(TicketingTestData.PROJECT_ID, SESSION_ID, TASK_ID, userId , "OBSERVER");
        Map<UUID, String> participants = chatSessionRepository
            .findParticipantsById(TicketingTestData.PROJECT_ID, SESSION_ID, TASK_ID);
        assertEquals("OBSERVER", participants.get(userId), "Role should match the expected value");
    }

    @Test
    void addParticipant_NO_ROW_FOUND() {
        logger.info("Testing addParticipant with no row found");
        Executable executable = () -> chatSessionRepository.addParticipant(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.fromString(TicketingTestData.USER_ID_2.toString()),
                "OBSERVER"
        );

        RuntimeException exception = assertThrows(RuntimeException.class, executable);

        logger.info("EXCEPTION ACTUAL: " + exception.getMessage());
        logger.info("EXCEPTION EXPECTED: No participants found for the given projectId and sessionId");

        assertTrue(
                exception.getMessage().contains("No participants found for the given projectId and sessionId"),
                "Exception message should contain " +
                        "'No participants found for the given projectId and sessionId'"
        );
    }


    @Test
    void addParticipant_INVALID_ROLE() {
        logger.info("Testing addParticipant with invalid role");
        UUID userId = UUID.fromString(TicketingTestData.USER_ID_3.toString());
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                chatSessionRepository.addParticipant(TicketingTestData.PROJECT_ID,
                    SESSION_ID, TASK_ID, userId, "INVALID_ROLE"));
        logger.info("EXCEPTION ACTUAL: " + exception.getMessage());
        logger.info("EXCEPTION EXPECTED: " + "Invalid role: INVALID_ROLE");
        assertTrue(exception.getMessage().contains("Invalid role: "),
                "Exception message should contain 'Invalid role'");
    }

    @Test
    void addParticipant_PARTICIPANT_ALREADY_EXISTS() {
        logger.info("Testing addParticipant with participant already exists");
        Exception exception = assertThrows(RuntimeException.class, () ->
                chatSessionRepository
                .addParticipant(TicketingTestData.PROJECT_ID, SESSION_ID, TASK_ID,
                    UUID.fromString(TicketingTestData.USER_ID_1.toString()), "OBSERVER"));
        logger.info("EXCEPTION ACTUAL: " + exception.getMessage());
        logger.info("EXCEPTION EXPECTED: " + "User already exists in the session");
        assertTrue(exception.getMessage().contains("User already exists in the session"),
                "Exception message should contain 'User already exists in the session'");
    }

    @Test
    void addParticipant_INITIATOR_ALREADY_EXISTS() {
        logger.info("Testing addParticipant with initiator already exists");
        UUID userId = UUID.fromString(TicketingTestData.USER_ID_3.toString());
        Exception exception = assertThrows(RuntimeException.class, () ->
                chatSessionRepository
                .addParticipant(TicketingTestData.PROJECT_ID, SESSION_ID, TASK_ID, userId, "INITIATOR"));
        logger.info("EXCEPTION ACTUAL: " + exception.getMessage());
        logger.info("EXCEPTION EXPECTED: " + "Initiator already exists in the session");
        assertTrue(exception.getMessage().contains("Initiator already exists in the session"),
                "Exception message should contain 'Initiator already exists in the session'");
    }

    @Test
    void changeParticipantRole_SUCCESS() {
        logger.info("Testing changeParticipantRole with valid data");
        chatSessionRepository.changeParticipantRole(TicketingTestData.PROJECT_ID, SESSION_ID, TASK_ID, TicketingTestData.USER_ID_1, "HANDLER");
        Map<UUID, String> participants = chatSessionRepository.findParticipantsById(TicketingTestData.PROJECT_ID, SESSION_ID, TASK_ID);
        assertEquals("HANDLER", participants.get(UUID.fromString(TicketingTestData.USER_ID_1.toString())),
                "Role should match the expected value");
    }

    @Test
    void changeParticipantRole_NO_ROW_FOUND() {
        logger.info("Testing changeParticipantRole with no row found");
        Executable executable = () -> chatSessionRepository.changeParticipantRole(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                TicketingTestData.USER_ID_1,
                "HANDLER"
        );
        RuntimeException exception = assertThrows(RuntimeException.class, executable);
        assertTrue(
                exception.getMessage().contains("An error occurred while changing the participant role"),
                "Exception message should contain 'An error occurred while changing the participant role'"
        );
    }


    @Test
    void removeParticipant_SUCCESS() {
        logger.info("Testing removeParticipant with valid data");
        chatSessionRepository.deleteMember(TicketingTestData.PROJECT_ID, SESSION_ID, TASK_ID, TicketingTestData.USER_ID_1);
        Map<UUID, String> participants = chatSessionRepository.findParticipantsById(TicketingTestData.PROJECT_ID, SESSION_ID, TASK_ID);
        assertNull(participants.get(UUID.fromString(TicketingTestData.USER_ID_1.toString())), "Participant should be removed");
    }

    @Test
    void removeParticipant_NO_ROW_FOUND() {
        logger.info("Testing removeParticipant with no row found");
        Executable executable = () -> chatSessionRepository.deleteMember(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                TicketingTestData.USER_ID_1
        );
        RuntimeException exception = assertThrows(RuntimeException.class, executable);
        logger.info("EXCEPTION ACTUAL: " + exception.getMessage());
        logger.info("EXCEPTION EXPECTED: No participants found for the given projectId and sessionId");

        assertTrue(
                exception.getMessage().contains("No participants found for the given projectId and sessionId"),
                "Exception message should contain 'No participants found for the given projectId and sessionId'"
        );
    }




    /**
     * Test für deleteMember: "An error occurred while removing the participant"
     */
    @Test
    void deleteMember_DATABASE_ERROR() {
        logger.info("Testing deleteMember with database error");

        UUID invalidProjectId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        Executable executable = () -> chatSessionRepository.deleteMember(
                invalidProjectId,
                SESSION_ID,
                TASK_ID,
                TicketingTestData.USER_ID_1
        );

        RuntimeException exception = assertThrows(RuntimeException.class, executable);
        logger.info("EXCEPTION ACTUAL: " + exception.getMessage());

        assertTrue(
                exception.getMessage().contains("An error occurred while removing the participant") ||
                        exception.getMessage().contains("No participants found"),
                "Exception message should contain error message about removing participant"
        );
    }

    /**
     * Test für ensureNoExistingInitiator: Alle Szenarien
     */
    @Test
    void ensureNoExistingInitiator_WITH_EXISTING_INITIATOR() {
        logger.info("Testing ensureNoExistingInitiator when initiator exists");

        UUID newUserId = UUID.fromString(TicketingTestData.USER_ID_3.toString());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                chatSessionRepository.addParticipant(
                        TicketingTestData.PROJECT_ID,
                        SESSION_ID,
                        TASK_ID,
                        newUserId,
                        "INITIATOR"
                )
        );

        assertTrue(exception.getMessage().contains("Initiator already exists in the session"),
                "Exception should indicate initiator already exists");
    }

    @Test
    void ensureNoExistingInitiator_WITHOUT_EXISTING_INITIATOR() {
        logger.info("Testing ensureNoExistingInitiator when no initiator exists");

        UUID testProjectId = UUID.randomUUID();
        UUID testTaskId = UUID.randomUUID();
        UUID testSessionId = UUID.randomUUID();

        String insertSessionCql = "INSERT INTO remsfal.chat_sessions " +
                "(project_id, issue_id, session_id, created_at, participants) " +
                "VALUES (?, ?, ?, ?, ?)";

        cqlSession.execute(insertSessionCql,
                testProjectId, testTaskId, testSessionId, Instant.now(),
                Map.of(TicketingTestData.USER_ID_1, ChatSessionRepository.ParticipantRole.HANDLER.name(),
                        TicketingTestData.USER_ID_2, ChatSessionRepository.ParticipantRole.OBSERVER.name()));

        UUID newInitiatorId = UUID.fromString(TicketingTestData.USER_ID_3.toString());

        assertDoesNotThrow(() ->
                chatSessionRepository.addParticipant(
                        testProjectId,
                        testSessionId,
                        testTaskId,
                        newInitiatorId,
                        "INITIATOR"
                )
        );

        Map<UUID, String> participants = chatSessionRepository
                .findParticipantsById(testProjectId, testSessionId, testTaskId);
        assertEquals("INITIATOR", participants.get(newInitiatorId),
                "New initiator should be added successfully");
    }

    @Test
    void ensureNoExistingInitiator_EMPTY_PARTICIPANTS() {
        logger.info("Testing ensureNoExistingInitiator with empty participants map");

        UUID testProjectId = UUID.randomUUID();
        UUID testTaskId = UUID.randomUUID();
        UUID testSessionId = UUID.randomUUID();

        String insertSessionCql = "INSERT INTO remsfal.chat_sessions " +
                "(project_id, issue_id, session_id, created_at, participants) " +
                "VALUES (?, ?, ?, ?, ?)";

        cqlSession.execute(insertSessionCql,
                testProjectId, testTaskId, testSessionId, Instant.now(), Map.of());

        UUID initiatorId = UUID.fromString(TicketingTestData.USER_ID_1.toString());

        assertDoesNotThrow(() ->
                chatSessionRepository.addParticipant(
                        testProjectId,
                        testSessionId,
                        testTaskId,
                        initiatorId,
                        "INITIATOR"
                )
        );

        Map<UUID, String> participants = chatSessionRepository
                .findParticipantsById(testProjectId, testSessionId, testTaskId);
        assertEquals("INITIATOR", participants.get(initiatorId),
                "Initiator should be added to empty session");
    }




    @Test
    void findStatusById_SESSION_NOT_FOUND() {
        logger.info("Testing findStatusById with non-existent session");

        UUID randomProjectId = UUID.randomUUID();
        UUID randomSessionId = UUID.randomUUID();
        UUID randomTaskId = UUID.randomUUID();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                chatSessionRepository.findStatusById(randomProjectId, randomSessionId, randomTaskId)
        );

        assertTrue(
                exception.getMessage().contains("An error occurred while fetching the status"),
                "Exception message should contain error about fetching status"
        );
    }


    @Test
    void findTaskTypeById_SESSION_NOT_FOUND() {
        logger.info("Testing findTaskTypeById with non-existent session");

        UUID randomProjectId = UUID.randomUUID();
        UUID randomSessionId = UUID.randomUUID();
        UUID randomTaskId = UUID.randomUUID();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                chatSessionRepository.findTaskTypeById(randomProjectId, randomSessionId, randomTaskId)
        );

        assertTrue(
                exception.getMessage().contains("An error occurred while fetching the task type"),
                "Exception message should contain error about fetching task type"
        );
    }


    @Test
    void changeParticipantRole_USER_NOT_PARTICIPANT() {
        logger.info("Testing changeParticipantRole when user is not a participant");

        UUID nonParticipantUserId = UUID.randomUUID();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                chatSessionRepository.changeParticipantRole(
                        TicketingTestData.PROJECT_ID,
                        SESSION_ID,
                        TASK_ID,
                        nonParticipantUserId,
                        "OBSERVER"
                )
        );

        assertTrue(
                exception.getMessage().contains("An error occurred while changing the participant role"),
                "Exception message should contain 'An error occurred while changing the participant role'"
        );
    }

    @Test
    void changeParticipantRole_PARTICIPANT_NOT_FOUND_IN_ISSUE_PARTICIPANTS() {
        logger.info("Testing changeParticipantRole when participant not found in issue_participants");

        UUID testProjectId = UUID.randomUUID();
        UUID testIssueId = UUID.randomUUID();
        UUID testSessionId = UUID.randomUUID();
        UUID testUserId = UUID.randomUUID();

        String insertSessionCql = "INSERT INTO remsfal.chat_sessions " +
                "(project_id, issue_id, session_id, created_at, participants) " +
                "VALUES (?, ?, ?, ?, ?)";

        cqlSession.execute(insertSessionCql,
                testProjectId, testIssueId, testSessionId, Instant.now(),
                Map.of(testUserId, "HANDLER"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                chatSessionRepository.changeParticipantRole(
                        testProjectId,
                        testSessionId,
                        testIssueId,
                        testUserId,
                        "OBSERVER"
                )
        );

        assertTrue(
                exception.getMessage().contains("An error occurred while changing the participant role"),
                "Exception message should contain error about changing participant role"
        );
    }

    @Test
    void changeParticipantRole_UPDATE_ROLE_GENERAL_EXCEPTION() {
        logger.info("Testing changeParticipantRole with general exception during updateRole");

        UUID testProjectId = UUID.randomUUID();
        UUID testIssueId = UUID.randomUUID();
        UUID testSessionId = UUID.randomUUID();
        UUID testUserId = UUID.randomUUID();

        String insertSessionCql = "INSERT INTO remsfal.chat_sessions " +
                "(project_id, issue_id, session_id, created_at, participants) " +
                "VALUES (?, ?, ?, ?, ?)";

        cqlSession.execute(insertSessionCql,
                testProjectId, testIssueId, testSessionId, Instant.now(),
                Map.of(testUserId, "HANDLER"));

        String insertParticipantCql = "INSERT INTO remsfal.issue_participants " +
                "(user_id, issue_id, session_id, project_id, role, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        cqlSession.execute(insertParticipantCql,
                testUserId, testIssueId, testSessionId, testProjectId, "HANDLER", Instant.now());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                chatSessionRepository.changeParticipantRole(
                        testProjectId,
                        testSessionId,
                        testIssueId,
                        testUserId,
                        null
                )
        );

        assertTrue(
                exception.getMessage().contains("An error occurred while changing the participant role"),
                "Exception message should contain error about changing participant role"
        );
    }


        @Test
        void createChatSession_PARTICIPANT_INSERTION_FAILURE() {
                logger.info("Testing createChatSession with participant insertion failure");

                UUID testProjectId = UUID.randomUUID();
                UUID testIssueId = UUID.randomUUID();

                Map<UUID, String> invalidParticipants = new HashMap<>();
                invalidParticipants.put(null, "INITIATOR");
                RuntimeException exception = assertThrows(RuntimeException.class,
                        () -> chatSessionRepository.createChatSession(
                                testProjectId,
                                testIssueId,
                                invalidParticipants));

                logger.info("EXCEPTION ACTUAL: " + exception.getMessage());
                assertTrue(
                        exception.getMessage().contains("Failed to create chat session participants"),
                        "Exception message should contain 'Failed to create chat session participants'");
        }




        @Test
        void createChatSession_PARTIAL_PARTICIPANT_INSERTION_WITH_ROLLBACK() {
                logger.info("Testing createChatSession with partial participant insertion requiring rollback");

                UUID testProjectId = UUID.randomUUID();
                UUID testIssueId = UUID.randomUUID();

                // Create a map with valid and invalid participants
                // The invalid one should cause the entire operation to rollback
                Map<UUID, String> mixedParticipants = new HashMap<>();
                mixedParticipants.put(TicketingTestData.USER_ID_1, "INITIATOR");
                mixedParticipants.put(null, "HANDLER"); // This will cause failure

                RuntimeException exception = assertThrows(RuntimeException.class,
                        () -> chatSessionRepository.createChatSession(
                                testProjectId,
                                testIssueId,
                                mixedParticipants));

                assertTrue(
                        exception.getMessage().contains("Failed to create chat session participants"),
                        "Should throw exception about participant creation failure");

                // Verify that the successfully inserted participant was rolled back
                // by checking it doesn't exist in issue_participants table
                boolean participantExists = false;
                try {
                        String checkCql = "SELECT * FROM remsfal.issue_participants WHERE user_id = ? AND issue_id = ?";
                        com.datastax.oss.driver.api.core.cql.ResultSet rs = cqlSession.execute(checkCql,
                                TicketingTestData.USER_ID_1, testIssueId);
                        participantExists = rs.one() != null;
                } catch (Exception e) {
                        logger.info("Error checking participant existence: " + e.getMessage());
                }

                assertFalse(participantExists,
                        "Participant should have been rolled back and not exist in database");
        }
}
