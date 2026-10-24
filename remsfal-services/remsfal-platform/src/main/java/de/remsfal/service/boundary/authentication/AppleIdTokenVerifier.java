package de.remsfal.service.boundary.authentication;

import java.net.MalformedURLException;
import java.net.URI;
import java.text.ParseException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import de.remsfal.common.authentication.UnauthorizedException;

/**
 * Verifies JWTs issued by Sign in with Apple (ID tokens and server-to-server notifications) against Apple's
 * public keys (JWKS): signature, issuer, audience and expiration, and for ID tokens the nonce.
 */
@ApplicationScoped
public class AppleIdTokenVerifier {

    static final String ISSUER = "https://appleid.apple.com";

    static final String NONCE_CLAIM = "nonce";

    @ConfigProperty(name = "de.remsfal.auth.apple.auth-server-url", defaultValue = ISSUER)
    String authServerUrl;

    private final AtomicReference<JWKSource<SecurityContext>> jwkSource = new AtomicReference<>();

    public AppleIdTokenVerifier() {
        // CDI
    }

    /** Uses the given key source instead of Apple's JWKS endpoint, e.g. a mocked JWKS in tests. */
    AppleIdTokenVerifier(final JWKSource<SecurityContext> jwkSource) {
        this.jwkSource.set(jwkSource);
    }

    /**
     * Verifies an ID token.
     *
     * @param idToken  the serialized ID token
     * @param audience the expected audience (Services ID for the web flow, bundle ID for the native app)
     * @param nonce    the raw nonce; the token must carry its SHA-256 hash ({@link OAuthStateManager#hash(String)})
     * @return the verified claims
     * @throws UnauthorizedException if the token is invalid
     */
    public JWTClaimsSet verifyIdToken(final String idToken, final String audience, final String nonce) {
        if (idToken == null || idToken.isBlank()) {
            throw new UnauthorizedException("Missing Apple ID token");
        }
        if (nonce == null || nonce.isBlank()) {
            throw new UnauthorizedException("Missing nonce");
        }
        final JWTClaimsSet exactMatch = new JWTClaimsSet.Builder()
            .issuer(ISSUER)
            .claim(NONCE_CLAIM, OAuthStateManager.hash(nonce))
            .build();
        return process(idToken, Set.of(audience), exactMatch);
    }

    /**
     * Verifies the payload of a server-to-server notification.
     *
     * @param payload   the serialized, signed payload
     * @param audiences the accepted audiences (Services ID and bundle ID)
     * @return the verified claims
     * @throws UnauthorizedException if the payload is invalid
     */
    public JWTClaimsSet verifyNotification(final String payload, final Set<String> audiences) {
        if (payload == null || payload.isBlank()) {
            throw new UnauthorizedException("Missing notification payload");
        }
        final JWTClaimsSet exactMatch = new JWTClaimsSet.Builder().issuer(ISSUER).build();
        return process(payload, audiences, exactMatch);
    }

    private JWTClaimsSet process(final String token, final Set<String> audiences, final JWTClaimsSet exactMatch) {
        final DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
        processor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, getJwkSource()));
        processor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(audiences, exactMatch,
            Set.of(JWTClaimNames.SUBJECT, JWTClaimNames.EXPIRATION_TIME, JWTClaimNames.ISSUED_AT), null));
        try {
            return processor.process(token, null);
        } catch (ParseException | BadJOSEException | JOSEException e) {
            throw new UnauthorizedException("Invalid Apple token", e);
        }
    }

    private JWKSource<SecurityContext> getJwkSource() {
        return jwkSource.updateAndGet(source -> source != null ? source : createJwkSource());
    }

    private JWKSource<SecurityContext> createJwkSource() {
        try {
            return JWKSourceBuilder.<SecurityContext>create(URI.create(authServerUrl + "/auth/keys").toURL())
                .build();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid Sign in with Apple server URL: " + authServerUrl, e);
        }
    }

    /**
     * Reads a boolean claim that Apple sends either as boolean or as string ({@code "true"}).
     */
    static boolean booleanClaim(final JWTClaimsSet claims, final String name) {
        final Object value = claims.getClaim(name);
        if (value instanceof Boolean bool) {
            return bool;
        }
        return value != null && Boolean.parseBoolean(value.toString());
    }

}
