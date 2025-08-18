package de.remsfal.common.authentication;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.JWSAlgorithm;

import de.remsfal.test.AbstractTest;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import io.smallrye.jwt.build.Jwt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class JWTManagerTest extends AbstractTest {

    private JWTManager jwtManager;

    private PrivateKey mockPrivateKey;
    private PublicKey mockPublicKey;

    @BeforeEach
    void setUp() throws Exception {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        mockPrivateKey = keyPair.getPrivate();
        mockPublicKey = keyPair.getPublic();
        jwtManager = new JWTManager();

        try (MockedStatic<KeyLoader> keyLoaderMock = Mockito.mockStatic(KeyLoader.class)) {
            keyLoaderMock.when(() -> KeyLoader.loadPrivateKey(Mockito.anyString()))
                .thenReturn(mockPrivateKey);
            keyLoaderMock.when(() -> KeyLoader.loadPublicKey(Mockito.anyString()))
                .thenReturn(mockPublicKey);
        }

        jwtManager = new JWTManager();
        jwtManager.setPrivateKey(mockPrivateKey);
        jwtManager.setPublicKey(mockPublicKey);
    }

    /**
     * Injects a value into a private field using reflection.
     *
     * @param target The object whose field should be set
     * @param fieldName The name of the private field
     * @param value The value to set (can be String, int, etc.)
     */
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testCreateJWT() {
        // Arrange
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
            .subject("12345")
            .claim("email", "user@example.de")
            .expirationTime(new Date(System.currentTimeMillis() + 3600 * 1000))
            .build();

        SessionInfo sessionInfo = new SessionInfo(claims);

        // Act
        String jwt = jwtManager.createJWT(sessionInfo);

        // Assert
        assertNotNull(jwt);
        assertEquals(3, jwt.split("\\.").length, "JWT should have three parts");
    }

    @Test
    void testVerifyJWT_Success() {
        // Arrange
        String jwt = Jwt.claims()
            .claim("sub", "12345")
            .claim("email", "user@example.com")
            .expiresAt(System.currentTimeMillis() / 1000 + 3600)
            .jws().algorithm(SignatureAlgorithm.RS256)
            .sign(mockPrivateKey);

        // Act
        SessionInfo sessionInfo = jwtManager.verifyJWT(jwt);

        // Assert
        assertNotNull(sessionInfo);
        assertEquals("12345", sessionInfo.getClaims().get("sub"));
        assertEquals("user@example.com", sessionInfo.getClaims().get("email"));
    }

    @Test
    void testVerifyJWT_ExpiredToken() {
        // Arrange
        String jwt = Jwt.claims()
            .claim("sub", "12345")
            .expiresAt(System.currentTimeMillis() / 1000 - 10) // Token expired
            .jws().algorithm(SignatureAlgorithm.RS256)
            .sign(mockPrivateKey);

        // Act & Assert
        assertThrows(TokenExpiredException.class, () -> jwtManager.verifyJWT(jwt));
    }

    @Test
    void testVerifyJqt_noClaims_fail() {
        // Arrange
        String jwt = Jwt.claims()
            .jws().algorithm(SignatureAlgorithm.RS256)
            .sign(mockPrivateKey);

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> jwtManager.verifyJWT(jwt));
    }

    @Test
    void testVerifyJWT_missing_refreshtoken_fails() {
        // Arrange
        String jwt = Jwt.claims()
            .claim("sub", "12345")
            .claim("email", "1234")
            .expiresAt(System.currentTimeMillis() / 1000 + 3600)
            .jws().algorithm(SignatureAlgorithm.RS256)
            .sign(mockPrivateKey);

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> jwtManager.verifyJWT(jwt, true));
    }

    @Test
    void testVerifyJWT_invalidSessionInfo_fails() {
        // Arrange
        String jwt = Jwt.claims()
            .claim("sub", "12345")
            .expiresAt(System.currentTimeMillis() / 1000 + 3600)
            .jws().algorithm(SignatureAlgorithm.RS256)
            .sign(mockPrivateKey);

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> jwtManager.verifyJWT(jwt));
    }

    @Test
    void testVerifyJWT_InvalidSignature() {
        // Arrange
        String jwt = "invalid.jwt.token";

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> jwtManager.verifyJWT(jwt));
    }

    @Test
    void test_verifyTokenManually() {
        // Arrange
        String jwt = Jwt.claims()
            .claim("sub", "12345")
            .claim("email", "12345")
            .expiresAt(System.currentTimeMillis() / 1000 + 3600)
            .jws().algorithm(SignatureAlgorithm.RS256)
            .sign(mockPrivateKey);

        // Act
        JWTClaimsSet claims = jwtManager.verifyTokenManually(jwt, mockPublicKey);

        // Assert
        assertNotNull(claims);
        assertEquals("12345", claims.getSubject());
        assertEquals("12345", claims.getClaim("email"));
    }

    @Test
    void test_verifyTokenManually_InvalidSignature() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey wrongKey = keyPair.getPrivate();

        // Arrange
        String jwt = Jwt.claims()
            .claim("sub", "12345")
            .claim("email", "12345")
            .expiresAt(System.currentTimeMillis() / 1000 + 3600)
            .jws().algorithm(SignatureAlgorithm.RS256)
            .sign(wrongKey);

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> jwtManager.verifyTokenManually(jwt, mockPublicKey));
    }

    @Test
    void testInit_LoadsPublicKeyFromJwks_WhenNoPrivateKeyAvailable() throws Exception {
        RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) mockPublicKey)
            .keyUse(KeyUse.SIGNATURE)
            .algorithm(JWSAlgorithm.RS256)
            .keyID("test")
            .build();
        JWKSet jwkSet = new JWKSet(rsaKey);

        try (MockedStatic<JWKSet> jwkSetMock = Mockito.mockStatic(JWKSet.class);
             MockedStatic<KeyLoader> keyLoaderMock = Mockito.mockStatic(KeyLoader.class)) {

            jwkSetMock.when(() -> JWKSet.load(Mockito.any(URL.class))).thenReturn(jwkSet);
            keyLoaderMock.when(() -> KeyLoader.loadPrivateKey(Mockito.anyString())).thenThrow(new IllegalArgumentException());
            keyLoaderMock.when(() -> KeyLoader.loadPublicKey(Mockito.anyString())).thenThrow(new IllegalArgumentException());

            JWTManager manager = new JWTManager();
            setField(manager, "jwksUrl", "http://external/jwks");
            setField(manager, "privateKeyLocation", "private.pem");
            setField(manager, "publicKeyLocation", "public.pem");

            manager.init();

            String jwt = Jwt.claims()
                .claim("sub", "12345")
                .claim("email", "user@example.com")
                .expiresAt(System.currentTimeMillis() / 1000 + 3600)
                .jws().algorithm(SignatureAlgorithm.RS256)
                .sign(mockPrivateKey);

            SessionInfo info = manager.verifyJWT(jwt);
            assertEquals("12345", info.getUserId());
            jwkSetMock.verify(() -> JWKSet.load(Mockito.any(URL.class)));
        }
    }

    @Test
    void testInit_SkipsJwksLookup_IfUrlPointsToSelf() throws Exception {
        try (MockedStatic<JWKSet> jwkSetMock = Mockito.mockStatic(JWKSet.class);
             MockedStatic<KeyLoader> keyLoaderMock = Mockito.mockStatic(KeyLoader.class)) {

            keyLoaderMock.when(() -> KeyLoader.loadPrivateKey(Mockito.anyString())).thenThrow(new IllegalArgumentException());
            keyLoaderMock.when(() -> KeyLoader.loadPublicKey(Mockito.anyString())).thenReturn(mockPublicKey);

            JWTManager manager = new JWTManager();
            setField(manager, "jwksUrl", "http://localhost:8080/api/v1/authentication/jwks");
            setField(manager, "httpPort", 8080);
            setField(manager, "httpHost", "localhost");
            setField(manager, "privateKeyLocation", "private.pem");
            setField(manager, "publicKeyLocation", "public.pem");

            manager.init();

            jwkSetMock.verify(() -> JWKSet.load(Mockito.any(URL.class)), Mockito.never());
            keyLoaderMock.verify(() -> KeyLoader.loadPublicKey(Mockito.anyString()));
        }
    }

    @Test
    void testInit_IgnoresJwksUrl_WhenPrivateKeyIsPresent() throws Exception {
        try (MockedStatic<JWKSet> jwkSetMock = Mockito.mockStatic(JWKSet.class);
             MockedStatic<KeyLoader> keyLoaderMock = Mockito.mockStatic(KeyLoader.class)) {

            keyLoaderMock.when(() -> KeyLoader.loadPrivateKey(Mockito.anyString())).thenReturn(mockPrivateKey);
            keyLoaderMock.when(() -> KeyLoader.loadPublicKey(Mockito.anyString())).thenReturn(mockPublicKey);

            JWTManager manager = new JWTManager();
            setField(manager, "jwksUrl", "http://example.com/jwks");
            setField(manager, "privateKeyLocation", "private.pem");
            setField(manager, "publicKeyLocation", "public.pem");

            manager.init();

            jwkSetMock.verify(() -> JWKSet.load(Mockito.any(URL.class)), Mockito.never());
            keyLoaderMock.verify(() -> KeyLoader.loadPublicKey(Mockito.anyString()));
        }
    }

    @Test
    void testGetPublicJwk_returnsExpectedKey() throws Exception {
        jwtManager.setPublicKey(mockPublicKey);

        Field keyIdField = JWTManager.class.getDeclaredField("keyId");
        keyIdField.setAccessible(true);
        keyIdField.set(jwtManager, "remsfal-platform-key");

        RSAKey jwk = jwtManager.getPublicJwk();

        assertNotNull(jwk);
        assertEquals("remsfal-platform-key", jwk.getKeyID());
    }

    @Test
    void testIsSelfUrl_returnsTrueForLocalhost() throws Exception {
        setField(jwtManager, "httpPort", 8080);
        setField(jwtManager, "httpHost", "localhost");
        assertTrue(jwtManager.isSelfUrl("http://localhost:8080/jwks"));
    }

    @Test
    void testIsSelfUrl_returnsFalseForOtherHost() throws Exception {
        setField(jwtManager, "httpPort", 8080);
        setField(jwtManager, "httpHost", "localhost");
        assertFalse(jwtManager.isSelfUrl("http://example.com:8080/jwks"));
    }

    @Test
    void testIsSelfUrl_malformedURL_returnsFalse() {
        assertFalse(jwtManager.isSelfUrl("not-a-url"));
    }

    @Test
    void testLoadPublicKeyFromJwks_throwsExceptionOnEmptyKeyList() {
        assertThrows(IllegalArgumentException.class, () -> {
            try (MockedStatic<JWKSet> jwkSetMock = Mockito.mockStatic(JWKSet.class)) {
                jwkSetMock.when(() -> JWKSet.load(Mockito.any(URL.class)))
                        .thenReturn(new JWKSet());
                jwtManager.loadPublicKeyFromJwks("http://test-jwks");
            }
        });
    }

}
