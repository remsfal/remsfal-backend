package de.remsfal.ticketing.boundary;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.ticketing.control.InboxController;
import de.remsfal.ticketing.control.InboxMessageJsonMapper;
import de.remsfal.ticketing.entity.dto.InboxMessageEntity;
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
    InboxMessageJsonMapper mapper;

    @InjectMock
    RemsfalPrincipal principal;

    @InjectMock
    JsonWebToken jwt;

    InboxResource resource;

    @BeforeEach
    void setup() {
        resource = new InboxResource();
        resource.controller = controller;
        resource.mapper = mapper;
        resource.principal = principal;

        when(principal.getJwt()).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn("11111111-1111-1111-1111-111111111111");
    }

    @Test
    void testGetInboxMessages_success() {
        InboxMessageEntity e = new InboxMessageEntity();

        String userId = "11111111-1111-1111-1111-111111111111";
        when(principal.getId()).thenReturn(UUID.fromString(userId));

        when(controller.getInboxMessages(true, userId)).thenReturn(List.of(e));

        InboxMessageJson dummyJson = mock(InboxMessageJson.class);
        when(mapper.toJsonList(any())).thenReturn(List.of(dummyJson));

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
        when(principal.getId()).thenReturn(UUID.fromString(userId));

        when(controller.updateMessageStatus(msgId.toString(), true, userId))
                .thenReturn(updated);

        InboxMessageJson dummyJson = mock(InboxMessageJson.class);
        when(mapper.toJson(any())).thenReturn(dummyJson);

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
