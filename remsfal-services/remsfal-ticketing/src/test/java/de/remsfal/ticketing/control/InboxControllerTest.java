package de.remsfal.ticketing.control;

import de.remsfal.ticketing.entity.dao.InboxMessageRepository;
import de.remsfal.ticketing.entity.dto.InboxMessageEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class InboxControllerTest {

    InboxController controller;
    InboxMessageRepository repository;

    @BeforeEach
    void setup() {
        controller = new InboxController();
        repository = mock(InboxMessageRepository.class);

        controller.repository = repository;
    }

    @Test
    void testGetInboxMessages_userIdNull_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.getInboxMessages(null, null, null)
        );
    }

    @Test
    void testGetInboxMessages_typeAndRead() {
        InboxMessageEntity msg = new InboxMessageEntity();
        msg.setReceivedAt(Instant.now());

        when(repository.findByUserIdAndTypeAndRead("u1", "INFO", true))
                .thenReturn(List.of(msg));

        var result = controller.getInboxMessages("INFO", true, "u1");

        verify(repository).findByUserIdAndTypeAndRead("u1", "INFO", true);
        assertEquals(1, result.size());
    }

    @Test
    void testGetInboxMessages_typeOnly() {
        InboxMessageEntity msg = new InboxMessageEntity();
        msg.setReceivedAt(Instant.now());

        when(repository.findByUserIdAndType("u1", "INFO"))
                .thenReturn(List.of(msg));

        controller.getInboxMessages("INFO", null, "u1");

        verify(repository).findByUserIdAndType("u1", "INFO");
    }

    @Test
    void testGetInboxMessages_readOnly() {
        InboxMessageEntity msg = new InboxMessageEntity();
        msg.setReceivedAt(Instant.now());

        when(repository.findByUserIdAndRead("u1", true))
                .thenReturn(List.of(msg));

        controller.getInboxMessages(null, true, "u1");

        verify(repository).findByUserIdAndRead("u1", true);
    }

    @Test
    void testGetInboxMessages_noFilters() {
        InboxMessageEntity msg = new InboxMessageEntity();
        msg.setReceivedAt(Instant.now());

        when(repository.findByUserId("u1"))
                .thenReturn(List.of(msg));

        controller.getInboxMessages(null, null, "u1");

        verify(repository).findByUserId("u1");
    }

    @Test
    void testGetInboxMessages_sortedByReceivedAt() {
        InboxMessageEntity oldMsg = new InboxMessageEntity();
        oldMsg.setReceivedAt(Instant.now().minusSeconds(100));

        InboxMessageEntity newMsg = new InboxMessageEntity();
        newMsg.setReceivedAt(Instant.now());

        when(repository.findByUserId("u1"))
                .thenReturn(List.of(oldMsg, newMsg));

        var result = controller.getInboxMessages(null, null, "u1");

        assertEquals(newMsg, result.get(0));
        assertEquals(oldMsg, result.get(1));
    }

    @Test
    void testUpdateMessageStatus_notFound() {
        UUID id = UUID.randomUUID();

        when(repository.findByUserIdAndId("u1", id))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                controller.updateMessageStatus(id.toString(), true, "u1")
        );
    }

    @Test
    void testUpdateMessageStatus_success() {
        UUID id = UUID.randomUUID();

        InboxMessageEntity msg = new InboxMessageEntity();
        msg.setReceivedAt(Instant.now());

        when(repository.findByUserIdAndId("u1", id))
                .thenReturn(Optional.of(msg)); // first call
        when(repository.findByUserIdAndId("u1", id))
                .thenReturn(Optional.of(msg)); // second call after update

        var result = controller.updateMessageStatus(id.toString(), true, "u1");

        verify(repository).updateReadStatus("u1", id, true);
        assertEquals(msg, result);
    }

    @Test
    void testDeleteMessage_notFound() {
        UUID id = UUID.randomUUID();

        when(repository.findByUserIdAndId("u1", id))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                controller.deleteMessage(id.toString(), "u1")
        );
    }

    @Test
    void testDeleteMessage_success() {
        UUID id = UUID.randomUUID();
        InboxMessageEntity msg = new InboxMessageEntity();

        when(repository.findByUserIdAndId("u1", id))
                .thenReturn(Optional.of(msg));

        controller.deleteMessage(id.toString(), "u1");

        verify(repository).deleteInboxMessage("u1", id);
    }
}
