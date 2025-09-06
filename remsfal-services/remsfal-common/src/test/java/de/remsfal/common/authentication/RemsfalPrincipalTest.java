package de.remsfal.common.authentication;

import org.eclipse.microprofile.jwt.JsonWebToken;
import de.remsfal.core.model.UserModel;
import de.remsfal.test.AbstractTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class RemsfalPrincipalTest extends AbstractTest {

    private static void injectJwt(RemsfalPrincipal principal, JsonWebToken jwt) throws Exception {
        Field field = RemsfalPrincipal.class.getDeclaredField("jwt");
        field.setAccessible(true);
        field.set(principal, jwt);
    }

    @Test
    void testGetIdAndEmail_fromJwt() throws Exception {
        RemsfalPrincipal principal = new RemsfalPrincipal();

        JsonWebToken jwt = Mockito.mock(JsonWebToken.class);
        when(jwt.getSubject()).thenReturn("jwt-id");
        when(jwt.getClaim("email")).thenReturn("jwt@example.com");
        when(jwt.getClaim("name")).thenReturn("JWT User");
        when(jwt.getClaim("active")).thenReturn(Boolean.TRUE);
        injectJwt(principal, jwt);

        assertEquals("jwt-id", principal.getId());
        assertEquals("jwt@example.com", principal.getEmail());
        assertEquals("JWT User", principal.getName());
        assertTrue(principal.isActive());
    }

    @Test
    void testFallBackToUserModel_ifJwtMissingClaims() throws Exception {
        RemsfalPrincipal principal = new RemsfalPrincipal();

        JsonWebToken jwt = Mockito.mock(JsonWebToken.class);
        when(jwt.getSubject()).thenReturn(null);
        when(jwt.getClaim("email")).thenReturn(null);
        when(jwt.getClaim("name")).thenReturn(null);
        when(jwt.getClaim("active")).thenReturn(null);
        injectJwt(principal, jwt);

        principal.setUserModel(new UserModel() {
            public String getId() { return "user-id"; }
            public String getEmail() { return "user@example.com"; }
            public String getName() { return "Fallback User"; }
            public Boolean isActive() { return Boolean.FALSE; }
        });

        assertEquals("user-id", principal.getId());
        assertEquals("user@example.com", principal.getEmail());
        assertEquals("Fallback User", principal.getName());
        assertFalse(principal.isActive());
    }

    @Test
    void testActiveDefaultsTrue_whenNeitherJwtNorFallbackProvideIt() throws Exception {
        RemsfalPrincipal principal = new RemsfalPrincipal();

        JsonWebToken jwt = Mockito.mock(JsonWebToken.class);
        when(jwt.getSubject()).thenReturn("jwt-id");
        when(jwt.getClaim("email")).thenReturn("jwt@example.com");
        when(jwt.getClaim("name")).thenReturn("JWT User");
        when(jwt.getClaim("active")).thenReturn(null);
        injectJwt(principal, jwt);

        assertTrue(principal.isActive());
    }

    @Test
    void testGetEmailAndName_preferJwtOverFallback() throws Exception {
        RemsfalPrincipal principal = new RemsfalPrincipal();

        JsonWebToken jwt = Mockito.mock(JsonWebToken.class);
        when(jwt.getSubject()).thenReturn("jwt-id");
        when(jwt.getClaim("email")).thenReturn("jwt@example.com");
        when(jwt.getClaim("name")).thenReturn("JWT User");
        when(jwt.getClaim("active")).thenReturn(Boolean.TRUE);
        injectJwt(principal, jwt);

        principal.setUserModel(new UserModel() {
            public String getId() { return "user-id"; }
            public String getEmail() { return "user@example.com"; }
            public String getName() { return "Fallback User"; }
            public Boolean isActive() { return Boolean.FALSE; }
        });

        assertEquals("jwt-id", principal.getId());
        assertEquals("jwt@example.com", principal.getEmail());
        assertEquals("JWT User", principal.getName());
        assertTrue(principal.isActive());
    }

}
