package de.remsfal.common.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import de.remsfal.test.AbstractTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;

import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class JWTManagerTest extends AbstractTest {

    private JWTManager jwtManager;

    @BeforeEach
    void setUp() throws Exception {
        jwtManager = new JWTManager();

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        jwtManager.setPrivateKey(keyPair.getPrivate());
        jwtManager.setPublicKey(keyPair.getPublic());

        Field kidField = JWTManager.class.getDeclaredField("keyId");
        kidField.setAccessible(true);
        kidField.set(jwtManager, "unit-test-kid");

        Field issField = JWTManager.class.getDeclaredField("issuer");
        issField.setAccessible(true);
        issField.set(jwtManager, "REMSFAL");
    }

    @Test
    void testCreateAccessToken_containsStandardAndProjectClaims() {
        Map<String, String> projectRoles = Map.of(
                "proj-1", "MANAGER",
                "proj-2", "PROPRIETOR"
        );

        String token = jwtManager.createAccessToken("u1", "u1@example.com", "User One",
                true, projectRoles, 3600);

        assertNotNull(token);
        assertEquals(3, token.split("\\.").length, "JWT must have 3 parts");

        Map<String, Object> payload = decodePayload(token);
        assertEquals("u1", payload.get("sub"));
        assertEquals("u1@example.com", payload.get("email"));
        assertEquals("User One", payload.get("name"));
        assertEquals(Boolean.TRUE, payload.get("active"));
        assertEquals("REMSFAL", payload.get("iss"));
        assertTrue(((Number) payload.get("exp")).longValue() > (System.currentTimeMillis() / 1000));
        assertEquals(projectRoles, payload.get("project_roles"));

        assertNull(payload.get("refreshToken"), "access token must NOT contain refreshToken claim");
    }

    @Test
    void testCreateRefreshToken_containsRefreshTokenClaim() {
        String token = jwtManager.createRefreshToken("u2", "u2@example.com", "r-123", 604800);
        Map<String, Object> payload = decodePayload(token);

        assertEquals("u2", payload.get("sub"));
        assertEquals("u2@example.com", payload.get("email"));
        assertEquals("r-123", payload.get("refreshToken"));
    }

    @Test
    void testGetPublicJwk_exposesConfiguredKid() throws JOSEException {
        RSAKey jwk = jwtManager.getPublicJwk();
        assertEquals("unit-test-kid", jwk.getKeyID());
        assertInstanceOf(RSAPublicKey.class, jwk.toPublicKey());
    }

    @Test
    void testIssuerGuard_throwsWhenNoPrivateKey() {
        JWTManager verifierOnly = new JWTManager();
        Map<String, String> projectRoles = Map.of();
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> verifierOnly.createAccessToken("u1", "e@x", "Name", true, projectRoles, 60));

        assertTrue(exception.getMessage().contains("issuer mode"), exception.getMessage());
    }

    private Map<String, Object> decodePayload(String jwt) {
        try {
            String payloadB64 = jwt.split("\\.")[1];
            byte[] json = Base64.getUrlDecoder().decode(payloadB64);
            ObjectMapper mapper = new ObjectMapper();

            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new AssertionError("Failed to decode JWT payload", e);
        }
    }

}
