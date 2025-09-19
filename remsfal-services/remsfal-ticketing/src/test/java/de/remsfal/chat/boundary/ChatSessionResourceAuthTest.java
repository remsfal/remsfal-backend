package de.remsfal.chat.boundary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import de.remsfal.chat.control.ChatSessionController;
import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.model.ProjectMemberModel.MemberRole;
import de.remsfal.core.model.ticketing.ChatSessionModel;

class ChatSessionResourceAuthTest {

    private static void setField(Object target, Class<?> cls, String name, Object value) throws Exception {
        Field f = cls.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private RemsfalPrincipal principalWithRole(String projectId, String role) {
        RemsfalPrincipal principal = mock(RemsfalPrincipal.class);
        JsonWebToken jwt = mock(JsonWebToken.class);
        when(principal.getJwt()).thenReturn(jwt);
        when(principal.getId()).thenReturn("user1");
        when(jwt.getClaim("project_roles")).thenReturn(Map.of(projectId, role));
        return principal;
    }

    @Test
    void testCreateChatSession() throws Exception {
        String projectId = "11111111-1111-1111-1111-111111111111";
        String taskId = "22222222-2222-2222-2222-222222222222";
        UUID sessionUUID = UUID.randomUUID();

        ChatSessionModel session = mock(ChatSessionModel.class);
        when(session.getSessionId()).thenReturn(sessionUUID);
        when(session.getProjectId()).thenReturn(UUID.fromString(projectId));
        when(session.getTaskId()).thenReturn(UUID.fromString(taskId));
        when(session.getCreatedAt()).thenReturn(Instant.now());
        when(session.getModifiedAt()).thenReturn(Instant.now());

        ChatSessionController controller = mock(ChatSessionController.class);
        when(controller.createChatSession(projectId, taskId, "user1")).thenReturn(session);

        ChatSessionResource resource = new ChatSessionResource();
        setField(resource, ChatSubResource.class, "principal", principalWithRole(projectId, MemberRole.PROPRIETOR.name()));
        setField(resource, ChatSessionResource.class, "chatSessionController", controller);
        setField(resource, ChatSessionResource.class, "logger", Logger.getLogger(ChatSessionResource.class));
        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(UriBuilder.fromUri(URI.create("http://localhost")));
        setField(resource, ChatSubResource.class, "uri", uriInfo);

        Response resp = resource.createChatSession(projectId, taskId);
        assertEquals(Response.Status.CREATED.getStatusCode(), resp.getStatus());
        assertNotNull(resp.getEntity());
    }

    @Test
    void testExtractFileNameFromUrl() {
        ChatSessionResource resource = new ChatSessionResource();
        assertEquals("file.txt", resource.extractFileNameFromUrl("http://localhost/files/file.txt"));
        assertEquals("file.txt", resource.extractFileNameFromUrl("file.txt"));
        assertThrows(IllegalArgumentException.class, () -> resource.extractFileNameFromUrl("http://localhost/files/"));
        assertThrows(IllegalArgumentException.class, () -> resource.extractFileNameFromUrl(null));
    }

}
