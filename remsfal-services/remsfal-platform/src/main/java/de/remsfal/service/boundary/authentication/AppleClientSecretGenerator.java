package de.remsfal.service.boundary.authentication;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import de.remsfal.common.authentication.KeyLoader;

/**
 * Generates the client secret for the Sign in with Apple REST API: a JWT signed with the private key of the
 * Apple developer account (ES256). A client secret is cached per client ID (Services ID or bundle ID) and
 * regenerated shortly before it expires. Apple accepts a lifetime of at most six months.
 */
@ApplicationScoped
public class AppleClientSecretGenerator {

    static final Duration MAX_TIMEOUT = Duration.ofDays(180);

    static final Duration RENEWAL_THRESHOLD = Duration.ofDays(1);

    @ConfigProperty(name = "de.remsfal.auth.apple.team-id")
    String teamId;

    @ConfigProperty(name = "de.remsfal.auth.apple.key-id")
    String keyId;

    @ConfigProperty(name = "de.remsfal.auth.apple.private-key-location")
    String privateKeyLocation;

    @ConfigProperty(name = "de.remsfal.auth.apple.client-secret.timeout", defaultValue = "P30D")
    Duration timeout;

    Clock clock = Clock.systemUTC();

    private final Map<String, ClientSecret> secrets = new ConcurrentHashMap<>();

    private volatile ECPrivateKey privateKey;

    record ClientSecret(String value, Instant expiresAt) {
    }

    /**
     * Returns a valid client secret for the given client ID.
     */
    public String getClientSecret(final String clientId) {
        final Instant now = clock.instant();
        return secrets.compute(clientId, (id, cached) -> cached != null
            && cached.expiresAt().minus(RENEWAL_THRESHOLD).isAfter(now) ? cached : generate(id, now)).value();
    }

    private ClientSecret generate(final String clientId, final Instant now) {
        final Duration lifetime = timeout.compareTo(MAX_TIMEOUT) > 0 ? MAX_TIMEOUT : timeout;
        final Instant expiresAt = now.plus(lifetime);
        final JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(keyId).build();
        final JWTClaimsSet claims = new JWTClaimsSet.Builder()
            .issuer(teamId)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(expiresAt))
            .audience(AppleIdTokenVerifier.ISSUER)
            .subject(clientId)
            .build();
        try {
            final SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(new ECDSASigner(getPrivateKey()));
            return new ClientSecret(jwt.serialize(), expiresAt);
        } catch (JOSEException e) {
            throw new IllegalStateException("Unable to sign the Sign in with Apple client secret", e);
        }
    }

    private ECPrivateKey getPrivateKey() {
        if (privateKey == null) {
            try {
                final PrivateKey key = KeyLoader.loadPrivateKey(privateKeyLocation, "EC");
                if (!(key instanceof ECPrivateKey ecKey)) {
                    throw new IllegalStateException("Sign in with Apple private key is not an EC key");
                }
                privateKey = ecKey;
            } catch (IOException | GeneralSecurityException e) {
                throw new IllegalStateException("Unable to load the Sign in with Apple private key", e);
            }
        }
        return privateKey;
    }

}
