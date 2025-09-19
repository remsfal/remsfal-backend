package de.remsfal.chat.entity;

import com.datastax.oss.driver.api.core.CqlSession;

import com.datastax.oss.quarkus.test.CassandraTestResource;
import de.remsfal.chat.AbstractTicketingTest;
import de.remsfal.chat.entity.dao.ChatMessageRepository;
import de.remsfal.chat.entity.dao.ChatMessageRepository.ContentType;
import de.remsfal.chat.entity.dao.ChatSessionRepository.ParticipantRole;
import de.remsfal.chat.entity.dto.ChatMessageEntity;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
public class ChatMessageRepositoryTest extends AbstractTicketingTest {

    static final UUID PROJECT_ID = UUID.randomUUID();
    static final UUID TASK_ID = UUID.randomUUID();
    static final UUID USER_ID_1 = UUID.randomUUID();
    static final UUID USER_ID_2 = UUID.randomUUID();
    static final UUID SESSION_ID = UUID.randomUUID();
    static final String EXAMPLE_URL = "Example.url";
    static final UUID MESSAGE_ID_1 = UUID.randomUUID();
    static final String MESSAGE_CONTENT_1 = "Hi, i am user 1";
    static final UUID MESSAGE_ID_2 = UUID.randomUUID();
    static final String MESSAGE_CONTENT_2 = "Hello, I AM USER 2";
    static final UUID MESSAGE_ID_3 = UUID.randomUUID();
    static final String MESSAGE_CONTENT_3 = "How are you user 2?";

    @Inject
    ChatMessageRepository chatMessageRepository;

    @Inject
    CqlSession cqlSession;

