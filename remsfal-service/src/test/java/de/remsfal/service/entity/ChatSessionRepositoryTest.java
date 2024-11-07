package de.remsfal.service.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.remsfal.core.model.project.ChatSessionModel;
import de.remsfal.core.model.project.ChatSessionModel.ParticipantRole;
import de.remsfal.service.entity.dto.ChatMessageEntity;
import de.remsfal.service.entity.dto.ChatSessionEntity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import de.remsfal.service.entity.dao.ChatSessionRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import de.remsfal.service.TestData;
import de.remsfal.service.AbstractTest;
import de.remsfal.core.model.ProjectMemberModel.UserRole;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class ChatSessionRepositoryTest extends AbstractTest {

    @Inject
    ChatSessionRepository repository;

    @Inject
    EntityManager entityManager;

    String TASK_ID = UUID.randomUUID().toString();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    @Transactional
    protected void setupTestChatSessions() throws InterruptedException {
        // Insert users
        entityManager.createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, EMAIL, FIRST_NAME, LAST_NAME) VALUES (?,?,?,?,?)")
                .setParameter(1, TestData.USER_ID)
                .setParameter(2, TestData.USER_TOKEN)
                .setParameter(3, TestData.USER_EMAIL)
                .setParameter(4, TestData.USER_FIRST_NAME)
                .setParameter(5, TestData.USER_LAST_NAME)
                .executeUpdate();

        entityManager.createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, EMAIL, FIRST_NAME, LAST_NAME) VALUES (?,?,?,?,?)")
                .setParameter(1, TestData.USER_ID_2)
                .setParameter(2, TestData.USER_TOKEN_2)
                .setParameter(3, TestData.USER_EMAIL_2)
                .setParameter(4, TestData.USER_FIRST_NAME_2)
                .setParameter(5, TestData.USER_LAST_NAME_2)
                .executeUpdate();

        entityManager.createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, EMAIL, FIRST_NAME, LAST_NAME) VALUES (?,?,?,?,?)")
                .setParameter(1, TestData.USER_ID_3)
                .setParameter(2, TestData.USER_TOKEN_3)
                .setParameter(3, TestData.USER_EMAIL_3)
                .setParameter(4, TestData.USER_FIRST_NAME_3)
                .setParameter(5, TestData.USER_LAST_NAME_3)
                .executeUpdate();

        entityManager.createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, EMAIL, FIRST_NAME, LAST_NAME) VALUES (?,?,?,?,?)")
                .setParameter(1, TestData.USER_ID_4)
                .setParameter(2, TestData.USER_TOKEN_4)
                .setParameter(3, TestData.USER_EMAIL_4)
                .setParameter(4, TestData.USER_FIRST_NAME_4)
                .setParameter(5, TestData.USER_LAST_NAME_4)
                .executeUpdate();

        // Insert project
        entityManager.createNativeQuery("INSERT INTO PROJECT (ID, TITLE) VALUES (?,?)")
                .setParameter(1, TestData.PROJECT_ID)
                .setParameter(2, TestData.PROJECT_TITLE)
                .executeUpdate();

        // Insert project memberships
        entityManager.createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, USER_ROLE) VALUES (?,?,?)")
                .setParameter(1, TestData.PROJECT_ID)
                .setParameter(2, TestData.USER_ID)
                .setParameter(3, UserRole.MANAGER.name())
                .executeUpdate();

        entityManager.createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, USER_ROLE) VALUES (?,?,?)")
                .setParameter(1, TestData.PROJECT_ID)
                .setParameter(2, TestData.USER_ID_2)
                .setParameter(3, UserRole.LESSOR.name())
                .executeUpdate();

        entityManager.createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, USER_ROLE) VALUES (?,?,?)")
                .setParameter(1, TestData.PROJECT_ID)
                .setParameter(2, TestData.USER_ID_3)
                .setParameter(3, UserRole.CARETAKER.name()) // Ensure "CARETAKER" is a valid enum constant
                .executeUpdate();

        entityManager.createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, USER_ROLE) VALUES (?,?,?)")
                .setParameter(1, TestData.PROJECT_ID)
                .setParameter(2, TestData.USER_ID_4)
                .setParameter(3, UserRole.CARETAKER.name()) // Ensure "CARETAKER" is a valid enum constant
                .executeUpdate();

        entityManager.createNativeQuery("INSERT INTO TASK (ID, TYPE, PROJECT_ID, TITLE, STATUS, OWNER_ID, CREATED_BY) VALUES (?,?,?,?,?,?,?)")
                .setParameter(1, TASK_ID)
                .setParameter(2, "TASK")
                .setParameter(3, TestData.PROJECT_ID)
                .setParameter(4, TestData.TASK_TITLE_1)
                .setParameter(5, "OPEN")
                .setParameter(6, TestData.USER_ID)
                .setParameter(7, TestData.USER_ID)
                .executeUpdate();

        // Insert chat session
        String CHAT_SESSION_ID = UUID.randomUUID().toString();
        entityManager.createNativeQuery("INSERT INTO CHAT_SESSION (ID, PROJECT_ID, TASK_ID, TASK_TYPE, STATUS) VALUES (?,?,?,?,?)")
                .setParameter(1, CHAT_SESSION_ID)
                .setParameter(2, TestData.PROJECT_ID)
                .setParameter(3, TASK_ID)
                .setParameter(4, "TASK")
                .setParameter(5, "OPEN")
                .executeUpdate();

        // Insert chat session participants
        entityManager.createNativeQuery("INSERT INTO CHAT_SESSION_PARTICIPANT (CHAT_SESSION_ID, PARTICIPANT_ID, ROLE) VALUES (?,?,?)")
                .setParameter(1, CHAT_SESSION_ID)
                .setParameter(2, TestData.USER_ID_2)
                .setParameter(3, "INITIATOR")
                .executeUpdate();

        entityManager.createNativeQuery("INSERT INTO CHAT_SESSION_PARTICIPANT (CHAT_SESSION_ID, PARTICIPANT_ID, ROLE) VALUES (?,?,?)")
                .setParameter(1, CHAT_SESSION_ID)
                .setParameter(2, TestData.USER_ID_3)
                .setParameter(3, "HANDLER")
                .executeUpdate();

        entityManager.createNativeQuery("INSERT INTO CHAT_SESSION_PARTICIPANT (CHAT_SESSION_ID, PARTICIPANT_ID, ROLE) VALUES (?,?,?)")
                .setParameter(1, CHAT_SESSION_ID)
                .setParameter(2, TestData.USER_ID)
                .setParameter(3, "OBSERVER")
                .executeUpdate();

        // Insert chat messages
        entityManager.createNativeQuery("INSERT INTO CHAT_MESSAGE (ID, CHAT_SESSION_ID, SENDER_ID, CONTENT_TYPE, CONTENT) VALUES (?,?,?,?,?)")
                .setParameter(1, UUID.randomUUID().toString())
                .setParameter(2, CHAT_SESSION_ID)
                .setParameter(3, TestData.USER_ID_2)
                .setParameter(4, "TEXT")
                .setParameter(5, "Hello, this is a test message. I am the initiator.")
                .executeUpdate();

        // Introduce a minimal delay to ensure distinct CREATED_AT
        Thread.sleep(1000); // Sleep for 1 second

        entityManager.createNativeQuery("INSERT INTO CHAT_MESSAGE (ID, CHAT_SESSION_ID, SENDER_ID, CONTENT_TYPE, CONTENT) VALUES (?,?,?,?,?)")
                .setParameter(1, UUID.randomUUID().toString())
                .setParameter(2, CHAT_SESSION_ID)
                .setParameter(3, TestData.USER_ID_3)
                .setParameter(4, "TEXT")
                .setParameter(5, "Hello, this is a test message. I am the handler.")
                .executeUpdate();

    }

    @AfterEach
    @Transactional
    protected void cleanDB() {
        // Delete CHAT_MESSAGE entries first
        entityManager.createNativeQuery("DELETE FROM CHAT_MESSAGE").executeUpdate();

        // Then delete CHAT_SESSION_PARTICIPANT entries
        entityManager.createNativeQuery("DELETE FROM CHAT_SESSION_PARTICIPANT").executeUpdate();

        // Delete CHAT_SESSION entries
        entityManager.createNativeQuery("DELETE FROM CHAT_SESSION").executeUpdate();

        // Delete TASK entries
        entityManager.createNativeQuery("DELETE FROM TASK").executeUpdate();

        // Delete PROJECT_MEMBERSHIP entries
        entityManager.createNativeQuery("DELETE FROM PROJECT_MEMBERSHIP").executeUpdate();

        // Delete PROJECT entries
        entityManager.createNativeQuery("DELETE FROM PROJECT").executeUpdate();

        // Finally, delete USER entries
        entityManager.createNativeQuery("DELETE FROM USER").executeUpdate();
    }

    @Test
    void hashCode_SUCCESS_chatSessionEntity() {
        final ChatSessionEntity entity = repository.findChatSessionsByProjectId(TestData.PROJECT_ID).get(0);
        assertNotNull(entity, "ChatSessionEntity should not be null");
        assertTrue(entity.hashCode() != 0, "Hash code should not be zero");
    }

    @Test
    void equals_SUCCESS_chatSessionEntity() {
        final List<ChatSessionEntity> entities = repository.findChatSessionsByProjectId(TestData.PROJECT_ID);
        assertNotNull(entities, "Entities list should not be null");
        assertEquals(1, entities.size(), "There should be exactly one chat session");

        final ChatSessionEntity entity1 = entities.get(0);
        final ChatSessionEntity entity2 = repository.findChatSessionById(entity1.getId());

        assertNotNull(entity2, "Retrieved ChatSessionEntity should not be null");
        assertEquals(entity1, entity2, "Both ChatSessionEntities should be equal");
    }

    @Test
    void findChatSessionById_SUCCESS() {
        final List<ChatSessionEntity> entities = repository.findChatSessionsByProjectId(TestData.PROJECT_ID);
        assertNotNull(entities, "Entities list should not be null");
        assertEquals(1, entities.size(), "There should be exactly one chat session");

        final ChatSessionEntity expectedSession = entities.get(0);
        final ChatSessionEntity actualSession = repository.findChatSessionById(expectedSession.getId());

        assertNotNull(actualSession, "Retrieved ChatSessionEntity should not be null");
        assertEquals(expectedSession, actualSession, "Retrieved ChatSessionEntity should match the expected session");
    }

    @Test
    void exportChatLogs_WITH_MESSAGES() {
        final List<ChatSessionEntity> sessions = repository.findChatSessionsByProjectId(TestData.PROJECT_ID);
        assertNotNull(sessions, "Sessions list should not be null");
        assertEquals(1, sessions.size(), "There should be exactly one chat session");

        final ChatSessionEntity session = sessions.get(0);
        final List<ChatMessageEntity> messages = repository.exportChatLogs(session.getId());

        assertNotNull(messages, "Exported chat logs should not be null");
        assertEquals(2, messages.size(), "There should be two chat messages");

        assertTrue(messages.get(0).getTimestamp().before(messages.get(1).getTimestamp()) ||
                        messages.get(0).getTimestamp().equals(messages.get(1).getTimestamp()),
                "Chat messages should be sorted by timestamp ascending");

        ChatMessageEntity firstMessage = messages.get(0);
        ChatMessageEntity secondMessage = messages.get(1);

        assertEquals(TestData.USER_ID_2, firstMessage.getSenderId(), "First message should be from the initiator");
        assertEquals("Hello, this is a test message. I am the initiator.", firstMessage.getContent(), "First message content should match");

        assertEquals(TestData.USER_ID_3, secondMessage.getSenderId(), "Second message should be from the handler");
        assertEquals("Hello, this is a test message. I am the handler.", secondMessage.getContent(), "Second message content should match");
    }

   @Test
    void exportChatLogsAsJsonString_SUCCESS() throws Exception {

        final String projectId = TestData.PROJECT_ID;
        final List<ChatSessionEntity> sessions = repository.findChatSessionsByProjectId(projectId);
        assertNotNull(sessions, "Sessions list should not be null");
        assertEquals(1, sessions.size(), "There should be exactly one chat session");

        final ChatSessionEntity session = sessions.get(0);
        final String json = repository.exportChatLogsAsJsonString(session.getId());

        JsonNode rootNode = objectMapper.readTree(json);

        assertTrue(rootNode.has("CHAT_SESSION_ID"), "JSON should contain CHAT_SESSION_ID");
        assertTrue(rootNode.has("TASK_ID"), "JSON should contain TASK_ID");
        assertTrue(rootNode.has("PROJECT_ID"), "JSON should contain PROJECT_ID");
        assertTrue(rootNode.has("TASK_TYPE"), "JSON should contain TASK_TYPE");
        assertTrue(rootNode.has("messages"), "JSON should contain messages");

        assertEquals(session.getId(), rootNode.get("CHAT_SESSION_ID").asText(), "CHAT_SESSION_ID should match");
        assertEquals(session.getTaskId(), rootNode.get("TASK_ID").asText(), "TASK_ID should match");
        assertEquals(session.getProjectId(), rootNode.get("PROJECT_ID").asText(), "PROJECT_ID should match");
        assertEquals(session.getTaskType().toString(), rootNode.get("TASK_TYPE").asText(), "TASK_TYPE should match");

        JsonNode messagesNode = rootNode.get("messages");
        assertTrue(messagesNode.isArray(), "messages should be an array");
        assertEquals(session.getMessages().size(), messagesNode.size(), "Number of messages should match");

        for (int i = 0; i < messagesNode.size(); i++) {
            JsonNode messageNode = messagesNode.get(i);
            ChatMessageEntity message = session.getMessages().get(i);

            assertEquals(message.getId(), messageNode.get("MESSAGE_ID").asText(), "MESSAGE_ID should match");
            assertEquals(message.getSenderId(), messageNode.get("SENDER_ID").asText(), "SENDER_ID should match");
            assertEquals(message.getContentType().toString(), messageNode.get("MESSAGE_TYPE").asText(), "MESSAGE_TYPE should match");
            assertEquals(message.getContent(), messageNode.get("MESSAGE_CONTENT").asText(), "MESSAGE_CONTENT should match");
            assertNotNull(messageNode.get("DATETIME"), "DATETIME should not be null");
            assertNotNull(messageNode.get("MEMBER_ROLE"), "MEMBER_ROLE should not be null");
        }
    }

    @Test
        void findParticipantRole_SUCCESS() {
            final List<ChatSessionEntity> sessions = repository.findChatSessionsByProjectId(TestData.PROJECT_ID);
            assertNotNull(sessions, "Sessions list should not be null");
            assertEquals(1, sessions.size(), "There should be exactly one chat session");

            final ChatSessionEntity session = sessions.get(0);

            ParticipantRole role1 = repository.findParticipantRole(session.getId(), TestData.USER_ID_2);
            assertEquals(ParticipantRole.INITIATOR, role1, "User should have INITIATOR role");

            ParticipantRole role2 = repository.findParticipantRole(session.getId(), TestData.USER_ID_3);
            assertEquals(ParticipantRole.HANDLER, role2, "User should have HANDLER role");

            ParticipantRole role3 = repository.findParticipantRole(session.getId(), TestData.USER_ID);
            assertEquals(ParticipantRole.OBSERVER, role3, "User should have OBSERVER role");
        }

    @Test
    void findChatSessionsByProjectId_SUCCESS() {
        final List<ChatSessionEntity> sessions = repository.findChatSessionsByProjectId(TestData.PROJECT_ID);
        assertNotNull(sessions, "Sessions list should not be null");
        assertEquals(1, sessions.size(), "There should be exactly one chat session");

        final ChatSessionEntity session = sessions.get(0);
        assertEquals(TestData.PROJECT_ID, session.getProjectId(), "Project ID should match");
    }

    @Test
    void findChatSessionsByParticipantId_SUCCESS() {
        final List<ChatSessionEntity> sessions = repository.findChatSessionsByParticipantId(TestData.USER_ID_2);
        assertNotNull(sessions, "Sessions list should not be null");
        assertEquals(1, sessions.size(), "There should be exactly one chat session");

        String expectedSessionId = repository.findChatSessionsByProjectId(TestData.PROJECT_ID).get(0).getId();
        String actualResult = sessions.get(0).getId();
        assertEquals(expectedSessionId, actualResult, "Session ID should match");
    }

    @Test
    void createChatSession_SUCCESS() {
        final String projectId = TestData.PROJECT_ID;
        final String taskId = TASK_ID;
        final Map<String, ParticipantRole> participants = new HashMap<>();
        participants.put(TestData.USER_ID, ParticipantRole.INITIATOR);
        participants.put(TestData.USER_ID_2, ParticipantRole.HANDLER);
        participants.put(TestData.USER_ID_3, ParticipantRole.OBSERVER);
        final ChatSessionModel.TaskType taskType = ChatSessionModel.TaskType.TASK;
        final ChatSessionModel.Status status = ChatSessionModel.Status.OPEN;

        final ChatSessionEntity session = repository.createChatSession(projectId, taskId, taskType , participants, status);

        assertNotNull(session, "ChatSessionEntity should not be null");
        assertEquals(projectId, session.getProjectId(), "Project ID should match");
        assertEquals(taskId, session.getTaskId(), "Task ID should match");
        assertEquals("TASK", session.getTaskType().name(), "Task type should match");
        assertEquals("OPEN", session.getStatus().name(), "Status should be OPEN");
        assertEquals(participants, session.getParticipants(), "Participants should match");
    }

    @Test
    void createChatSession_FAILURE() {
        String taskId = TASK_ID;
        ChatSessionModel.TaskType taskType = ChatSessionModel.TaskType.TASK;
        ChatSessionModel.Status status = ChatSessionModel.Status.OPEN;
        Map<String, ParticipantRole> participants = new HashMap<>();
        participants.put(TestData.USER_ID, ParticipantRole.INITIATOR);

        // Null project ID should fail
        assertThrows(Exception.class, () -> {
            repository.createChatSession(null, taskId, taskType, participants, status);
        }, "Expected an exception for null projectId");

        // Null task ID should fail
        assertThrows(Exception.class, () -> {
            repository.createChatSession(TestData.PROJECT_ID, null, taskType, participants, status);
        }, "Expected an exception for null taskId");

        // Null task type should fail
        assertThrows(Exception.class, () -> {
            repository.createChatSession(TestData.PROJECT_ID, taskId, null, participants, status);
        }, "Expected an exception for null taskType");
    }




    @Test
    void updateSessionStatus_SUCCESS() {
        final List<ChatSessionEntity> sessions = repository.findChatSessionsByProjectId(TestData.PROJECT_ID);
        assertNotNull(sessions, "Sessions list should not be null");
        assertEquals(1, sessions.size(), "There should be exactly one chat session");

        final ChatSessionEntity session = sessions.get(0);
        final ChatSessionModel.Status newStatus = ChatSessionModel.Status.CLOSED;

        final ChatSessionEntity updatedSession = repository.updateSessionStatus(session.getId(), newStatus);
        assertNotNull(updatedSession, "Updated ChatSessionEntity should not be null");
        assertEquals(newStatus, updatedSession.getStatus(), "Status should be updated");
    }

    @Test
    void addParticipant_SUCCESS() {
        final List<ChatSessionEntity> sessions = repository.findChatSessionsByProjectId(TestData.PROJECT_ID);
        assertNotNull(sessions, "Sessions list should not be null");
        assertEquals(1, sessions.size(), "There should be exactly one chat session");

        final ChatSessionEntity session = sessions.get(0);
        final String participantId = TestData.USER_ID_4;
        final ParticipantRole role = ParticipantRole.OBSERVER;

        final ChatSessionEntity updatedSession = repository.addParticipant(session.getId(), participantId, role);
        assertNotNull(updatedSession, "Updated ChatSessionEntity should not be null");
        assertEquals(role, updatedSession.getParticipants().get(participantId), "Participant role should be updated");
    }

    @Test
    void addParticipant_FAILURE() {
        final List<ChatSessionEntity> sessions = repository.findChatSessionsByProjectId(TestData.PROJECT_ID);
        assertNotNull(sessions, "Sessions list should not be null");
        assertEquals(1, sessions.size(), "There should be exactly one chat session");

        final ChatSessionEntity session = sessions.get(0);

        final String participantId = TestData.USER_ID_2;
        final ParticipantRole role = ParticipantRole.OBSERVER;
        final String participantId2 = TestData.USER_ID_4;
        final ParticipantRole role2 = ParticipantRole.INITIATOR;
        final String participantId3 = TestData.USER_ID_4;

        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> repository.addParticipant(session.getId(), participantId, role));
        assertEquals("Participant with ID " + participantId + " already exists in session " + session.getId(), exception1.getMessage(), "Exception message should match");

        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> repository.addParticipant(session.getId(), participantId2, role2));
        assertEquals("Only one participant can have the role INITIATOR", exception2.getMessage(), "Exception message should match");

        IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class, () -> repository.addParticipant(session.getId(), participantId3, null));
        assertEquals("Role is required", exception3.getMessage(), "Exception message should match");
    }

    @Test
    void changeParticipantRole_SUCCESS() {
        final List<ChatSessionEntity> sessions = repository.findChatSessionsByProjectId(TestData.PROJECT_ID);
        assertNotNull(sessions, "Sessions list should not be null");
        assertEquals(1, sessions.size(), "There should be exactly one chat session");

        final ChatSessionEntity session = sessions.get(0);
        final String participantId = TestData.USER_ID_3;
        final ParticipantRole newRole = ParticipantRole.OBSERVER;

        final ChatSessionEntity updatedSession = repository.changeParticipantRole(session.getId(), participantId, newRole);
        assertNotNull(updatedSession, "Updated ChatSessionEntity should not be null");
        assertEquals(newRole, updatedSession.getParticipants().get(participantId), "Participant role should be updated");
    }

    @Test
    void changeParticipantRole_FAILURE() {
        final List<ChatSessionEntity> sessions = repository.findChatSessionsByProjectId(TestData.PROJECT_ID);
        assertNotNull(sessions, "Sessions list should not be null");
        assertEquals(1, sessions.size(), "There should be exactly one chat session");

        final ChatSessionEntity session = sessions.get(0);
        final String sessionId = session.getId();

        final ParticipantRole initiatorRole = ParticipantRole.INITIATOR;
        final String existingParticipantId = TestData.USER_ID_3;

        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> repository.changeParticipantRole(sessionId, existingParticipantId, initiatorRole));
        assertEquals("The role INITIATOR can not be changed or assigned to another participant or more than one participant", exception2.getMessage(), "Exception message should match");

        Exception exception3 = assertThrows(IllegalArgumentException.class, () -> repository.changeParticipantRole(sessionId, existingParticipantId, null));
        assertEquals("Role is required", exception3.getMessage(), "Exception message should match");
    }


    @Test
    @Transactional
    void mergeSession_SUCCESS() {

        final List<ChatSessionEntity> sessions = repository.findChatSessionsByProjectId(TestData.PROJECT_ID);
        assertNotNull(sessions, "Sessions list should not be null");
        assertEquals(1, sessions.size(), "There should be exactly one chat session");

        final ChatSessionEntity session = sessions.get(0);
        assertNotNull(session, "ChatSessionEntity should not be null");

        entityManager.detach(session);
        session.setStatus(ChatSessionModel.Status.CLOSED);

        ChatSessionEntity mergedSession = repository.mergeSession(session);
        assertNotNull(mergedSession, "Merged ChatSessionEntity should not be null");
        assertEquals(ChatSessionModel.Status.CLOSED, mergedSession.getStatus(), "Merged session status should be CLOSED");

        entityManager.flush();
        entityManager.clear();

        ChatSessionEntity updatedSession = repository.findChatSessionById(session.getId());
        assertNotNull(updatedSession, "Updated ChatSessionEntity should not be null");
        assertEquals(ChatSessionModel.Status.CLOSED, updatedSession.getStatus(), "Chat session status should be updated to CLOSED");
    }

    @Test
    void deleteChatSession_SUCCESS() {
        final List<ChatSessionEntity> sessions = repository.findChatSessionsByProjectId(TestData.PROJECT_ID);
        assertNotNull(sessions, "Sessions list should not be null");
        assertEquals(1, sessions.size(), "There should be exactly one chat session");

        final ChatSessionEntity session = sessions.get(0);
        final String sessionId = session.getId();

        assertTrue(repository.deleteChatSession(sessionId), "Chat session should be deleted");
        assertTrue(repository.findChatSessionsByProjectId(TestData.PROJECT_ID).isEmpty(), "Chat session should not be found");
    }

    @Test
    void deleteMember_SUCCESS() {
        final List<ChatSessionEntity> sessions = repository.findChatSessionsByProjectId(TestData.PROJECT_ID);
        assertNotNull(sessions, "Sessions list should not be null");
        assertEquals(1, sessions.size(), "There should be exactly one chat session");

        final ChatSessionEntity session = sessions.get(0);
        final String sessionId = session.getId();
        final String participantId = TestData.USER_ID_3;

        ChatSessionEntity updatedSession = repository.deleteMember(sessionId, participantId);
        assertNotNull(updatedSession, "Updated ChatSessionEntity should not be null");
        Map<String, ParticipantRole> participants = updatedSession.getParticipants();
        for (Map.Entry<String, ParticipantRole> entry : participants.entrySet()) {
            assertNotEquals(participantId, entry.getKey(), "Participant should be removed from the session");
        }

    }

    @Test
    @Transactional
    void updateTaskType_SUCCESS() {
        final List<ChatSessionEntity> sessions = repository.findChatSessionsByProjectId(TestData.PROJECT_ID);
        assertNotNull(sessions, "Sessions list should not be null");
        assertEquals(1, sessions.size(), "There should be exactly one chat session");

        final ChatSessionEntity session = sessions.get(0);
        final String sessionId = session.getId();
        final ChatSessionModel.TaskType newTaskType = ChatSessionModel.TaskType.DEFECT;

        ChatSessionEntity updatedSession = repository.updateTaskType(sessionId, newTaskType);
        assertNotNull(updatedSession, "Updated ChatSessionEntity should not be null");
        assertEquals(newTaskType, updatedSession.getTaskType(), "Task type should be updated");
    }

    @Test
    void updateTaskType_FAILURE() {
        final List<ChatSessionEntity> sessions = repository.findChatSessionsByProjectId(TestData.PROJECT_ID);
        assertNotNull(sessions, "Sessions list should not be null");
        assertEquals(1, sessions.size(), "There should be exactly one chat session");

        final ChatSessionEntity session = sessions.get(0);
        final String sessionId = session.getId();

        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> repository.updateTaskType(sessionId, null));
        assertEquals("TaskType is required", exception1.getMessage(), "Exception message should match");

        ChatSessionModel.TaskType existingTaskType = session.getTaskType();
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> repository.updateTaskType(sessionId, existingTaskType));
        assertEquals("TaskType is already set to " + existingTaskType, exception2.getMessage(), "Exception message should match");
    }

}
