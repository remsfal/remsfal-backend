package de.remsfal.service.entity;

import de.remsfal.core.model.ProjectMemberModel;
import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import de.remsfal.service.entity.dao.ChatMessageRepository;
import de.remsfal.service.entity.dao.ChatSessionRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.ChatMessageEntity;
import de.remsfal.service.entity.dto.ChatSessionEntity;
import de.remsfal.service.entity.dto.UserEntity;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ChatMessageRepositoryTest extends AbstractTest {

    @Inject
    ChatSessionRepository chatSessionRepository;

    @Inject
    ChatMessageRepository chatMessageRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    EntityManager entityManager;

    static final String TASK_ID = UUID.randomUUID().toString();
    static final String CHAT_SESSION_ID = UUID.randomUUID().toString();
    static final String CHAT_MESSAGE_ID = UUID.randomUUID().toString();
    static final String CLOSED_CHAT_SESSION_ID = UUID.randomUUID().toString();

    @BeforeEach
    @Transactional
    void setupTestData() {
        insertUsers();
        insertProject();
        insertProjectMemberships();
        insertTask();
        insertChatSessions();
        insertChatSessionParticipants();
        insertChatMessage();
    }

    @AfterEach
    @Transactional
    void cleanDB() {
        entityManager.createNativeQuery("DELETE FROM CHAT_MESSAGE").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM CHAT_SESSION_PARTICIPANT").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM CHAT_SESSION").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM TASK").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM PROJECT_MEMBERSHIP").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM PROJECT").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM USER").executeUpdate();
    }

    private void insertUsers() {
        String insertUserSQL = "INSERT INTO USER (ID, TOKEN_ID, EMAIL, FIRST_NAME, LAST_NAME) VALUES (?,?,?,?,?)";
        insertUser(TestData.USER_ID, TestData.USER_TOKEN, TestData.USER_EMAIL, TestData.USER_FIRST_NAME,
                TestData.USER_LAST_NAME, insertUserSQL);
        insertUser(TestData.USER_ID_2, TestData.USER_TOKEN_2, TestData.USER_EMAIL_2, TestData.USER_FIRST_NAME_2,
                TestData.USER_LAST_NAME_2, insertUserSQL);
        insertUser(TestData.USER_ID_3, TestData.USER_TOKEN_3, TestData.USER_EMAIL_3, TestData.USER_FIRST_NAME_3,
                TestData.USER_LAST_NAME_3, insertUserSQL);
        insertUser(TestData.USER_ID_4, TestData.USER_TOKEN_4, TestData.USER_EMAIL_4, TestData.USER_FIRST_NAME_4,
                TestData.USER_LAST_NAME_4, insertUserSQL);
    }

    private void insertUser(String id, String tokenId, String email, String firstName, String lastName, String sql) {
        entityManager.createNativeQuery(sql)
                .setParameter(1, id)
                .setParameter(2, tokenId)
                .setParameter(3, email)
                .setParameter(4, firstName)
                .setParameter(5, lastName)
                .executeUpdate();
    }

    private void insertProject() {
        entityManager.createNativeQuery("INSERT INTO PROJECT (ID, TITLE) VALUES (?,?)")
                .setParameter(1, TestData.PROJECT_ID)
                .setParameter(2, TestData.PROJECT_TITLE)
                .executeUpdate();
    }

    private void insertProjectMemberships() {
        String insertMembershipSQL = "INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, USER_ROLE) VALUES (?,?,?)";
        insertMembership(TestData.USER_ID, ProjectMemberModel.UserRole.MANAGER.name(), insertMembershipSQL);
        insertMembership(TestData.USER_ID_2, ProjectMemberModel.UserRole.LESSOR.name(), insertMembershipSQL);
        insertMembership(TestData.USER_ID_3, ProjectMemberModel.UserRole.CARETAKER.name(), insertMembershipSQL);
        insertMembership(TestData.USER_ID_4, ProjectMemberModel.UserRole.CARETAKER.name(), insertMembershipSQL);
    }

    private void insertMembership(String userId, String role, String sql) {
        entityManager.createNativeQuery(sql)
                .setParameter(1, TestData.PROJECT_ID)
                .setParameter(2, userId)
                .setParameter(3, role)
                .executeUpdate();
    }

    private void insertTask() {
        entityManager.createNativeQuery("INSERT INTO TASK (ID, TYPE, PROJECT_ID, TITLE, STATUS, OWNER_ID, CREATED_BY)" +
                        " VALUES (?,?,?,?,?,?,?)")
                .setParameter(1, TASK_ID)
                .setParameter(2, "TASK")
                .setParameter(3, TestData.PROJECT_ID)
                .setParameter(4, TestData.TASK_TITLE_1)
                .setParameter(5, "OPEN")
                .setParameter(6, TestData.USER_ID)
                .setParameter(7, TestData.USER_ID)
                .executeUpdate();
    }

    private void insertChatSessions() {
        entityManager.createNativeQuery("INSERT INTO CHAT_SESSION (ID, PROJECT_ID, TASK_ID, TASK_TYPE, STATUS) " +
                        "VALUES (?,?,?,?,?)")
                .setParameter(1, CHAT_SESSION_ID)
                .setParameter(2, TestData.PROJECT_ID)
                .setParameter(3, TASK_ID)
                .setParameter(4, "TASK")
                .setParameter(5, "OPEN")
                .executeUpdate();

        entityManager.createNativeQuery("INSERT INTO CHAT_SESSION (ID, PROJECT_ID, TASK_ID, TASK_TYPE, STATUS) " +
                        "VALUES (?,?,?,?,?)")
                .setParameter(1, CLOSED_CHAT_SESSION_ID)
                .setParameter(2, TestData.PROJECT_ID)
                .setParameter(3, TASK_ID)
                .setParameter(4, "TASK")
                .setParameter(5, "CLOSED")
                .executeUpdate();
    }

    private void insertChatSessionParticipants() {
        String insertParticipantSQL = "INSERT INTO CHAT_SESSION_PARTICIPANT (CHAT_SESSION_ID, PARTICIPANT_ID, ROLE) " +
                "VALUES (?,?,?)";
        insertParticipant(TestData.USER_ID_2, "INITIATOR", insertParticipantSQL);
        insertParticipant(TestData.USER_ID_3, "HANDLER", insertParticipantSQL);
        insertParticipant(TestData.USER_ID, "OBSERVER", insertParticipantSQL);
    }

    private void insertParticipant(String participantId, String role, String sql) {
        entityManager.createNativeQuery(sql)
                .setParameter(1, ChatMessageRepositoryTest.CHAT_SESSION_ID)
                .setParameter(2, participantId)
                .setParameter(3, role)
                .executeUpdate();
    }

    private void insertChatMessage() {
        entityManager.createNativeQuery("INSERT INTO CHAT_MESSAGE" +
                        " (ID, CHAT_SESSION_ID, SENDER_ID, CONTENT_TYPE, CONTENT) VALUES (?,?,?,?,?)")
                .setParameter(1, CHAT_MESSAGE_ID)
                .setParameter(2, CHAT_SESSION_ID)
                .setParameter(3, TestData.USER_ID_2)
                .setParameter(4, "TEXT")
                .setParameter(5, "This is a test message")
                .executeUpdate();
    }

    @Test
    void hashCode_SUCCESS() {
        ChatMessageEntity chatMessage = chatMessageRepository.findChatMessageById(CHAT_MESSAGE_ID);
        assertNotNull(chatMessage, "Chat message should not be null");
        assertTrue(chatMessage.hashCode() != 0, "Hash code should not be 0");
    }

    @Test
    void equals_SUCCESS() {
        ChatMessageEntity chatMessage1 = chatMessageRepository.findChatMessageById(CHAT_MESSAGE_ID);
        ChatMessageEntity chatMessage2 = chatMessageRepository.findChatMessageById(CHAT_MESSAGE_ID);
        assertNotNull(chatMessage1, "Chat message 1 should not be null");
        assertNotNull(chatMessage2, "Chat message 2 should not be null");
        assertEquals(chatMessage1, chatMessage2, "Chat messages should be equal");
    }

    @Test
    void findChatMessageById_SUCCESS() {
        ChatMessageEntity chatMessage = chatMessageRepository.findChatMessageById(CHAT_MESSAGE_ID);
        assertEquals(CHAT_MESSAGE_ID, chatMessage.getId(), "Chat message ID should match");
    }

    @Test
    void findChatMessageById_FAILURE() {
        String randomId = UUID.randomUUID().toString();
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () ->
                chatMessageRepository.findChatMessageById(randomId));
        assertEquals("ChatMessage with ID " + randomId + " not found", exception.getMessage(),
                "Exception message should match");
    }

    @Test
    @Transactional
    void sendTextChatMessage_SUCCESS() {
        chatMessageRepository.sendChatMessage(CHAT_SESSION_ID, TestData.USER_ID_2, ChatMessageEntity.ContentType.TEXT,
                "This is a second test message");
        ChatSessionEntity chatSession = chatSessionRepository.findChatSessionById(CHAT_SESSION_ID);
        assertEquals(2, chatSession.getMessages().size(), "Chat session should have 2 messages");
        assertEquals("This is a second test message", chatSession.getMessages().get(1).getContent(),
                "Content of the second message should match");
    }


    @Test
    @Transactional
    void sendFileMetadataChatMessage_SUCCESS() {
        chatMessageRepository.sendChatMessage(CHAT_SESSION_ID, TestData.USER_ID_2, ChatMessageEntity.ContentType.FILE,
                "https://example.com/image.jpg");
        ChatSessionEntity chatSession = chatSessionRepository.findChatSessionById(CHAT_SESSION_ID);
        assertEquals(2, chatSession.getMessages().size(), "Chat session should have 2 messages");
        assertEquals("https://example.com/image.jpg", chatSession.getMessages().get(1).getUrl(),
                "Image URL of the second message should match");
    }

    @Test
    void sendTextChatMessage_FAILURE() {
        String validSessionId = CHAT_SESSION_ID;
        String validSenderId = TestData.USER_ID_2;
        String validMessageContent = "Test message content";
        ChatMessageEntity.ContentType validContentType = ChatMessageEntity.ContentType.TEXT;

        // Non-existent session ID should fail
        assertThrows(Exception.class, () ->
                chatMessageRepository.sendChatMessage(UUID.randomUUID().toString(), validSenderId, validContentType,
                        validMessageContent)
        );

        // Closed session should fail
        assertThrows(Exception.class, () ->
                chatMessageRepository.sendChatMessage(CLOSED_CHAT_SESSION_ID, validSenderId, validContentType,
                        validMessageContent)
        );

        // Null sender ID should fail
        assertThrows(Exception.class, () ->
                chatMessageRepository.sendChatMessage(validSessionId, null, validContentType,
                        validMessageContent)
        );

        // Null content type should fail
        assertThrows(Exception.class, () ->
                chatMessageRepository.sendChatMessage(validSessionId, validSenderId, null,
                        validMessageContent)
        );
    }


    @Test
    @Transactional
    void deleteChatMessage_SUCCESS() {
        chatMessageRepository.deleteChatMessage(CHAT_MESSAGE_ID);
        ChatSessionEntity chatSession = chatSessionRepository.findChatSessionById(CHAT_SESSION_ID);
        assertEquals(0, chatSession.getMessages().size(), "Chat session should have 0 messages");
    }

    @Test
    void deleteChatMessage_FAILURE() {
        String randomId = UUID.randomUUID().toString();
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, ()
                -> chatMessageRepository.deleteChatMessage(randomId));
        assertEquals("ChatMessage with ID " + randomId + " not found", exception.getMessage(),
                "Exception message should match");
    }

    @Test
    @Transactional
    void updateTextChatMessage_SUCCESS() {
        chatMessageRepository.updateTextChatMessage(CHAT_MESSAGE_ID, "This is an updated message");
        ChatSessionEntity chatSession = chatSessionRepository.findChatSessionById(CHAT_SESSION_ID);
        assertEquals("This is an updated message", chatSession.getMessages().get(0).getContent(),
                "Content of the updated message should match");
    }


    @Test
    void updateTextChatMessage_FAILURE() {
        String randomId = UUID.randomUUID().toString();
        String validContent = "This is an updated message";
        String blankContent = "   ";

        Exception exception1 = assertThrows(NoSuchElementException.class, () ->
                chatMessageRepository.updateTextChatMessage(randomId, validContent));
        assertEquals("ChatMessage with ID " + randomId + " not found", exception1.getMessage(),
                "Exception message should match for non-existent message ID");

        Exception exception2 = assertThrows(IllegalArgumentException.class, () ->
                chatMessageRepository.updateTextChatMessage(CHAT_MESSAGE_ID, null));
        assertEquals("Content cannot be null or empty", exception2.getMessage(),
                "Exception message should match for null content");

        Exception exception3 = assertThrows(IllegalArgumentException.class, () ->
                chatMessageRepository.updateTextChatMessage(CHAT_MESSAGE_ID, blankContent));
        assertEquals("Content cannot be null or empty", exception3.getMessage(),
                "Exception message should match for blank content");

        chatMessageRepository.sendChatMessage(CHAT_SESSION_ID, TestData.USER_ID_2, ChatMessageEntity.ContentType.FILE,
                "This is an image message");
        String imageMessageId = chatSessionRepository.findChatSessionById(CHAT_SESSION_ID).getMessages().get(1).getId();
        Exception exception4 = assertThrows(IllegalArgumentException.class, () ->
                chatMessageRepository.updateTextChatMessage(imageMessageId, validContent));
        assertEquals("Cannot update non-text message with updateTextChatMessage() method",
                exception4.getMessage(), "Exception message should match for non-text message");
    }

    @Test
    @Transactional
    void updateImageURL_SUCCESS() {

        UserEntity sender = userRepository.findByIdOptional(TestData.USER_ID_2).orElseThrow(() ->
                new NotFoundException("User does not exist"));

        ChatMessageEntity chatMessage = new ChatMessageEntity();
        chatMessage.setId(UUID.randomUUID().toString());
        chatMessage.setChatSession(chatSessionRepository.findChatSessionById(CHAT_SESSION_ID));
        chatMessage.setSender(sender);
        chatMessage.setSenderId(TestData.USER_ID_2);
        chatMessage.setContentType(ChatMessageEntity.ContentType.FILE);
        chatMessage.setUrl("https://example.com/image.jpg");
        chatSessionRepository.findChatSessionById(CHAT_SESSION_ID).getMessages().add(chatMessage);
        chatMessageRepository.persist(chatMessage);

        String randomId = UUID.randomUUID().toString();
        String invalidImageUrl = "   ";
        String validImageUrl = "https://example.com/updated-image.jpg";

        // Non-existent message ID
        NoSuchElementException exception1 = assertThrows(NoSuchElementException.class, () ->
                chatMessageRepository.updateImageURL(randomId, validImageUrl)
        );
        assertEquals("ChatMessage with ID " + randomId + " not found", exception1.getMessage());

        // Invalid image URL
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () ->
                chatMessageRepository.updateImageURL(chatMessage.getId(), invalidImageUrl)
        );
        assertEquals("Image URL cannot be null or empty", exception2.getMessage());

        IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class, () ->
                chatMessageRepository.updateImageURL(chatMessage.getId(), null)
        );
        assertEquals("Image URL cannot be null or empty", exception3.getMessage());

        // Attempt to update non-image message
        IllegalArgumentException exception4 = assertThrows(IllegalArgumentException.class, () ->
                chatMessageRepository.updateImageURL(CHAT_MESSAGE_ID, validImageUrl)
        );
        assertEquals("Cannot update non-image message with updateImageURL() method", exception4.getMessage());
    }

    @Test
    @Transactional
    void updateImageURL_FAILURE() {

        UserEntity sender = userRepository.findByIdOptional(TestData.USER_ID_2).orElseThrow(() ->
                new NotFoundException("User does not exist"));

        ChatMessageEntity chatMessage = new ChatMessageEntity();
        chatMessage.setId(UUID.randomUUID().toString());
        chatMessage.setChatSession(chatSessionRepository.findChatSessionById(CHAT_SESSION_ID));
        chatMessage.setSender(sender);
        chatMessage.setSenderId(TestData.USER_ID_2);
        chatMessage.setContentType(ChatMessageEntity.ContentType.FILE);
        chatMessage.setUrl("https://example.com/image.jpg");
        chatSessionRepository.findChatSessionById(CHAT_SESSION_ID).getMessages().add(chatMessage);
        chatMessageRepository.persist(chatMessage);

        String randomId = UUID.randomUUID().toString();
        String invalidImageUrl = "   ";
        String validImageUrl = "https://example.com/updated-image.jpg";

        Exception exception1 = assertThrows(NoSuchElementException.class, ()
                -> chatMessageRepository.updateImageURL(randomId, validImageUrl));
        assertEquals("ChatMessage with ID " + randomId + " not found", exception1.getMessage(),
                "Exception message should match for non-existent message ID");

        Exception exception2 = assertThrows(IllegalArgumentException.class, ()
                -> chatMessageRepository.updateImageURL(chatMessage.getId(), invalidImageUrl));
        assertEquals("Image URL cannot be null or empty", exception2.getMessage(),
                "Exception message should match for blank image URL");

        Exception exception3 = assertThrows(IllegalArgumentException.class, ()
                -> chatMessageRepository.updateImageURL(chatMessage.getId(), null));
        assertEquals("Image URL cannot be null or empty", exception3.getMessage(),
                "Exception message should match for null image URL");

        Exception exception4 = assertThrows(IllegalArgumentException.class, () ->
                chatMessageRepository.updateImageURL(CHAT_MESSAGE_ID, validImageUrl));
        assertEquals("Cannot update non-image message with updateImageURL() method", exception4.getMessage(),
                "Exception message should match for non-image message");

    }





}