    @BeforeEach
    void setUp() {
        logger.info("Setting up test data");
        String insertSessionCql = "INSERT INTO remsfal.chat_sessions " +
                "(project_id, task_id, session_id, created_at, participants) " +
                "VALUES (?, ?, ?, ?, ?)";
        cqlSession.execute(insertSessionCql,
                PROJECT_ID, TASK_ID, SESSION_ID,
                Instant.now(),
                Map.of(USER_ID_1, ParticipantRole.INITIATOR.name(), USER_ID_2, ParticipantRole.HANDLER.name()));
        logger.info("Test session created: " + SESSION_ID);
        String insertMessageCql =
                "INSERT INTO remsfal.chat_messages " +
                        "(session_id, message_id, sender_id, content_type, content, url, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
        logger.info("Inserting messages");
        cqlSession.execute(insertMessageCql,
                SESSION_ID, MESSAGE_ID_1, USER_ID_1, ContentType.TEXT.name(),
                MESSAGE_CONTENT_1, null, Instant.now());
        logger.info("Message inserted " + MESSAGE_ID_1);
        cqlSession.execute(insertMessageCql,
                SESSION_ID, MESSAGE_ID_2, USER_ID_2, ContentType.TEXT.name(), MESSAGE_CONTENT_2,
                null, Instant.now());
        logger.info("Message inserted " + MESSAGE_ID_2);
        cqlSession.execute(insertMessageCql,
                SESSION_ID, MESSAGE_ID_3, USER_ID_1, ContentType.TEXT.name(), MESSAGE_CONTENT_3,
                null, Instant.now());
        logger.info("Message inserted " + MESSAGE_ID_3);
        String insertMessageCql2 = "INSERT INTO remsfal.chat_messages " +
                "(session_id, message_id, sender_id, content_type, content, url, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        cqlSession.execute(insertMessageCql2, SESSION_ID, UUID.randomUUID(),
                USER_ID_1, ContentType.FILE.name(), null, EXAMPLE_URL, Instant.now());
        logger.info("Message inserted for user 1 with file content " + EXAMPLE_URL);
        logger.info("Test data setup complete");
    }

    @Test
    void findChatMessageById_SUCCESS() {
        String sessionId = SESSION_ID.toString();
        UUID messageId = UUID.randomUUID();
        String insertMessageCql = "INSERT INTO remsfal.chat_messages " +
                "(session_id, message_id, sender_id, content_type, content, url, created_at) " +
                "VALUES (?, ?, ?, ?, 'Test message content', null, toTimestamp(now()))";
        cqlSession.execute(insertMessageCql, SESSION_ID, messageId, USER_ID_1, ContentType.TEXT.name());
        ChatMessageEntity result = chatMessageRepository
                .findMessageById(sessionId, messageId.toString()).get();
        logger.info("Entity: " + result);
        assertNotNull(result, "Chat message should be found");
        assertEquals(messageId, result.getMessageId(), "Message ID should match");
        assertEquals("Test message content", result.getContent(), "Message content should match");
    }

    @Test
    void sendCassChatMessage_SUCCESS() {
        String sessionId = SESSION_ID.toString();
        String userId = USER_ID_1.toString();
        String contentType = ContentType.TEXT.name();
        String content = "Test message content";
        ChatMessageEntity result = chatMessageRepository
                .sendMessage(sessionId, userId, contentType, content);
        assertNotNull(result, "Chat message should be sent");
        assertEquals(UUID.fromString(sessionId), result.getSessionId(), "Session ID should match");
        assertEquals(UUID.fromString(userId), result.getSenderId(), "Sender ID should match");
        assertEquals(contentType, result.getContentType(), "Content type should match");
        assertEquals(content, result.getContent(), "Message content should match");
    }

    @Test
    void deleteMessageById_SUCCESS() {
        String sessionId = SESSION_ID.toString();
        UUID messageId = UUID.randomUUID();
        String insertMessageCql = "INSERT INTO remsfal.chat_messages " +
                "(session_id, message_id, sender_id, content_type, content, url, created_at) " +
                "VALUES (?, ?, ?, ?, 'Message to be deleted', null, toTimestamp(now()))";

        cqlSession.execute(insertMessageCql, SESSION_ID, messageId, USER_ID_1, ContentType.TEXT.name());

        chatMessageRepository.deleteChatMessage(sessionId, messageId.toString());
    }


    @Test
    void updateTextChatMessage_SUCCESS() {
        String sessionId = SESSION_ID.toString();
        UUID messageId = UUID.randomUUID();
        String newContent = "Updated text content";
        String insertMessageCql = "INSERT INTO remsfal.chat_messages " +
                "(session_id, message_id, sender_id, content_type, content, url, created_at) " +
                "VALUES (?, ?, ?, ?, 'Original text content', null, toTimestamp(now()))";
        cqlSession.execute(insertMessageCql, SESSION_ID, messageId, USER_ID_1, ContentType.TEXT.name()
        );

        chatMessageRepository.updateTextChatMessage(sessionId, messageId.toString(), newContent);
        ChatMessageEntity updatedMessage = chatMessageRepository.findMessageById(sessionId, messageId.toString()).get();

        assertEquals(newContent, updatedMessage.getContent(), "Message content should be updated");
    }

    @Test
    void updateFileUrl_SUCCESS() {
        String sessionId = SESSION_ID.toString();
        UUID messageId = UUID.randomUUID();
        String newUrl = "Updated.url";
        String insertMessageCql = "INSERT INTO remsfal.chat_messages " +
                "(session_id, message_id, sender_id, content_type, content, url, created_at) " +
                "VALUES (?, ?, ?, ?, null, 'Original.url', toTimestamp(now()))";
        cqlSession.execute(insertMessageCql, SESSION_ID, messageId, USER_ID_1, ContentType.FILE.name());

        chatMessageRepository.updateFileUrl(sessionId, messageId.toString(), newUrl);
        ChatMessageEntity updatedMessage = chatMessageRepository.findMessageById(sessionId, messageId.toString()).get();

        assertEquals(newUrl, updatedMessage.getUrl(), "Message URL should be updated");
    }

    @Test
    void exportChatLogsAsJsonString_SUCCESS() {
        logger.info("Testing exportChatLogsAsJsonString");
        String exportedJson = chatMessageRepository.getChatLogsAsJsonString(PROJECT_ID, TASK_ID, SESSION_ID);
        logger.info("Exported Chat Logs JSON: " + exportedJson);
        assertTrue(exportedJson.contains(PROJECT_ID.toString()), "JSON should contain the project ID");
        assertTrue(exportedJson.contains(TASK_ID.toString()), "JSON should contain the task ID");
        assertTrue(exportedJson.contains(SESSION_ID.toString()), "JSON should contain the session ID");
        assertTrue(exportedJson.contains(MESSAGE_CONTENT_1), "JSON should contain the first message content");
        assertTrue(exportedJson.contains(USER_ID_1.toString()), "JSON should contain the first user's ID");
        assertTrue(exportedJson.contains(ContentType.TEXT.name()), "JSON should contain the message type");
    }


}
