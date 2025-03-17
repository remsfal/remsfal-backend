package de.remsfal.service.entity;

import com.datastax.oss.driver.api.core.CqlSession;
import de.remsfal.service.TestData;
import de.remsfal.service.entity.dto.ChatSessionEntity;
import de.remsfal.service.entity.dao.ChatSessionRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ChatSessionRepositoryTest {

    @Inject
    ChatSessionRepository chatSessionRepository;

    @Inject
    Logger logger;

    @Inject
    CqlSession cqlSession;

    @Inject
    EntityManager entityManager;

    static final UUID PROJECT_ID = UUID.randomUUID();
    static final UUID TASK_ID = UUID.randomUUID();
    static final UUID USER_ID_1 = UUID.randomUUID();
    static final UUID USER_ID_2 = UUID.randomUUID();
    static final UUID SESSION_ID = UUID.randomUUID();

    @BeforeEach
    @Transactional
    void setUp() {

        logger.info("Setting up test data");
        String insertSessionCql = "INSERT INTO remsfal.chat_sessions " +
                "(project_id, task_id, session_id, task_type, status, created_at, participants) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        cqlSession.execute(insertSessionCql,
                PROJECT_ID, TASK_ID, SESSION_ID,
                ChatSessionRepository.TaskType.TASK.name(), ChatSessionRepository.Status.OPEN.name(),
                Instant.now(),
                Map.of(USER_ID_1, ChatSessionRepository.ParticipantRole.INITIATOR.name(), USER_ID_2,
                        ChatSessionRepository.ParticipantRole.HANDLER.name()));
        logger.info("Test session created: " + SESSION_ID);
        entityManager.createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, EMAIL, FIRST_NAME, LAST_NAME) " +
                        "VALUES (?,?,?,?,?)")
                .setParameter(1, TestData.USER_ID_3)
                .setParameter(2, TestData.USER_TOKEN_3)
                .setParameter(3, TestData.USER_EMAIL_3)
                .setParameter(4, TestData.USER_FIRST_NAME_3)
                .setParameter(5, TestData.USER_LAST_NAME_3)
                .executeUpdate();
        logger.info("Test data setup complete");
    }

    @AfterEach
    @Transactional
    void tearDown() {
        logger.info("Tearing down test data");
        chatSessionRepository.deleteSession(PROJECT_ID, SESSION_ID, TASK_ID);
        Optional<ChatSessionEntity> deletedSession = chatSessionRepository
                .findSessionById(PROJECT_ID, SESSION_ID, TASK_ID);
        assertFalse(deletedSession.isPresent(), "Session should be deleted from the database");
        logger.info("Test data teardown complete");
        entityManager.createNativeQuery("DELETE FROM USER").executeUpdate();

    }
    @Test
    void createChatSession_SUCCESS() {
        logger.info("Testing createChatSession with valid task type");
        ChatSessionEntity session = chatSessionRepository.createChatSession(PROJECT_ID,
                TASK_ID,
                "TASK", Map.of(USER_ID_1, "INITIATOR", USER_ID_2, "HANDLER"));
        assertNotNull(session, "Session should be created");
        assertEquals(PROJECT_ID, session.getProjectId(), "Project ID should match");
        assertEquals(TASK_ID, session.getTaskId(), "Task ID should match");
        assertEquals("TASK", session.getTaskType(), "Task type should match");
        assertEquals("OPEN", session.getStatus(), "Status should be OPEN");
        assertEquals(2, session.getParticipants().size(),
                "Participants should match the initial value");
        assertNotNull(session.getCreatedAt(), "Created at should not be null");
        assertNotNull(session.getModifiedAt(), "Modified at should not be null");
    }

    @Test
    void createChatSession_INVALID_TASK_TYPE() {
        logger.info("Testing createChatSession with invalid task type");

        Executable executable = () -> chatSessionRepository.createChatSession(
                PROJECT_ID,
                TASK_ID,
                "INVALID_TASK_TYPE",
                Map.of(USER_ID_1, "INITIATOR", USER_ID_2, "HANDLER")
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                executable);

        logger.info("EXCEPTION ACTUAL: " + exception.getMessage());

        assertTrue(
                exception.getMessage().contains("Invalid task type"),
                "Exception message should contain 'Invalid task type'"
        );
    }


    @Test
    void createChatSession_DATABASE_ERROR() {
        logger.info("Testing createChatSession with database error");
        Exception exception = assertThrows(RuntimeException.class, () ->
                chatSessionRepository.createChatSession(null, null, "TASK", null));
        assertTrue(exception.getMessage().contains("An error occurred while creating the session"),
                "Exception message should contain 'An error occurred while creating the session'");
    }

    @Test
    void findSessionById_SUCCESS() {
        logger.info("Testing findById");
        Optional<ChatSessionEntity> session = chatSessionRepository
                .findSessionById(PROJECT_ID, SESSION_ID, TASK_ID);
        assertTrue(session.isPresent(), "Session should exist in the database");
        logger.info("Session found: " + session.get().getSessionId() + " - " + session.get().getStatus());
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
    void findSessionStatusById_SUCCESS() {
        logger.info("Testing findStatusById");
        String status = chatSessionRepository
                .findStatusById(PROJECT_ID, SESSION_ID, TASK_ID);
        logger.info(SESSION_ID + " Status found: " + status);
        assertEquals("OPEN", status, "Status should match the initial value");
    }

    @Test
    void findSessionStatusById_FAILURE() {
        logger.info("Testing findStatusById with random IDs to test failure scenario");
        UUID randomProjectId = UUID.randomUUID();
        UUID randomSessionId = UUID.randomUUID();
        UUID randomTaskId = UUID.randomUUID();
        Exception exception = assertThrows(RuntimeException.class, () ->
                chatSessionRepository.findStatusById(randomProjectId, randomSessionId,randomTaskId));
        System.out.println(exception.getMessage());
        assertTrue(exception.getMessage().contains("An error occurred while fetching the status"));
    }

    @Test
    void findSessionParticipantsById_SUCCESS() {
        logger.info("Testing findParticipantsById");
        Map<UUID, String> participants = chatSessionRepository
                .findParticipantsById(PROJECT_ID, SESSION_ID, TASK_ID);
        for (Map.Entry<UUID, String> entry : participants.entrySet()) {
            logger.info("Participant: " + entry.getKey() + " - " + entry.getValue());
        }
        assertEquals(2, participants.size(), "Participants should match the initial value");
    }

    @Test
    void findTaskTypeById_SUCCESS() {
        logger.info("Testing findTaskTypeById");
        String taskType = chatSessionRepository.findTaskTypeById(PROJECT_ID, SESSION_ID, TASK_ID);
        logger.info(SESSION_ID + " Task type found: " + taskType);
        assertEquals("TASK", taskType, "Task type should match the initial value");
    }

    @Test
    void findTaskTypeById_FAILURE() {
        logger.info("Testing findTaskTypeById with random IDs to test failure scenario");
        UUID randomProjectId = UUID.randomUUID();
        UUID randomSessionId = UUID.randomUUID();
        UUID randomTaskId = UUID.randomUUID();
        Exception exception = assertThrows(RuntimeException.class, () ->
                chatSessionRepository.findTaskTypeById(randomProjectId, randomSessionId, randomTaskId));
        System.out.println(exception.getMessage());
        assertTrue(exception.getMessage().contains("An error occurred while fetching the task type"));
    }

    @Test
    void findParticipantRole_SUCCESS() {
        logger.info("Testing findParticipantRole with valid data");
        String role = chatSessionRepository
                .findParticipantRole(PROJECT_ID, SESSION_ID, TASK_ID, USER_ID_1);
        assertNotNull(role, "Role should not be null");
        assertEquals("INITIATOR", role, "Role should match the expected value");
    }

    @Test
    void findParticipantRole_NO_PARTICIPANTS() {
        logger.info("Testing findParticipantRole with no participants");
        Executable executable = () -> chatSessionRepository.findParticipantRole(
                PROJECT_ID,
                SESSION_ID,
                TASK_ID,
                UUID.randomUUID()
        );

        RuntimeException exception = assertThrows(RuntimeException.class, executable);

        assertTrue(
                exception.getMessage().contains("An error occurred while fetching the participant role"),
                "Exception message should contain 'No participants found'"
        );
    }


    @Test
    void findParticipantRole_DATABASE_ERROR() {
        logger.info("Testing findParticipantRole with database error");
        Exception exception = assertThrows(RuntimeException.class, () ->
                chatSessionRepository.findParticipantRole(null, null, null, null));
        assertTrue(exception.getMessage().contains("An error occurred while fetching the participant role"),
                "Exception message should contain 'An error occurred while fetching the participant role'");
    }

    @Test
    void updateSessionStatus_SUCCESS() {
        logger.info("Testing updateSessionStatus with valid data");
        chatSessionRepository.updateSessionStatus(PROJECT_ID, SESSION_ID, TASK_ID, "CLOSED");
        String status = chatSessionRepository.findStatusById(PROJECT_ID, SESSION_ID, TASK_ID);
        assertEquals("CLOSED", status, "Status should be updated to CLOSED");
    }

    @Test
    void updateSessionStatus_INVALID_STATUS() {
        logger.info("Testing updateSessionStatus with invalid status");
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                chatSessionRepository.updateSessionStatus(PROJECT_ID, SESSION_ID, TASK_ID, "INVALID_STATUS"));
        assertTrue(exception.getMessage().contains("Invalid status: "),
                "Exception message should contain 'Invalid status'");
    }

    @Test
    void updateSessionSatus_DATABASE_ERROR() {
        logger.info("Testing updateSessionStatus with database error");
        Exception exception = assertThrows(RuntimeException.class, () ->
                chatSessionRepository.updateSessionStatus(null, null, null, "CLOSED"));
        logger.info("EXCEPTION ACTUAL: " + exception.getMessage());
        logger.info("EXCEPTION EXPECTED: " + "An error occurred while updating the status");
        assertTrue(exception.getMessage().contains("An error occurred while updating the status"),
                "Exception message should contain 'An error occurred while updating the status'");
    }

    @Test
    void addParticipant_SUCCESS() {
        logger.info("Testing addParticipant with valid data");
        UUID userId = UUID.fromString(TestData.USER_ID_3);
        chatSessionRepository.addParticipant(PROJECT_ID, SESSION_ID, TASK_ID, userId , "OBSERVER");
        Map<UUID, String> participants = chatSessionRepository.findParticipantsById(PROJECT_ID, SESSION_ID, TASK_ID);
        assertEquals("OBSERVER", participants.get(userId), "Role should match the expected value");
    }

    @Test
    void addParticipant_NO_ROW_FOUND() {
        logger.info("Testing addParticipant with no row found");
        Executable executable = () -> chatSessionRepository.addParticipant(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                USER_ID_2,
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
        UUID userId = UUID.fromString(TestData.USER_ID_3);
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                chatSessionRepository.addParticipant(PROJECT_ID, SESSION_ID, TASK_ID, userId, "INVALID_ROLE"));
        logger.info("EXCEPTION ACTUAL: " + exception.getMessage());
        logger.info("EXCEPTION EXPECTED: " + "Invalid role: INVALID_ROLE");
        assertTrue(exception.getMessage().contains("Invalid role: "),
                "Exception message should contain 'Invalid role'");
    }

    @Test
    void addParticipant_NONEXISTENT_USER() {
        logger.info("Testing addParticipant with nonexistent user");
        Executable executable = () -> chatSessionRepository.addParticipant(
                PROJECT_ID,
                SESSION_ID,
                TASK_ID,
                UUID.randomUUID(),
                "OBSERVER"
        );
        RuntimeException exception = assertThrows(RuntimeException.class, executable);
        logger.info("EXCEPTION ACTUAL: " + exception.getMessage());
        logger.info("EXCEPTION EXPECTED: User not found");
        assertTrue(
                exception.getMessage().contains("User not found"),
                "Exception message should contain 'User not found'"
        );
    }


    @Test
    void addParticipant_PARTICIPANT_ALREADY_EXISTS() {
        logger.info("Testing addParticipant with participant already exists");
        Exception exception = assertThrows(RuntimeException.class, () ->
                chatSessionRepository.addParticipant(PROJECT_ID, SESSION_ID, TASK_ID, USER_ID_1, "OBSERVER"));
        logger.info("EXCEPTION ACTUAL: " + exception.getMessage());
        logger.info("EXCEPTION EXPECTED: " + "User already exists in the session");
        assertTrue(exception.getMessage().contains("User already exists in the session"),
                "Exception message should contain 'User already exists in the session'");
    }

    @Test
    void addParticipant_INITIATOR_ALREADY_EXISTS() {
        logger.info("Testing addParticipant with initiator already exists");
        UUID userId = UUID.fromString(TestData.USER_ID_3);
        Exception exception = assertThrows(RuntimeException.class, () ->
                chatSessionRepository.addParticipant(PROJECT_ID, SESSION_ID, TASK_ID, userId, "INITIATOR"));
        logger.info("EXCEPTION ACTUAL: " + exception.getMessage());
        logger.info("EXCEPTION EXPECTED: " + "Initiator already exists in the session");
        assertTrue(exception.getMessage().contains("Initiator already exists in the session"),
                "Exception message should contain 'Initiator already exists in the session'");
    }

    @Test
    void changeParticipantRole_SUCCESS() {
        logger.info("Testing changeParticipantRole with valid data");
        chatSessionRepository.changeParticipantRole(PROJECT_ID, SESSION_ID, TASK_ID, USER_ID_1, "HANDLER");
        Map<UUID, String> participants = chatSessionRepository.findParticipantsById(PROJECT_ID, SESSION_ID, TASK_ID);
        assertEquals("HANDLER", participants.get(USER_ID_1),
                "Role should match the expected value");
    }

    @Test
    void changeParticipantRole_NO_ROW_FOUND() {
        logger.info("Testing changeParticipantRole with no row found");
        Executable executable = () -> chatSessionRepository.changeParticipantRole(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                USER_ID_1,
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
        chatSessionRepository.deleteMember(PROJECT_ID, SESSION_ID, TASK_ID, USER_ID_1);
        Map<UUID, String> participants = chatSessionRepository.findParticipantsById(PROJECT_ID, SESSION_ID, TASK_ID);
        assertNull(participants.get(USER_ID_1), "Participant should be removed");
    }

    @Test
    void removeParticipant_NO_ROW_FOUND() {
        logger.info("Testing removeParticipant with no row found");
        Executable executable = () -> chatSessionRepository.deleteMember(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                USER_ID_1
        );
        RuntimeException exception = assertThrows(RuntimeException.class, executable);
        logger.info("EXCEPTION ACTUAL: " + exception.getMessage());
        logger.info("EXCEPTION EXPECTED: No participants found for the given projectId and sessionId");

        assertTrue(
                exception.getMessage().contains("No participants found for the given projectId and sessionId"),
                "Exception message should contain 'No participants found for the given projectId and sessionId'"
        );
    }


}