package de.remsfal.chat.entity;

import com.datastax.oss.driver.api.core.CqlSession;

import de.remsfal.chat.entity.dao.ChatSessionRepository;
import de.remsfal.chat.entity.dto.ChatSessionEntity;
import de.remsfal.chat.AbstractTicketingTest;
import de.remsfal.chat.TicketingTestData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class ChatSessionRepositoryTest extends AbstractTicketingTest {

    @Inject
    ChatSessionRepository chatSessionRepository;

    @Inject
    CqlSession cqlSession;

    static final UUID TASK_ID = UUID.randomUUID();
    static final UUID SESSION_ID = UUID.randomUUID();

    @BeforeEach
    @Transactional
    void setUp() {
        logger.info("Setting up test data");
        String insertSessionCql = "INSERT INTO remsfal.chat_sessions " +
                "(project_id, task_id, session_id, created_at, participants) " +
                "VALUES (?, ?, ?, ?, ?)";
        cqlSession.execute(insertSessionCql,
            UUID.fromString(TicketingTestData.PROJECT_ID), TASK_ID, SESSION_ID, Instant.now(),
            Map.of(UUID.fromString(TicketingTestData.USER_ID_1), ChatSessionRepository.ParticipantRole.INITIATOR.name(),
                UUID.fromString(TicketingTestData.USER_ID_2), ChatSessionRepository.ParticipantRole.HANDLER.name()));
        logger.info("Test session created: " + SESSION_ID);
        entityManager.createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, EMAIL, FIRST_NAME, LAST_NAME) " +
                        "VALUES (?,?,?,?,?)")
                .setParameter(1, TicketingTestData.USER_ID_3)
                .setParameter(2, TicketingTestData.USER_TOKEN_3)
                .setParameter(3, TicketingTestData.USER_EMAIL_3)
                .setParameter(4, TicketingTestData.USER_FIRST_NAME_3)
                .setParameter(5, TicketingTestData.USER_LAST_NAME_3)
                .executeUpdate();
        logger.info("Test data setup complete");
    }

    @Test
    void createChatSession_SUCCESS() {
        logger.info("Testing createChatSession with valid task type");
        ChatSessionEntity session = chatSessionRepository.createChatSession(
            UUID.fromString(TicketingTestData.PROJECT_ID), TASK_ID,
                Map.of(UUID.fromString(TicketingTestData.USER_ID_1), "INITIATOR",
                    UUID.fromString(TicketingTestData.USER_ID_2), "HANDLER"));
        assertNotNull(session, "Session should be created");
        assertEquals(UUID.fromString(TicketingTestData.PROJECT_ID), session.getProjectId(), "Project ID should match");
        assertEquals(TASK_ID, session.getTaskId(), "Task ID should match");
        assertEquals(2, session.getParticipants().size(),
                "Participants should match the initial value");
        assertNotNull(session.getCreatedAt(), "Created at should not be null");
        assertNotNull(session.getModifiedAt(), "Modified at should not be null");
    }

    @Test
    void findSessionById_SUCCESS() {
        logger.info("Testing findById");
        Optional<ChatSessionEntity> session = chatSessionRepository
                .findSessionById(UUID.fromString(TicketingTestData.PROJECT_ID), SESSION_ID, TASK_ID);
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
                .findParticipantsById(UUID.fromString(TicketingTestData.PROJECT_ID), SESSION_ID, TASK_ID);
        for (Map.Entry<UUID, String> entry : participants.entrySet()) {
            logger.info("Participant: " + entry.getKey() + " - " + entry.getValue());
        }
        assertEquals(2, participants.size(), "Participants should match the initial value");
    }

    @Test
    void findParticipantRole_SUCCESS() {
        logger.info("Testing findParticipantRole with valid data");
        String role = chatSessionRepository
                .findParticipantRole(UUID.fromString(TicketingTestData.PROJECT_ID),
                    SESSION_ID, TASK_ID, UUID.fromString(TicketingTestData.USER_ID_1));
        assertNotNull(role, "Role should not be null");
        assertEquals("INITIATOR", role, "Role should match the expected value");
    }

    @Test
    void findParticipantRole_NO_PARTICIPANTS() {
        logger.info("Testing findParticipantRole with no participants");
        assertNull(chatSessionRepository.findParticipantRole(
            UUID.fromString(TicketingTestData.PROJECT_ID),
                SESSION_ID,
                TASK_ID,
                UUID.randomUUID()
        ));
    }

    @Test
    void addParticipant_SUCCESS() {
        logger.info("Testing addParticipant with valid data");
        UUID userId = UUID.fromString(TicketingTestData.USER_ID_3);
        chatSessionRepository.addParticipant(UUID.fromString(TicketingTestData.PROJECT_ID), SESSION_ID, TASK_ID, userId , "OBSERVER");
        Map<UUID, String> participants = chatSessionRepository
            .findParticipantsById(UUID.fromString(TicketingTestData.PROJECT_ID), SESSION_ID, TASK_ID);
        assertEquals("OBSERVER", participants.get(userId), "Role should match the expected value");
    }

    @Test
    void addParticipant_NO_ROW_FOUND() {
        logger.info("Testing addParticipant with no row found");
        Executable executable = () -> chatSessionRepository.addParticipant(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.fromString(TicketingTestData.USER_ID_2),
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
        UUID userId = UUID.fromString(TicketingTestData.USER_ID_3);
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                chatSessionRepository.addParticipant(UUID.fromString(TicketingTestData.PROJECT_ID),
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
                .addParticipant(UUID.fromString(TicketingTestData.PROJECT_ID), SESSION_ID, TASK_ID,
                    UUID.fromString(TicketingTestData.USER_ID_1), "OBSERVER"));
        logger.info("EXCEPTION ACTUAL: " + exception.getMessage());
        logger.info("EXCEPTION EXPECTED: " + "User already exists in the session");
        assertTrue(exception.getMessage().contains("User already exists in the session"),
                "Exception message should contain 'User already exists in the session'");
    }

    @Test
    void addParticipant_INITIATOR_ALREADY_EXISTS() {
        logger.info("Testing addParticipant with initiator already exists");
        UUID userId = UUID.fromString(TicketingTestData.USER_ID_3);
        Exception exception = assertThrows(RuntimeException.class, () ->
                chatSessionRepository
                .addParticipant(UUID.fromString(TicketingTestData.PROJECT_ID), SESSION_ID, TASK_ID, userId, "INITIATOR"));
        logger.info("EXCEPTION ACTUAL: " + exception.getMessage());
        logger.info("EXCEPTION EXPECTED: " + "Initiator already exists in the session");
        assertTrue(exception.getMessage().contains("Initiator already exists in the session"),
                "Exception message should contain 'Initiator already exists in the session'");
    }

    @Test
    void changeParticipantRole_SUCCESS() {
        logger.info("Testing changeParticipantRole with valid data");
        chatSessionRepository.changeParticipantRole(UUID.fromString(TicketingTestData.PROJECT_ID), SESSION_ID, TASK_ID, UUID.fromString(TicketingTestData.USER_ID_1), "HANDLER");
        Map<UUID, String> participants = chatSessionRepository.findParticipantsById(UUID.fromString(TicketingTestData.PROJECT_ID), SESSION_ID, TASK_ID);
        assertEquals("HANDLER", participants.get(UUID.fromString(TicketingTestData.USER_ID_1)),
                "Role should match the expected value");
    }

    @Test
    void changeParticipantRole_NO_ROW_FOUND() {
        logger.info("Testing changeParticipantRole with no row found");
        Executable executable = () -> chatSessionRepository.changeParticipantRole(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.fromString(TicketingTestData.USER_ID_1),
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
        chatSessionRepository.deleteMember(UUID.fromString(TicketingTestData.PROJECT_ID), SESSION_ID, TASK_ID, UUID.fromString(TicketingTestData.USER_ID_1));
        Map<UUID, String> participants = chatSessionRepository.findParticipantsById(UUID.fromString(TicketingTestData.PROJECT_ID), SESSION_ID, TASK_ID);
        assertNull(participants.get(UUID.fromString(TicketingTestData.USER_ID_1)), "Participant should be removed");
    }

    @Test
    void removeParticipant_NO_ROW_FOUND() {
        logger.info("Testing removeParticipant with no row found");
        Executable executable = () -> chatSessionRepository.deleteMember(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.fromString(TicketingTestData.USER_ID_1)
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