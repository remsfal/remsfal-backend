package de.remsfal.service.boundary.authentication;

import com.nimbusds.jwt.JWTClaimsSet;
import de.remsfal.service.boundary.exception.TokenExpiredException;
import de.remsfal.service.boundary.exception.UnauthorizedException;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import io.smallrye.jwt.build.Jwt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class JWTManagerTest {

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
        assertThrows(UnauthorizedException.class, () -> jwtManager.verifyJWT(jwt));
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
        assertThrows(UnauthorizedException.class, () -> jwtManager.verifyJWT(jwt, true));


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
        assertThrows(UnauthorizedException.class, () -> jwtManager.verifyJWT(jwt));

    }


    @Test
    void testVerifyJWT_InvalidSignature() {
        // Arrange
        String jwt = "invalid.jwt.token";

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> jwtManager.verifyJWT(jwt));
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
        assertThrows(UnauthorizedException.class, () -> jwtManager.verifyTokenManually(jwt, mockPublicKey));
    }

}
