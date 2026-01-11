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

    // GET TESTS
    @Test
    void testGetInboxMessages_userIdNull_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.getInboxMessages(true, null));
    }

    @Test
    void testGetInboxMessages_readOnly() {
        InboxMessageEntity msg = new InboxMessageEntity();
        msg.setCreatedAt(Instant.now());

        when(repository.findByUserIdAndRead("u1", true))
                .thenReturn(List.of(msg));

        controller.getInboxMessages(true, "u1");

        verify(repository).findByUserIdAndRead("u1", true);
    }

    @Test
    void testGetInboxMessages_noFilters() {
        InboxMessageEntity msg = new InboxMessageEntity();
        msg.setCreatedAt(Instant.now());

        when(repository.findByUserId("u1"))
                .thenReturn(List.of(msg));

        controller.getInboxMessages(null, "u1");

        verify(repository).findByUserId("u1");
    }

    @Test
    void testGetInboxMessages_sortedByCreatedAt() {
        InboxMessageEntity older = new InboxMessageEntity();
        older.setCreatedAt(Instant.now().minusSeconds(100));

        InboxMessageEntity newer = new InboxMessageEntity();
        newer.setCreatedAt(Instant.now());

        when(repository.findByUserId("u1"))
                .thenReturn(List.of(older, newer));

        var result = controller.getInboxMessages(null, "u1");

        assertEquals(newer, result.get(0));
        assertEquals(older, result.get(1));
    }

    // UPDATE MESSAGE STATUS
    @Test
    void testUpdateMessageStatus_notFound() {
        UUID id = UUID.randomUUID();

        when(repository.findByUserIdAndId("u1", id))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> controller.updateMessageStatus(id.toString(), true, "u1"));
    }

    @Test
    void testUpdateMessageStatus_success() {
        UUID id = UUID.randomUUID();
        InboxMessageEntity msg = new InboxMessageEntity();
        msg.setCreatedAt(Instant.now());

        when(repository.findByUserIdAndId("u1", id))
                .thenReturn(Optional.of(msg))
                .thenReturn(Optional.of(msg));

        var result = controller.updateMessageStatus(id.toString(), true, "u1");

        verify(repository).updateReadStatus("u1", id, true);
        assertEquals(msg, result);
    }

    // DELETE MESSAGE
    @Test
    void testDeleteMessage_notFound() {
        UUID id = UUID.randomUUID();

        when(repository.findByUserIdAndId("u1", id))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> controller.deleteMessage(id.toString(), "u1"));
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

