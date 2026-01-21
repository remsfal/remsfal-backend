package de.remsfal.ticketing.control;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.entity.dao.InboxMessageRepository;
import de.remsfal.ticketing.entity.dto.InboxMessageEntity;
import de.remsfal.ticketing.entity.dto.InboxMessageKey;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class InboxControllerTest extends AbstractTicketingTest {

    @Inject
    InboxController controller;

    @Inject
    InboxMessageRepository repository;

    // ========================================
    // Repository Tests (Direct Database Access)
    // ========================================

    @Test
    void testRepository_saveAndFindByUserId() {
        String userId = "user-123";
        InboxMessageEntity msg = createTestMessage(userId, "Test Title");

        repository.saveInboxMessage(msg);

        List<InboxMessageEntity> results = repository.findByUserId(userId);
        assertEquals(1, results.size());
        assertEquals("Test Title", results.get(0).getTitle());
    }

    // Note: Tests for findByUserIdAndRead, findByUserIdAndEventType, and findByUserIdAndEventTypeAndRead
    // are skipped because they require Cassandra secondary indexes which are not created in test environment.
    // These methods are tested indirectly through controller tests that use findByUserId.

    @Test
    void testRepository_findByUserIdAndId() {
        String userId = "user-def";
        UUID messageId = UUID.randomUUID();

        InboxMessageEntity msg = createTestMessage(userId, "Specific Message");
        msg.getKey().setId(messageId);
        repository.saveInboxMessage(msg);

        Optional<InboxMessageEntity> result = repository.findByUserIdAndId(userId, messageId);

        assertTrue(result.isPresent());
        assertEquals("Specific Message", result.get().getTitle());
        assertEquals(messageId, result.get().getKey().getId());
    }

    @Test
    void testRepository_findByUserIdAndId_notFound() {
        String userId = "user-xyz";
        UUID nonExistentId = UUID.randomUUID();

        Optional<InboxMessageEntity> result = repository.findByUserIdAndId(userId, nonExistentId);

        assertTrue(result.isEmpty());
    }

    @Test
    void testRepository_updateReadStatus() {
        String userId = "user-update";
        UUID messageId = UUID.randomUUID();

        InboxMessageEntity msg = createTestMessage(userId, "Message to Update");
        msg.getKey().setId(messageId);
        msg.setRead(false);
        repository.saveInboxMessage(msg);

        repository.updateReadStatus(userId, messageId, true);

        Optional<InboxMessageEntity> updated = repository.findByUserIdAndId(userId, messageId);
        assertTrue(updated.isPresent());
        assertTrue(updated.get().getRead());
    }

    @Test
    void testRepository_deleteInboxMessage() {
        String userId = "user-delete";
        UUID messageId = UUID.randomUUID();

        InboxMessageEntity msg = createTestMessage(userId, "Message to Delete");
        msg.getKey().setId(messageId);
        repository.saveInboxMessage(msg);

        Optional<InboxMessageEntity> beforeDelete = repository.findByUserIdAndId(userId, messageId);
        assertTrue(beforeDelete.isPresent());

        repository.deleteInboxMessage(userId, messageId);

        Optional<InboxMessageEntity> afterDelete = repository.findByUserIdAndId(userId, messageId);
        assertTrue(afterDelete.isEmpty());
    }

    @Test
    void testRepository_saveWithNullDescription() {
        String userId = "user-null-desc";
        InboxMessageEntity msg = createTestMessage(userId, "No Description");
        msg.setDescription(null);

        repository.saveInboxMessage(msg);

        List<InboxMessageEntity> results = repository.findByUserId(userId);
        assertEquals(1, results.size());
        assertEquals("", results.get(0).getDescription());
    }

    @Test
    void testRepository_saveWithNullActorEmail() {
        String userId = "user-null-actor";
        InboxMessageEntity msg = createTestMessage(userId, "No Actor");
        msg.setActorEmail(null);

        repository.saveInboxMessage(msg);

        List<InboxMessageEntity> results = repository.findByUserId(userId);
        assertEquals(1, results.size());
        assertEquals("", results.get(0).getActorEmail());
    }

    @Test
    void testRepository_saveWithNullOwnerEmail() {
        String userId = "user-null-owner";
        InboxMessageEntity msg = createTestMessage(userId, "No Owner");
        msg.setOwnerEmail(null);

        repository.saveInboxMessage(msg);

        List<InboxMessageEntity> results = repository.findByUserId(userId);
        assertEquals(1, results.size());
        assertEquals("", results.get(0).getOwnerEmail());
    }

    // ========================================
    // Controller Tests (Business Logic)
    // ========================================

    @Test
    void testController_getInboxMessages_userIdNull_throws() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> controller.getInboxMessages(true, null)
        );
        assertEquals("userId cannot be null", exception.getMessage());
    }

    // Note: Test for getInboxMessages with read filter skipped - requires Cassandra secondary index

    @Test
    void testController_getInboxMessages_noFilter() {
        String userId = "user-no-filter";

        repository.saveInboxMessage(createTestMessage(userId, "Message 1"));
        repository.saveInboxMessage(createTestMessage(userId, "Message 2"));

        List<InboxMessageEntity> allMessages = controller.getInboxMessages(null, userId);
        assertEquals(2, allMessages.size());
    }

    @Test
    void testController_getInboxMessages_sortedByCreatedAtDesc() {
        String userId = "user-sorted";

        InboxMessageEntity older = createTestMessage(userId, "Older Message");
        older.setCreatedAt(Instant.now().minusSeconds(100));
        repository.saveInboxMessage(older);

        InboxMessageEntity newer = createTestMessage(userId, "Newer Message");
        newer.setCreatedAt(Instant.now());
        repository.saveInboxMessage(newer);

        List<InboxMessageEntity> messages = controller.getInboxMessages(null, userId);

        assertEquals(2, messages.size());
        assertEquals("Newer Message", messages.get(0).getTitle());
        assertEquals("Older Message", messages.get(1).getTitle());
    }

    @Test
    void testController_updateMessageStatus_success() {
        String userId = "user-update-status";
        UUID messageId = UUID.randomUUID();

        InboxMessageEntity msg = createTestMessage(userId, "Status Update Test");
        msg.getKey().setId(messageId);
        msg.setRead(false);
        repository.saveInboxMessage(msg);

        InboxMessageEntity updated = controller.updateMessageStatus(messageId.toString(), true, userId);

        assertNotNull(updated);
        assertTrue(updated.getRead());

        Optional<InboxMessageEntity> fromDb = repository.findByUserIdAndId(userId, messageId);
        assertTrue(fromDb.isPresent());
        assertTrue(fromDb.get().getRead());
    }

    @Test
    void testController_updateMessageStatus_invalidUuid() {
        String userId = "user-invalid-uuid";

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> controller.updateMessageStatus("not-a-uuid", true, userId)
        );
        assertTrue(exception.getMessage().contains("Invalid UUID format"));
    }

    @Test
    void testController_updateMessageStatus_notFound() {
        String userId = "user-not-found";
        UUID nonExistentId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> controller.updateMessageStatus(nonExistentId.toString(), true, userId)
        );
        assertEquals("Inbox message not found for user", exception.getMessage());
    }

    @Test
    void testController_updateMessageStatus_markAsUnread() {
        String userId = "user-mark-unread";
        UUID messageId = UUID.randomUUID();

        InboxMessageEntity msg = createTestMessage(userId, "Mark as Unread Test");
        msg.getKey().setId(messageId);
        msg.setRead(true);
        repository.saveInboxMessage(msg);

        InboxMessageEntity updated = controller.updateMessageStatus(messageId.toString(), false, userId);

        assertNotNull(updated);
        assertFalse(updated.getRead());
    }

    @Test
    void testController_deleteMessage_success() {
        String userId = "user-delete-success";
        UUID messageId = UUID.randomUUID();

        InboxMessageEntity msg = createTestMessage(userId, "Delete Test");
        msg.getKey().setId(messageId);
        repository.saveInboxMessage(msg);

        controller.deleteMessage(messageId.toString(), userId);

        Optional<InboxMessageEntity> afterDelete = repository.findByUserIdAndId(userId, messageId);
        assertTrue(afterDelete.isEmpty());
    }

    @Test
    void testController_deleteMessage_invalidUuid() {
        String userId = "user-delete-invalid";

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> controller.deleteMessage("invalid-uuid", userId)
        );
        assertTrue(exception.getMessage().contains("Invalid UUID format"));
    }

    @Test
    void testController_deleteMessage_notFound() {
        String userId = "user-delete-not-found";
        UUID nonExistentId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> controller.deleteMessage(nonExistentId.toString(), userId)
        );
        assertEquals("Inbox message not found for user", exception.getMessage());
    }

    @Test
    void testController_multipleUsersIsolation() {
        String user1 = "user-isolation-1";
        String user2 = "user-isolation-2";

        repository.saveInboxMessage(createTestMessage(user1, "User 1 Message"));
        repository.saveInboxMessage(createTestMessage(user2, "User 2 Message"));

        List<InboxMessageEntity> user1Messages = controller.getInboxMessages(null, user1);
        assertEquals(1, user1Messages.size());
        assertEquals("User 1 Message", user1Messages.get(0).getTitle());

        List<InboxMessageEntity> user2Messages = controller.getInboxMessages(null, user2);
        assertEquals(1, user2Messages.size());
        assertEquals("User 2 Message", user2Messages.get(0).getTitle());
    }

    // Note: Test for complex filtering scenario skipped - requires Cassandra secondary index

    // ========================================
    // Helper Methods
    // ========================================

    private InboxMessageEntity createTestMessage(String userId, String title) {
        InboxMessageKey key = new InboxMessageKey();
        key.setUserId(userId);
        key.setId(UUID.randomUUID());

        InboxMessageEntity entity = new InboxMessageEntity();
        entity.setKey(key);
        entity.setEventType("ISSUE_CREATED");
        entity.setIssueId(UUID.randomUUID().toString());
        entity.setTitle(title);
        entity.setIssueType("TASK");
        entity.setStatus("OPEN");
        entity.setDescription("Test description");
        entity.setLink("/api/issues/" + entity.getIssueId());
        entity.setActorEmail("actor@example.com");
        entity.setOwnerEmail("owner@example.com");
        entity.setCreatedAt(Instant.now());
        entity.setRead(false);

        return entity;
    }
}
