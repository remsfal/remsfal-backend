package de.remsfal.chat.boundary;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.model.ProjectMemberModel.MemberRole;

@QuarkusTest
class ChatSubResourceTest {

    private static void injectPrincipal(ChatSubResource resource, RemsfalPrincipal principal) throws Exception {
        Field field = ChatSubResource.class.getDeclaredField("principal");
        field.setAccessible(true);
        field.set(resource, principal);
    }

    private RemsfalPrincipal mockPrincipal(Object claim) {
        RemsfalPrincipal principal = mock(RemsfalPrincipal.class);
        JsonWebToken jwt = mock(JsonWebToken.class);
        when(principal.getJwt()).thenReturn(jwt);
        when(principal.getId()).thenReturn("user1");
        when(jwt.getClaim("project_roles")).thenReturn(claim);
        return principal;
    }

    @Test
    void testCheckReadPermissionsSuccess() throws Exception {
        ChatSubResource resource = new ChatSubResource();
        injectPrincipal(resource, mockPrincipal(Map.of("p1", MemberRole.MANAGER.name())));
        assertTrue(resource.checkReadPermissions("p1"));
    }

    @Test
    void testCheckReadPermissionsWithJsonObjectClaim() throws Exception {
        ChatSubResource resource = new ChatSubResource();
        JsonObject roles = Json.createObjectBuilder()
                .add("p1", MemberRole.PROPRIETOR.name()).build();
        injectPrincipal(resource, mockPrincipal(roles));
        assertTrue(resource.checkReadPermissions("p1"));
    }

    @Test
    void testCheckReadPermissionsNotAuthorized() throws Exception {
        ChatSubResource resource = new ChatSubResource();
        RemsfalPrincipal principal = mock(RemsfalPrincipal.class);
        when(principal.getJwt()).thenReturn(null);
        when(principal.getId()).thenReturn(null);
        injectPrincipal(resource, principal);
        assertThrows(NotAuthorizedException.class, () -> resource.checkReadPermissions("p1"));
    }

    @Test
    void testCheckReadPermissionsForbidden() throws Exception {
        ChatSubResource resource = new ChatSubResource();
        injectPrincipal(resource, mockPrincipal(Map.of()));
        assertThrows(ForbiddenException.class, () -> resource.checkReadPermissions("p1"));
    }

    @Test
    void testCheckReadPermissionsInvalidRole() throws Exception {
        ChatSubResource resource = new ChatSubResource();
        injectPrincipal(resource, mockPrincipal(Map.of("p1", "INVALID")));
        assertThrows(ForbiddenException.class, () -> resource.checkReadPermissions("p1"));
    }

    @Test
    void testCheckWritePermissionsProprietor() throws Exception {
        ChatSubResource resource = new ChatSubResource();
        injectPrincipal(resource, mockPrincipal(Map.of("p1", MemberRole.PROPRIETOR.name())));
        assertTrue(resource.checkWritePermissions("p1"));
    }

    @Test
    void testCheckWritePermissionsManager() throws Exception {
        ChatSubResource resource = new ChatSubResource();
        injectPrincipal(resource, mockPrincipal(Map.of("p1", MemberRole.MANAGER.name())));
        assertTrue(resource.checkWritePermissions("p1"));
    }

    @Test
    void testCheckWritePermissionsForbidden() throws Exception {
        ChatSubResource resource = new ChatSubResource();
        injectPrincipal(resource, mockPrincipal(Map.of("p1", MemberRole.LESSOR.name())));
        assertThrows(ForbiddenException.class, () -> resource.checkWritePermissions("p1"));
    }

    @Test
    void testCheckOwnerPermissionsProprietor() throws Exception {
        ChatSubResource resource = new ChatSubResource();
        injectPrincipal(resource, mockPrincipal(Map.of("p1", MemberRole.PROPRIETOR.name())));
        assertTrue(resource.checkOwnerPermissions("p1"));
    }

    @Test
    void testCheckOwnerPermissionsForbidden() throws Exception {
        ChatSubResource resource = new ChatSubResource();
        injectPrincipal(resource, mockPrincipal(Map.of("p1", MemberRole.MANAGER.name())));
        assertThrows(ForbiddenException.class, () -> resource.checkOwnerPermissions("p1"));
    }

}
