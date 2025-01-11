package de.remsfal.service.entity;

import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import de.remsfal.service.entity.dao.CassChatMessageRepository;
import de.remsfal.service.entity.dto.CassChatMessageEntity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class CassChatMessageRepositoryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassChatMessageRepositoryTest.class);

    @Inject
    CassChatMessageRepository chatMessageRepository;

    @Inject
    QuarkusCqlSession cqlSession;

    private static final String ACTIVE_TABLE = "active_chat_messages";
    private static final String ARCHIVED_TABLE = "archived_chat_messages";

    @BeforeEach
    public void setUp() {
        LOGGER.info("Setting up test environment. Creating tables if they do not exist.");

        cqlSession.execute(
                "CREATE TABLE IF NOT EXISTS REMSFAL." + ACTIVE_TABLE + " (" +
                        "chat_session_id UUID, " +
                        "message_id UUID, " +
                        "sender_id UUID, " +
                        "content_type TEXT, " +
                        "content TEXT, " +
                        "url TEXT, " +
                        "created_at TIMESTAMP, " +
                        "PRIMARY KEY (chat_session_id, message_id)) " +
                        "WITH CLUSTERING ORDER BY (message_id ASC);"
        );
        LOGGER.info("Active table created: {}", ACTIVE_TABLE);

        cqlSession.execute(
                "CREATE TABLE IF NOT EXISTS REMSFAL." + ARCHIVED_TABLE + " (" +
                        "chat_session_id UUID, " +
                        "message_id UUID, " +
                        "sender_id UUID, " +
                        "content_type TEXT, " +
                        "content TEXT, " +
                        "url TEXT, " +
                        "created_at TIMESTAMP, " +
                        "PRIMARY KEY (chat_session_id, message_id)) " +
                        "WITH CLUSTERING ORDER BY (message_id ASC);"
        );
        LOGGER.info("Archived table created: {}", ARCHIVED_TABLE);
    }

    @AfterEach
    public void tearDown() {
        LOGGER.info("Cleaning up test data.");

        cqlSession.execute("TRUNCATE REMSFAL." + ACTIVE_TABLE + ";");
        LOGGER.info("Truncated table: {}", ACTIVE_TABLE);

        cqlSession.execute("TRUNCATE REMSFAL." + ARCHIVED_TABLE + ";");
        LOGGER.info("Truncated table: {}", ARCHIVED_TABLE);
    }

    @Test
    public void testSaveAndFindActiveMessage() {
        LOGGER.info("Running test: testSaveAndFindActiveMessage");

        UUID sessionId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        CassChatMessageEntity message = new CassChatMessageEntity();
        message.setChatSessionId(sessionId);
        message.setMessageId(messageId);
        message.setSenderId(senderId);
        message.setContentType("text");
        message.setContent("Hello, Active!");
        message.setCreatedAt(Instant.now());

        LOGGER.info("Saving active message: {}", message);
        chatMessageRepository.saveToActive(message);

        LOGGER.info("Finding active message by ID: sessionId={}, messageId={}", sessionId, messageId);
        Optional<CassChatMessageEntity> result = chatMessageRepository.findActiveById(sessionId, messageId);

        LOGGER.info("Found message: {}", result);
        assertTrue(result.isPresent());
        assertEquals("text", result.get().getContentType());
        assertEquals("Hello, Active!", result.get().getContent());
    }

    @Test
    public void testSaveAndFindArchivedMessage() {
        LOGGER.info("Running test: testSaveAndFindArchivedMessage");

        UUID sessionId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        CassChatMessageEntity message = new CassChatMessageEntity();
        message.setChatSessionId(sessionId);
        message.setMessageId(messageId);
        message.setSenderId(senderId);
        message.setContentType("text");
        message.setContent("Hello, Archived!");
        message.setCreatedAt(Instant.now());

        LOGGER.info("Saving archived message: {}", message);
        chatMessageRepository.saveToArchived(message);

        LOGGER.info("Finding archived message by ID: sessionId={}, messageId={}", sessionId, messageId);
        Optional<CassChatMessageEntity> result = chatMessageRepository.findArchivedById(sessionId, messageId);

        LOGGER.info("Found message: {}", result);
        assertTrue(result.isPresent());
        assertEquals("text", result.get().getContentType());
        assertEquals("Hello, Archived!", result.get().getContent());
    }

    @Test
    public void testUpdateActiveMessage() {
        LOGGER.info("Running test: testUpdateActiveMessage");

        UUID sessionId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        CassChatMessageEntity message = new CassChatMessageEntity();
        message.setChatSessionId(sessionId);
        message.setMessageId(messageId);
        message.setSenderId(senderId);
        message.setContentType("text");
        message.setContent("Hello, Update!");
        message.setCreatedAt(Instant.now());

        LOGGER.info("Saving active message: {}", message);
        chatMessageRepository.saveToActive(message);

        // Update the content
        message.setContent("Updated Active Message");
        LOGGER.info("Updating active message: {}", message);
        chatMessageRepository.updateActive(message);

        LOGGER.info("Finding updated active message by ID: sessionId={}, messageId={}", sessionId, messageId);
        Optional<CassChatMessageEntity> updatedMessage = chatMessageRepository.findActiveById(sessionId, messageId);

        LOGGER.info("Updated message: {}", updatedMessage);
        assertTrue(updatedMessage.isPresent());
        assertEquals("Updated Active Message", updatedMessage.get().getContent());
    }

    @Test
    public void testDeleteActiveMessage() {
        LOGGER.info("Running test: testDeleteActiveMessage");

        UUID sessionId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        CassChatMessageEntity message = new CassChatMessageEntity();
        message.setChatSessionId(sessionId);
        message.setMessageId(messageId);
        message.setSenderId(senderId);
        message.setContentType("text");
        message.setContent("To be deleted");
        message.setCreatedAt(Instant.now());

        LOGGER.info("Saving active message: {}", message);
        chatMessageRepository.saveToActive(message);

        LOGGER.info("Deleting active message: sessionId={}, messageId={}", sessionId, messageId);
        chatMessageRepository.deleteActive(sessionId, messageId);

        LOGGER.info("Finding deleted message by ID: sessionId={}, messageId={}", sessionId, messageId);
        Optional<CassChatMessageEntity> result = chatMessageRepository.findActiveById(sessionId, messageId);

        LOGGER.info("Deleted message result: {}", result);
        assertFalse(result.isPresent());
    }

    @Test
    public void testFindAllActiveMessages() {
        LOGGER.info("Running test: testFindAllActiveMessages");

        UUID sessionId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        CassChatMessageEntity message1 = new CassChatMessageEntity();
        message1.setChatSessionId(sessionId);
        message1.setMessageId(UUID.randomUUID());
        message1.setSenderId(senderId);
        message1.setContentType("text");
        message1.setContent("Message 1");
        message1.setCreatedAt(Instant.now());

        CassChatMessageEntity message2 = new CassChatMessageEntity();
        message2.setChatSessionId(sessionId);
        message2.setMessageId(UUID.randomUUID());
        message2.setSenderId(senderId);
        message2.setContentType("text");
        message2.setContent("Message 2");
        message2.setCreatedAt(Instant.now());

        LOGGER.info("Saving messages: {}, {}", message1, message2);
        chatMessageRepository.saveToActive(message1);
        chatMessageRepository.saveToActive(message2);

        LOGGER.info("Finding all active messages.");
        List<CassChatMessageEntity> messages = chatMessageRepository.findAllActive();

        LOGGER.info("Found active messages: {}", messages);
        assertEquals(2, messages.size());
    }
}
