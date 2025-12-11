package de.remsfal.ticketing.boundary;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.ticketing.control.InboxController;
import de.remsfal.ticketing.control.InboxMessageJsonMapper;
import de.remsfal.ticketing.entity.dto.InboxMessageEntity;
import de.remsfal.core.json.ticketing.InboxMessageJson;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class InboxResourceTest {

    @InjectMock
    InboxController controller;

    @InjectMock
    InboxMessageJsonMapper mapper;

    @InjectMock
    RemsfalPrincipal principal;

    InboxResource resource;

    @BeforeEach
    void setup() {
        resource = new InboxResource();
        resource.controller = controller;
        resource.mapper = mapper;
        resource.principal = principal;
    }

    @Test
    void testGetInboxMessages_success() {
        InboxMessageEntity e = new InboxMessageEntity();

        when(controller.getInboxMessages("INFO", true, "user1"))
                .thenReturn(List.of(e));

        InboxMessageJson dummyJson = mock(InboxMessageJson.class);

        when(mapper.toJsonList(any()))
                .thenReturn(List.of(dummyJson));

        List<InboxMessageJson> result =
                resource.getInboxMessages("INFO", true, "user1");

        assertEquals(1, result.size());
        verify(controller).getInboxMessages("INFO", true, "user1");
    }

    @Test
    void testGetInboxMessages_badRequest() {
        when(controller.getInboxMessages(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("boom"));

        assertThrows(BadRequestException.class, () ->
                resource.getInboxMessages("x", null, "u")
        );
    }

    @Test
    void testUpdateMessageStatus_success() {
        UUID msgId = UUID.randomUUID();

        InboxMessageEntity updated = new InboxMessageEntity();

        when(principal.getId()).thenReturn(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        when(controller.updateMessageStatus(msgId.toString(), true, "11111111-1111-1111-1111-111111111111"))
                .thenReturn(updated);

        InboxMessageJson dummyJson = mock(InboxMessageJson.class);
        when(mapper.toJson(any())).thenReturn(dummyJson);

        InboxMessageJson result =
                resource.updateMessageStatus(msgId.toString(), true);

        assertNotNull(result);
        verify(controller).updateMessageStatus(
                msgId.toString(), true, "11111111-1111-1111-1111-111111111111"
        );
    }

    @Test
    void testUpdateMessageStatus_notFound() {
        when(principal.getId()).thenReturn(UUID.randomUUID());
        when(controller.updateMessageStatus(any(), anyBoolean(), any()))
                .thenThrow(new IllegalArgumentException("missing"));

        assertThrows(NotFoundException.class, () ->
                resource.updateMessageStatus("abc", true)
        );
    }

    @Test
    void testDeleteInboxMessage_success() {
        when(principal.getId()).thenReturn(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        resource.deleteInboxMessage("xyz");

        verify(controller).deleteMessage("xyz", "11111111-1111-1111-1111-111111111111");
    }

    @Test
    void testDeleteInboxMessage_notFound() {
        when(principal.getId()).thenReturn(UUID.randomUUID());

        doThrow(new IllegalArgumentException("not found"))
                .when(controller).deleteMessage(any(), any());

        assertThrows(NotFoundException.class, () ->
                resource.deleteInboxMessage("xyz")
        );
    }
}
