package de.remsfal.service.boundary.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.nimbusds.jwt.JWTClaimsSet;

import de.remsfal.common.authentication.UnauthorizedException;

class AppleIdTokenVerifierTest {

    static final String AUDIENCE = "de.remsfal.test.web";

    static final String NONCE = "anyRandomNonce";

    private final AppleTestKeys keys = new AppleTestKeys();

    private final AppleIdTokenVerifier verifier = new AppleIdTokenVerifier(keys.jwkSource());

    @Test
    void verifyIdToken_SUCCESS_validToken() {
        final String token = keys.sign(AppleTestKeys.idTokenClaims(AUDIENCE, NONCE).build());

        final JWTClaimsSet claims = verifier.verifyIdToken(token, AUDIENCE, NONCE);

        assertEquals(AppleTestKeys.SUBJECT, claims.getSubject());
        assertEquals(AppleTestKeys.EMAIL, claims.getClaim("email"));
        assertTrue(AppleIdTokenVerifier.booleanClaim(claims, "email_verified"));
        assertTrue(AppleIdTokenVerifier.booleanClaim(claims, "is_private_email"));
    }

    @Test
    void verifyIdToken_SUCCESS_booleanClaims() {
        final String token = keys.sign(AppleTestKeys.idTokenClaims(AUDIENCE, NONCE)
            .claim("email_verified", true)
            .claim("is_private_email", false)
            .build());

        final JWTClaimsSet claims = verifier.verifyIdToken(token, AUDIENCE, NONCE);

        assertTrue(AppleIdTokenVerifier.booleanClaim(claims, "email_verified"));
        assertFalse(AppleIdTokenVerifier.booleanClaim(claims, "is_private_email"));
        assertFalse(AppleIdTokenVerifier.booleanClaim(claims, "missing_claim"));
    }

    @Test
    void verifyIdToken_FAILED_expiredToken() {
        final Instant past = Instant.now().minusSeconds(3600);
        final String token = keys.sign(AppleTestKeys.idTokenClaims(AUDIENCE, NONCE)
            .issueTime(Date.from(past.minusSeconds(600)))
            .expirationTime(Date.from(past))
            .build());

        assertThrows(UnauthorizedException.class, () -> verifier.verifyIdToken(token, AUDIENCE, NONCE));
    }

    @Test
    void verifyIdToken_FAILED_wrongAudience() {
        final String token = keys.sign(AppleTestKeys.idTokenClaims("de.remsfal.other", NONCE).build());

        assertThrows(UnauthorizedException.class, () -> verifier.verifyIdToken(token, AUDIENCE, NONCE));
    }

    @Test
    void verifyIdToken_FAILED_wrongIssuer() {
        final String token = keys.sign(AppleTestKeys.idTokenClaims(AUDIENCE, NONCE)
            .issuer("https://evil.example.org")
            .build());

        assertThrows(UnauthorizedException.class, () -> verifier.verifyIdToken(token, AUDIENCE, NONCE));
    }

    @Test
    void verifyIdToken_FAILED_foreignSigningKey() {
        final String token = AppleTestKeys.signWithForeignKey(AppleTestKeys.idTokenClaims(AUDIENCE, NONCE).build());

        assertThrows(UnauthorizedException.class, () -> verifier.verifyIdToken(token, AUDIENCE, NONCE));
    }

    @Test
    void verifyIdToken_FAILED_nonceMismatch() {
        final String token = keys.sign(AppleTestKeys.idTokenClaims(AUDIENCE, NONCE).build());

        assertThrows(UnauthorizedException.class, () -> verifier.verifyIdToken(token, AUDIENCE, "otherNonce"));
    }

    @Test
    void verifyIdToken_FAILED_rawNonceInToken() {
        final String token = keys.sign(AppleTestKeys.idTokenClaims(AUDIENCE, NONCE)
            .claim("nonce", NONCE)
            .build());

        assertThrows(UnauthorizedException.class, () -> verifier.verifyIdToken(token, AUDIENCE, NONCE));
    }

    @Test
    void verifyIdToken_FAILED_missingNonce() {
        final String token = keys.sign(AppleTestKeys.idTokenClaims(AUDIENCE, NONCE).build());

        assertThrows(UnauthorizedException.class, () -> verifier.verifyIdToken(token, AUDIENCE, null));
    }

    @Test
    void verifyIdToken_FAILED_malformedToken() {
        assertThrows(UnauthorizedException.class, () -> verifier.verifyIdToken("not-a-jwt", AUDIENCE, NONCE));
        assertThrows(UnauthorizedException.class, () -> verifier.verifyIdToken(null, AUDIENCE, NONCE));
    }

    @Test
    void verifyNotification_SUCCESS_anyAcceptedAudience() {
        final String payload = keys.sign(AppleTestKeys.notificationClaims("de.remsfal.test.app",
            Map.of("type", "email-disabled", "sub", AppleTestKeys.SUBJECT)).build());

        final JWTClaimsSet claims = verifier.verifyNotification(payload,
            Set.of(AUDIENCE, "de.remsfal.test.app"));

        assertTrue(claims.getClaim("events").toString().contains("email-disabled"));
    }

    @Test
    void verifyNotification_FAILED_wrongAudience() {
        final String payload = keys.sign(AppleTestKeys.notificationClaims("de.remsfal.other",
            Map.of("type", "email-disabled", "sub", AppleTestKeys.SUBJECT)).build());

        assertThrows(UnauthorizedException.class, () -> verifier.verifyNotification(payload, Set.of(AUDIENCE)));
    }

}
