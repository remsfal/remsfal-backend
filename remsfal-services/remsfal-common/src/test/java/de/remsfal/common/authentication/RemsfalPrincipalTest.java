package de.remsfal.common.authentication;

import org.eclipse.microprofile.jwt.JsonWebToken;
import de.remsfal.test.AbstractTest;
import de.remsfal.test.TestData;
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
    void testReadClaims_fromJwt() throws Exception {
        RemsfalPrincipal principal = new RemsfalPrincipal();

        JsonWebToken jwt = Mockito.mock(JsonWebToken.class);
        when(jwt.getSubject()).thenReturn(TestData.USER_ID.toString());
        when(jwt.getClaim("email")).thenReturn(TestData.USER_EMAIL);
        when(jwt.getClaim("name")).thenReturn(TestData.USER_FIRST_NAME);
        when(jwt.getClaim("active")).thenReturn(Boolean.TRUE);
        injectJwt(principal, jwt);

        assertEquals(TestData.USER_ID, principal.getId());
        assertEquals(TestData.USER_EMAIL, principal.getEmail());
        assertEquals(TestData.USER_FIRST_NAME, principal.getName());
        assertTrue(principal.isActive());
    }

    @Test
    void testReturnNulls_whenClaimsMissing() throws Exception {
        RemsfalPrincipal principal = new RemsfalPrincipal();

        JsonWebToken jwt = Mockito.mock(JsonWebToken.class);
        when(jwt.getSubject()).thenReturn(null);
        when(jwt.getClaim("email")).thenReturn(null);
        when(jwt.getClaim("name")).thenReturn(null);
        when(jwt.getClaim("active")).thenReturn(null);
        injectJwt(principal, jwt);

        assertNull(principal.getId());
        assertNull(principal.getEmail());
        assertNull(principal.getName());
        assertNull(principal.isActive());
    }

    @Test
    void testReturnNulls_whenJwtNotInjected() {
        RemsfalPrincipal principal = new RemsfalPrincipal();
        assertNull(principal.getId());
        assertNull(principal.getEmail());
        assertNull(principal.getName());
        assertNull(principal.isActive());
    }

}
