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
import de.remsfal.test.TestData;

@QuarkusTest
class ChatSubResourceTest {

    private static void injectPrincipal(AbstractResource resource, RemsfalPrincipal principal) throws Exception {
        Field field = AbstractResource.class.getDeclaredField("principal");
        field.setAccessible(true);
        field.set(resource, principal);
    }

    private RemsfalPrincipal mockPrincipal(Object claim) {
        RemsfalPrincipal principal = mock(RemsfalPrincipal.class);
        JsonWebToken jwt = mock(JsonWebToken.class);
        when(principal.getJwt()).thenReturn(jwt);
        when(principal.getId()).thenReturn(TestData.USER_ID);
        when(jwt.getClaim("project_roles")).thenReturn(claim);
        return principal;
    }

    @Test
    void testCheckReadPermissionsSuccess() throws Exception {
        AbstractResource resource = new AbstractResource();
        injectPrincipal(resource, mockPrincipal(Map.of("p1", MemberRole.MANAGER.name())));
        assertTrue(resource.checkReadPermissions("p1"));
    }

    @Test
    void testCheckReadPermissionsWithJsonObjectClaim() throws Exception {
        AbstractResource resource = new AbstractResource();
        JsonObject roles = Json.createObjectBuilder()
                .add("p1", MemberRole.PROPRIETOR.name()).build();
        injectPrincipal(resource, mockPrincipal(roles));
        assertTrue(resource.checkReadPermissions("p1"));
    }

    @Test
    void testCheckReadPermissionsNotAuthorized() throws Exception {
        AbstractResource resource = new AbstractResource();
        RemsfalPrincipal principal = mock(RemsfalPrincipal.class);
        when(principal.getJwt()).thenReturn(null);
        when(principal.getId()).thenReturn(null);
        injectPrincipal(resource, principal);
        assertThrows(NotAuthorizedException.class, () -> resource.checkReadPermissions("p1"));
    }

    @Test
    void testCheckReadPermissionsForbidden() throws Exception {
        AbstractResource resource = new AbstractResource();
        injectPrincipal(resource, mockPrincipal(Map.of()));
        assertThrows(ForbiddenException.class, () -> resource.checkReadPermissions("p1"));
    }

    @Test
    void testCheckReadPermissionsInvalidRole() throws Exception {
        AbstractResource resource = new AbstractResource();
        injectPrincipal(resource, mockPrincipal(Map.of("p1", "INVALID")));
        assertThrows(ForbiddenException.class, () -> resource.checkReadPermissions("p1"));
    }

    @Test
    void testCheckWritePermissionsProprietor() throws Exception {
        AbstractResource resource = new AbstractResource();
        injectPrincipal(resource, mockPrincipal(Map.of("p1", MemberRole.PROPRIETOR.name())));
        assertTrue(resource.checkWritePermissions("p1"));
    }

    @Test
    void testCheckWritePermissionsManager() throws Exception {
        AbstractResource resource = new AbstractResource();
        injectPrincipal(resource, mockPrincipal(Map.of("p1", MemberRole.MANAGER.name())));
        assertTrue(resource.checkWritePermissions("p1"));
    }

    @Test
    void testCheckWritePermissionsForbidden() throws Exception {
        AbstractResource resource = new AbstractResource();
        injectPrincipal(resource, mockPrincipal(Map.of("p1", MemberRole.LESSOR.name())));
        assertThrows(ForbiddenException.class, () -> resource.checkWritePermissions("p1"));
    }

    @Test
    void testCheckOwnerPermissionsProprietor() throws Exception {
        AbstractResource resource = new AbstractResource();
        injectPrincipal(resource, mockPrincipal(Map.of("p1", MemberRole.PROPRIETOR.name())));
        assertTrue(resource.checkOwnerPermissions("p1"));
    }

    @Test
    void testCheckOwnerPermissionsForbidden() throws Exception {
        AbstractResource resource = new AbstractResource();
        injectPrincipal(resource, mockPrincipal(Map.of("p1", MemberRole.MANAGER.name())));
        assertThrows(ForbiddenException.class, () -> resource.checkOwnerPermissions("p1"));
    }

}
