package de.remsfal.common.authentication;

import com.nimbusds.jwt.JWTClaimsSet;
import de.remsfal.core.model.UserModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RemsfalPrincipalTest {

    @Test
    void testGetIdAndEmail_fromUserModelFallback() {
        RemsfalPrincipal principal = new RemsfalPrincipal();

        // TODO: Replace with SessionInfo-only logic once JWT-based flow is final
        principal.setUserModel(new UserModel() {
            public String getId() {
                return "user-id";
            }

            public String getEmail() {
                return "user@example.com";
            }

            public String getName() {
                return "Test User";
            }

            public Boolean isActive() {
                return true;
            }
        });

        assertEquals("user-id", principal.getId());
        assertEquals("user@example.com", principal.getEmail());
    }

    @Test
    void testGetNameAndIsActive() {
        RemsfalPrincipal principal = new RemsfalPrincipal();

        // TODO: Replace with SessionInfo-only logic once JWT-based flow is final
        principal.setUserModel(new UserModel() {
            public String getId() {
                return "id";
            }

            public String getEmail() {
                return "email@example.com";
            }

            public String getName() {
                return "Test User";
            }

            public Boolean isActive() {
                return Boolean.FALSE;
            }
        });

        assertEquals("Test User", principal.getName());
        assertFalse(principal.isActive());
    }

    @Test
    void testGetIdAndEmail_fromSessionInfo() {
        RemsfalPrincipal principal = new RemsfalPrincipal();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("jwt-id")
                .claim("email", "jwt@example.com")
                .build();

        SessionInfo sessionInfo = new SessionInfo(claims);
        principal.setSessionInfo(sessionInfo);

        assertEquals("jwt-id", principal.getId());
        assertEquals("jwt@example.com", principal.getEmail());
    }

}
