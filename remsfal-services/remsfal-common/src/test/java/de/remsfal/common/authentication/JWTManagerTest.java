package de.remsfal.common.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.test.AbstractTest;
import de.remsfal.test.TestData;
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
        ImmutableUserJson user = ImmutableUserJson.builder()
            .id(TestData.USER_ID_1)
            .email(TestData.USER_EMAIL_1)
            .name(TestData.USER_FIRST_NAME_1)
            .active(true)
            .build();
        Map<String, String> projectRoles = Map.of(
            TestData.PROJECT_ID_1.toString(), "MANAGER",
            TestData.PROJECT_ID_2.toString(), "PROPRIETOR"
        );
        Map<String, String> tenancyProjects = Map.of(
            TestData.TENANCY_ID_1.toString(), TestData.PROJECT_ID_3.toString(),
            TestData.TENANCY_ID_2.toString(), TestData.PROJECT_ID_4.toString()
        );

        String token = jwtManager.createAccessToken(user, projectRoles, tenancyProjects, 3600);

        assertNotNull(token);
        assertEquals(3, token.split("\\.").length, "JWT must have 3 parts");

        Map<String, Object> payload = decodePayload(token);
        assertEquals(TestData.USER_ID_1.toString(), payload.get("sub"));
        assertEquals(TestData.USER_EMAIL_1, payload.get("email"));
        assertEquals(TestData.USER_FIRST_NAME_1, payload.get("name"));
        assertEquals(Boolean.TRUE, payload.get("active"));
        assertEquals("REMSFAL", payload.get("iss"));
        assertTrue(((Number) payload.get("exp")).longValue() > (System.currentTimeMillis() / 1000));
        assertEquals(projectRoles, payload.get("project_roles"));
        assertEquals(tenancyProjects, payload.get("tenancy_projects"));

        assertNull(payload.get("refreshToken"), "access token must NOT contain refreshToken claim");
    }

    @Test
    void testCreateRefreshToken_containsRefreshTokenClaim() {
        String token = jwtManager.createRefreshToken(TestData.USER_ID_2, "u2@example.com", "r-123", 604800);
        Map<String, Object> payload = decodePayload(token);

        assertEquals(TestData.USER_ID_2.toString(), payload.get("sub"));
        assertEquals("u2@example.com", payload.get("email"));
        assertEquals("r-123", payload.get("refreshTokenId"));
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
        ImmutableUserJson user = ImmutableUserJson.builder()
            .id(TestData.USER_ID_1)
            .email(TestData.USER_EMAIL_1)
            .firstName(TestData.USER_FIRST_NAME_1)
            .lastName(TestData.USER_LAST_NAME_1)
            .active(true)
            .build();
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> verifierOnly.createAccessToken(user, Map.of(), Map.of(), 60));

        assertTrue(exception.getMessage().contains("issuer mode"), exception.getMessage());
    }

    @SuppressWarnings("unchecked")
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
