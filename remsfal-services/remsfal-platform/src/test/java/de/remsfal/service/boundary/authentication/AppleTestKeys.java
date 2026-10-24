package de.remsfal.service.boundary.authentication;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * A mocked Apple JWKS and a signer for tokens as issued by Sign in with Apple.
 */
public final class AppleTestKeys {

    public static final String SUBJECT = "001234.abcdef0123456789.1234";

    public static final String EMAIL = "apple-user@privaterelay.appleid.com";

    private final RSAKey key;

    public AppleTestKeys() {
        try {
            key = new RSAKeyGenerator(2048).keyID(UUID.randomUUID().toString()).generate();
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

    public JWKSource<SecurityContext> jwkSource() {
        return new ImmutableJWKSet<>(new JWKSet(key.toPublicJWK()));
    }

    /** Claims of a valid ID token for the given audience and raw nonce. */
    public static JWTClaimsSet.Builder idTokenClaims(final String audience, final String nonce) {
        final Instant now = Instant.now();
        return new JWTClaimsSet.Builder()
            .issuer(AppleIdTokenVerifier.ISSUER)
            .audience(audience)
            .subject(SUBJECT)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(600)))
            .claim("nonce", OAuthStateManager.hash(nonce))
            .claim("email", EMAIL)
            .claim("email_verified", "true")
            .claim("is_private_email", "true");
    }

    /** Claims of a server-to-server notification with the given event. */
    public static JWTClaimsSet.Builder notificationClaims(final String audience, final Map<String, Object> events) {
        final Instant now = Instant.now();
        return new JWTClaimsSet.Builder()
            .issuer(AppleIdTokenVerifier.ISSUER)
            .audience(audience)
            .subject(SUBJECT)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(600)))
            .claim("jti", UUID.randomUUID().toString())
            .claim("events", toJson(events));
    }

    public String sign(final JWTClaimsSet claims) {
        return sign(claims, key);
    }

    /** Signs with a key that is not part of the JWKS. */
    public static String signWithForeignKey(final JWTClaimsSet claims) {
        try {
            return sign(claims, new RSAKeyGenerator(2048).keyID(UUID.randomUUID().toString()).generate());
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String sign(final JWTClaimsSet claims, final RSAKey signingKey) {
        try {
            final SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(signingKey.getKeyID()).build(), claims);
            jwt.sign(new RSASSASigner(signingKey));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String toJson(final Map<String, Object> events) {
        try {
            return new ObjectMapper().writeValueAsString(events);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

}
