package de.remsfal.service.boundary.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

class AppleClientSecretGeneratorTest {

    static final String CLIENT_ID = "de.remsfal.test.web";

    static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private final AppleClientSecretGenerator generator = new AppleClientSecretGenerator();

    private ECPublicKey publicKey;

    @BeforeEach
    void setUp(@TempDir final Path directory) throws Exception {
        final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"));
        final KeyPair keyPair = keyPairGenerator.generateKeyPair();
        publicKey = (ECPublicKey) keyPair.getPublic();
        final Path file = directory.resolve("AuthKey_TESTKEYID.p8");
        Files.writeString(file, "-----BEGIN PRIVATE KEY-----\n"
            + Base64.getMimeEncoder().encodeToString(keyPair.getPrivate().getEncoded())
            + "\n-----END PRIVATE KEY-----\n");

        generator.teamId = "TESTTEAMID";
        generator.keyId = "TESTKEYID";
        generator.privateKeyLocation = "file:" + file;
        generator.timeout = Duration.ofDays(30);
        generator.clock = Clock.fixed(NOW, ZoneOffset.UTC);
    }

    @Test
    void getClientSecret_SUCCESS_signedJwtWithAppleClaims() throws Exception {
        final SignedJWT jwt = SignedJWT.parse(generator.getClientSecret(CLIENT_ID));

        assertEquals(JWSAlgorithm.ES256, jwt.getHeader().getAlgorithm());
        assertEquals("TESTKEYID", jwt.getHeader().getKeyID());
        assertTrue(jwt.verify(new ECDSAVerifier(publicKey)));

        final JWTClaimsSet claims = jwt.getJWTClaimsSet();
        assertEquals("TESTTEAMID", claims.getIssuer());
        assertEquals(CLIENT_ID, claims.getSubject());
        assertEquals(List.of("https://appleid.apple.com"), claims.getAudience());
        assertEquals(NOW, claims.getIssueTime().toInstant());
        assertEquals(NOW.plus(Duration.ofDays(30)), claims.getExpirationTime().toInstant());
    }

    @Test
    void getClientSecret_SUCCESS_lifetimeIsLimitedToSixMonths() throws Exception {
        generator.timeout = Duration.ofDays(365);

        final JWTClaimsSet claims = SignedJWT.parse(generator.getClientSecret(CLIENT_ID)).getJWTClaimsSet();

        assertEquals(NOW.plus(AppleClientSecretGenerator.MAX_TIMEOUT), claims.getExpirationTime().toInstant());
    }

    @Test
    void getClientSecret_SUCCESS_cachedPerClientId() throws Exception {
        final String secret = generator.getClientSecret(CLIENT_ID);

        assertEquals(secret, generator.getClientSecret(CLIENT_ID));
        final String other = generator.getClientSecret("de.remsfal.test.app");
        assertNotEquals(secret, other);
        assertEquals("de.remsfal.test.app", SignedJWT.parse(other).getJWTClaimsSet().getSubject());
    }

    @Test
    void getClientSecret_SUCCESS_regeneratedBeforeExpiry() {
        final String secret = generator.getClientSecret(CLIENT_ID);

        generator.clock = Clock.fixed(NOW.plus(Duration.ofDays(28)), ZoneOffset.UTC);
        assertEquals(secret, generator.getClientSecret(CLIENT_ID));

        generator.clock = Clock.fixed(NOW.plus(Duration.ofDays(29)).plusSeconds(1), ZoneOffset.UTC);
        assertNotEquals(secret, generator.getClientSecret(CLIENT_ID));
    }

}
