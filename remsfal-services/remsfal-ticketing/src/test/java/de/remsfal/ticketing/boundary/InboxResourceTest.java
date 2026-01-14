package de.remsfal.ticketing.boundary;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.ticketing.control.InboxController;
import de.remsfal.ticketing.entity.dto.InboxMessageEntity;
import de.remsfal.ticketing.entity.dto.InboxMessageKey;
import de.remsfal.core.json.ticketing.InboxMessageJson;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.eclipse.microprofile.jwt.JsonWebToken;

@QuarkusTest
class InboxResourceTest {

    @InjectMock
    InboxController controller;

    @InjectMock
    RemsfalPrincipal principal;

    @InjectMock
    JsonWebToken jwt;

    InboxResource resource;

    @BeforeEach
    void setup() {
        resource = new InboxResource();
        resource.controller = controller;
        resource.principal = principal;

        when(principal.getJwt()).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn("11111111-1111-1111-1111-111111111111");
    }

    @Test
    void testGetInboxMessages_success() {
        InboxMessageEntity e = new InboxMessageEntity();
        InboxMessageKey key = new InboxMessageKey();
        key.setUserId("11111111-1111-1111-1111-111111111111");
        key.setId(UUID.randomUUID());
        e.setKey(key);
        e.setEventType("ISSUE_CREATED");
        e.setIssueId("issue-123");
        e.setTitle("Test Issue");
        e.setDescription("Test Description");
        e.setIssueType("BUG");
        e.setStatus("OPEN");
        e.setLink("/api/issues/issue-123");
        e.setActorEmail("actor@example.com");
        e.setOwnerEmail("owner@example.com");
        e.setRead(false);
        e.setCreatedAt(java.time.Instant.now());

        String userId = "11111111-1111-1111-1111-111111111111";
        when(principal.getId()).thenReturn(UUID.fromString(userId));

        when(controller.getInboxMessages(true, userId)).thenReturn(List.of(e));

        List<InboxMessageJson> result = resource.getInboxMessages(true);

        assertEquals(1, result.size());
        verify(controller).getInboxMessages(true, userId);
    }

    @Test
    void testGetInboxMessages_badRequest() {
        when(principal.getId()).thenReturn(UUID.randomUUID());

        when(controller.getInboxMessages(any(), any()))
                .thenThrow(new IllegalArgumentException("boom"));

        assertThrows(BadRequestException.class,
                () -> resource.getInboxMessages(false)
        );
    }

    @Test
    void testUpdateMessageStatus_success() {
        UUID msgId = UUID.randomUUID();
        String userId = "11111111-1111-1111-1111-111111111111";

        InboxMessageEntity updated = new InboxMessageEntity();
        InboxMessageKey key = new InboxMessageKey();
        key.setUserId(userId);
        key.setId(msgId);
        updated.setKey(key);
        updated.setEventType("ISSUE_UPDATED");
        updated.setIssueId("issue-456");
        updated.setTitle("Updated Issue");
        updated.setDescription("Updated Description");
        updated.setIssueType("FEATURE");
        updated.setStatus("IN_PROGRESS");
        updated.setLink("/api/issues/issue-456");
        updated.setActorEmail("actor2@example.com");
        updated.setOwnerEmail("owner2@example.com");
        updated.setRead(true);
        updated.setCreatedAt(java.time.Instant.now());

        when(principal.getId()).thenReturn(UUID.fromString(userId));

        when(controller.updateMessageStatus(msgId.toString(), true, userId))
                .thenReturn(updated);

        InboxMessageJson result = resource.updateMessageStatus(msgId.toString(), true);

        assertNotNull(result);
        verify(controller).updateMessageStatus(msgId.toString(), true, userId);
    }

    @Test
    void testUpdateMessageStatus_notFound() {
        when(principal.getId()).thenReturn(UUID.randomUUID());

        when(controller.updateMessageStatus(any(), anyBoolean(), any()))
                .thenThrow(new IllegalArgumentException("missing"));

        assertThrows(NotFoundException.class,
                () -> resource.updateMessageStatus("abc", true)
        );
    }

    @Test
    void testDeleteInboxMessage_success() {
        String userId = "11111111-1111-1111-1111-111111111111";
        when(principal.getId()).thenReturn(UUID.fromString(userId));

        resource.deleteInboxMessage("xyz");

        verify(controller).deleteMessage("xyz", userId);
    }

    @Test
    void testDeleteInboxMessage_notFound() {
        when(principal.getId()).thenReturn(UUID.randomUUID());

        doThrow(new IllegalArgumentException("not found"))
                .when(controller).deleteMessage(any(), any());

        assertThrows(NotFoundException.class,
                () -> resource.deleteInboxMessage("xyz")
        );
    }
}
