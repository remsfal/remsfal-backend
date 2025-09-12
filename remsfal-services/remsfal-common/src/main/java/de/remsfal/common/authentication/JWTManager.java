package de.remsfal.common.authentication;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import io.smallrye.jwt.build.Jwt;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

@Default
@ApplicationScoped
public class JWTManager {

    private static final Logger LOG = Logger.getLogger(JWTManager.class);

    @ConfigProperty(name = "de.remsfal.auth.jwt.issuer", defaultValue = "REMSFAL")
    String issuer;

    @ConfigProperty(name = "de.remsfal.auth.jwt.private-key-location", defaultValue = "privateKey.pem")
    String privateKeyLocation;

    @ConfigProperty(name = "de.remsfal.auth.jwt.public-key-location", defaultValue = "publicKey.pem")
    String publicKeyLocation;

    @ConfigProperty(name = "de.remsfal.auth.jwt.key-id", defaultValue = "remsfal-platform-key")
    String keyId;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() throws IOException {
        // If a private key is available, we are in issuer mode (platform)
        try {
            privateKey = KeyLoader.loadPrivateKey(privateKeyLocation);
        } catch (Exception e) {
            privateKey = null;
        }

        if (privateKey != null) {
            try {
                publicKey = KeyLoader.loadPublicKey(publicKeyLocation);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load local public key in issuer mode", e);
            }
            LOG.debug("JWTManager initialized in issuer mode (local keys loaded)");
        } else {
            // Verifier mode - SmallRye MP-JWT handles signature validation via configuration
            publicKey = null;
            LOG.info("JWTManager initialized in verifier mode; token verification is delegated to SmallRye MP-JWT");
        }
    }

    /** Issue an access token using SmallRye JWT Build (platform only) */
    public String createAccessToken(String userId, String email, String name, Boolean active,
            Map<String, String> projectRoles, long ttlSeconds) {
        ensureIssuerMode();
        long exp = (System.currentTimeMillis() / 1000) + ttlSeconds;

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("email", email);
        claims.put("name", name);
        claims.put("active", active);
        claims.put("project_roles", projectRoles);

        return Jwt.claims(claims)
                .issuer(issuer)
                .expiresAt(exp)
                .jws()
                .algorithm(SignatureAlgorithm.RS256)
                .header("typ", "JWT")
                .header("kid", keyId)
                .sign(privateKey);
    }

    /** Issue a refresh token using SmallRye JWT Build (platform only) */
    public String createRefreshToken(String userId, String email, String refreshTokenId, long ttlSeconds) {
        ensureIssuerMode();
        long exp = (System.currentTimeMillis() / 1000) + ttlSeconds;

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("email", email);
        claims.put("refreshToken", refreshTokenId);

        return Jwt.claims(claims)
                .issuer(issuer)
                .expiresAt(exp)
                .jws()
                .algorithm(SignatureAlgorithm.RS256)
                .header("typ", "JWT")
                .header("kid", keyId)
                .sign(privateKey);
    }

    private void ensureIssuerMode() {
        if (privateKey == null) {
            throw new IllegalStateException("This service does not own a signing key (issuer mode required)");
        }
    }

    /** JWKS exposure for consumers (platform only) */
    public RSAKey getPublicJwk() {
        RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
        return new RSAKey.Builder(rsaPublicKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID(keyId)
                .build();
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

}
