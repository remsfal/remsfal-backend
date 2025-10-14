package de.remsfal.ticketing.boundary;

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

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.model.ProjectMemberModel.MemberRole;
import de.remsfal.core.model.ticketing.ChatSessionModel;
import de.remsfal.test.TestData;
import de.remsfal.ticketing.control.ChatSessionController;
import de.remsfal.ticketing.control.IssueController;
import de.remsfal.ticketing.entity.dto.IssueEntity;

class ChatSessionResourceAuthTest {

    private static void setField(Object target, Class<?> cls, String name, Object value) throws Exception {
        Field f = cls.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private RemsfalPrincipal principalWithRole(UUID projectId, String role) {
        RemsfalPrincipal principal = mock(RemsfalPrincipal.class);
        JsonWebToken jwt = mock(JsonWebToken.class);
        when(principal.getJwt()).thenReturn(jwt);
        when(principal.getId()).thenReturn(TestData.USER_ID);
        when(jwt.getClaim("project_roles")).thenReturn(Map.of(projectId.toString(), role));
        when(principal.getProjectRoles()).thenReturn(Map.of(projectId, MemberRole.valueOf(role)));
        return principal;
    }

    @Test
    void testCreateChatSession() throws Exception {
        UUID issueId = UUID.randomUUID();
        UUID sessionUUID = UUID.randomUUID();

        IssueEntity issue = mock(IssueEntity.class);
        when(issue.getProjectId()).thenReturn(TestData.PROJECT_ID);
        IssueController issueController = mock(IssueController.class);
        when(issueController.getIssue(issueId)).thenReturn(issue);

        ChatSessionModel session = mock(ChatSessionModel.class);
        when(session.getSessionId()).thenReturn(sessionUUID);
        when(session.getProjectId()).thenReturn(TestData.PROJECT_ID);
        when(session.getIssueId()).thenReturn(issueId);
        when(session.getCreatedAt()).thenReturn(Instant.now());
        when(session.getModifiedAt()).thenReturn(Instant.now());

        ChatSessionController controller = mock(ChatSessionController.class);
        when(controller.createChatSession(TestData.PROJECT_ID, issueId, TestData.USER_ID)).thenReturn(session);

        ChatSessionResource resource = new ChatSessionResource();
        setField(resource, AbstractResource.class, "issueController", issueController);
        setField(resource, AbstractResource.class, "principal", principalWithRole(TestData.PROJECT_ID, MemberRole.PROPRIETOR.name()));
        setField(resource, ChatSessionResource.class, "chatSessionController", controller);
        setField(resource, ChatSessionResource.class, "logger", Logger.getLogger(ChatSessionResource.class));
        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(UriBuilder.fromUri(URI.create("http://localhost")));
        setField(resource, AbstractResource.class, "uri", uriInfo);

        Response resp = resource.createChatSession(issueId);
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