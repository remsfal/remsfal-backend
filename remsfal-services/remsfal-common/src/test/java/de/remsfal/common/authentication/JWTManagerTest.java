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
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.io.TempDir;

import io.quarkus.runtime.LaunchMode;

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
            .firstName(TestData.USER_FIRST_NAME_1)
            .active(true)
            .build();
        Map<String, String> projectRoles = Map.of(
            TestData.PROJECT_ID_1.toString(), "MANAGER",
            TestData.PROJECT_ID_2.toString(), "PROPRIETOR"
        );
        Map<String, String> organizationRoles = Map.of(
            TestData.ORGANIZATION_ID_1.toString(), "OWNER",
            TestData.ORGANIZATION_ID_2.toString(), "STAFF"
        );
        Map<String, String> tenancyProjects = Map.of(
            TestData.AGREEMENT_ID_1.toString(), TestData.PROJECT_ID_3.toString(),
            TestData.AGREEMENT_ID_2.toString(), TestData.PROJECT_ID_4.toString()
        );

        String token = jwtManager.createAccessToken(user, projectRoles, organizationRoles, tenancyProjects, 3600);

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
        assertEquals(organizationRoles, payload.get("organization_roles"));
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
                () -> verifierOnly.createAccessToken(user, Map.of(), Map.of(), Map.of(), 60));

        assertTrue(exception.getMessage().contains("issuer mode"), exception.getMessage());
    }

    @Test
    void testInit_verifierModeWhenSigningDisabled() throws Exception {
        final JWTManager manager = newManager(false, Optional.of("privateKey.pem"), ".dev-keys");
        manager.init(LaunchMode.NORMAL);

        final IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> manager.createRefreshToken(TestData.USER_ID_1, TestData.USER_EMAIL_1, "r-1", 60));
        assertTrue(exception.getMessage().contains("issuer mode"), exception.getMessage());
    }

    @Test
    void testInit_loadsConfiguredKeyAndDerivesPublicKey() throws Exception {
        final JWTManager manager = newManager(true, Optional.of("classpath:privateKey.pem"), ".dev-keys");
        manager.init(LaunchMode.NORMAL);

        assertEquals(KeyLoader.loadPublicKey("publicKey.pem"), manager.getPublicJwk().toPublicKey());
        assertNotNull(manager.createRefreshToken(TestData.USER_ID_1, TestData.USER_EMAIL_1, "r-1", 60));
    }

    @Test
    void testInit_failsWithoutKeyInProduction(@TempDir final Path directory) throws Exception {
        final JWTManager manager = newManager(true, Optional.empty(), directory.toString());

        final IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> manager.init(LaunchMode.NORMAL));
        assertTrue(exception.getMessage().contains("private-key-location"), exception.getMessage());
        assertFalse(Files.exists(directory.resolve(KeyLoader.PRIVATE_KEY_FILE_NAME)));
    }

    @Test
    void testInit_failsWithInvalidKeyLocation() throws Exception {
        final JWTManager manager = newManager(true, Optional.of("file:/does/not/exist.pem"), ".dev-keys");

        final IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> manager.init(LaunchMode.NORMAL));
        assertTrue(exception.getMessage().contains("/does/not/exist.pem"), exception.getMessage());
    }

    @Test
    void testInit_generatesAndReusesDevKey(@TempDir final Path directory) throws Exception {
        final Path keyDirectory = directory.resolve(".dev-keys");
        final JWTManager first = newManager(true, Optional.empty(), keyDirectory.toString());
        first.init(LaunchMode.DEVELOPMENT);
        assertTrue(Files.exists(keyDirectory.resolve(KeyLoader.PRIVATE_KEY_FILE_NAME)));

        final JWTManager second = newManager(true, Optional.empty(), keyDirectory.toString());
        second.init(LaunchMode.DEVELOPMENT);

        assertEquals(first.getPublicJwk().toPublicKey(), second.getPublicJwk().toPublicKey());
    }

    private static JWTManager newManager(final boolean signingEnabled, final Optional<String> location,
        final String devKeyDirectory) throws Exception {
        final JWTManager manager = new JWTManager();
        setField(manager, "signingEnabled", signingEnabled);
        setField(manager, "privateKeyLocation", location);
        setField(manager, "devKeyDirectory", devKeyDirectory);
        setField(manager, "keyId", "unit-test-kid");
        setField(manager, "issuer", "REMSFAL");
        return manager;
    }

    private static void setField(final JWTManager manager, final String name, final Object value) throws Exception {
        final Field field = JWTManager.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(manager, value);
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
