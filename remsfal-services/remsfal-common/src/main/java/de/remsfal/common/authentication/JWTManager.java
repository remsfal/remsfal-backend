package de.remsfal.common.authentication;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

import de.remsfal.core.model.UserModel;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.Startup;
import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Default
@Startup // fail fast: a missing signing key must stop the start, not the first login
@ApplicationScoped
public class JWTManager {

    private static final Logger LOG = Logger.getLogger(JWTManager.class);

    private static final String DOCUMENTATION_HINT =
        "See remsfal-services/remsfal-platform/README.md#jwt-token";

    @ConfigProperty(name = "de.remsfal.auth.jwt.issuer", defaultValue = "REMSFAL")
    String issuer;

    /** Only the service that issues tokens (platform) owns a signing key. */
    @ConfigProperty(name = "de.remsfal.auth.jwt.signing.enabled", defaultValue = "false")
    boolean signingEnabled;

    @ConfigProperty(name = "de.remsfal.auth.jwt.private-key-location")
    Optional<String> privateKeyLocation;

    /** Directory for the automatically generated key in dev mode. */
    @ConfigProperty(name = "de.remsfal.auth.jwt.dev-key-directory", defaultValue = ".dev-keys")
    String devKeyDirectory;

    @ConfigProperty(name = "de.remsfal.auth.jwt.key-id", defaultValue = "remsfal-platform-key")
    String keyId;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        init(LaunchMode.current());
    }

    void init(final LaunchMode launchMode) {
        if (!signingEnabled) {
            // Verifier mode - SmallRye MP-JWT handles signature validation via configuration
            privateKey = null;
            publicKey = null;
            LOG.info("JWTManager initialized in verifier mode; token verification is delegated to SmallRye MP-JWT");
            return;
        }
        try {
            if (privateKeyLocation.filter(location -> !location.isBlank()).isPresent()) {
                privateKey = KeyLoader.loadPrivateKey(privateKeyLocation.get());
                LOG.infov("JWTManager initialized in issuer mode (key loaded from {0})", privateKeyLocation.get());
            } else if (launchMode == LaunchMode.DEVELOPMENT) {
                privateKey = loadOrGenerateDevKey(Path.of(devKeyDirectory));
            } else {
                throw new IllegalStateException("No JWT signing key configured. Set "
                    + "de.remsfal.auth.jwt.private-key-location (e.g. DE_REMSFAL_AUTH_JWT_PRIVATE_KEY_LOCATION="
                    + "file:/run/secrets/remsfal_jwt_private_key). " + DOCUMENTATION_HINT);
            }
            publicKey = KeyLoader.derivePublicKey(privateKey);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load JWT signing key: " + e.getMessage()
                + ". " + DOCUMENTATION_HINT, e);
        }
    }

    private static PrivateKey loadOrGenerateDevKey(final Path directory) throws Exception {
        final Path keyFile = directory.resolve(KeyLoader.PRIVATE_KEY_FILE_NAME).toAbsolutePath();
        if (Files.exists(keyFile)) {
            LOG.infov("JWTManager initialized in issuer mode (dev key loaded from {0})", keyFile);
            return KeyLoader.loadPrivateKey("file:" + keyFile);
        }
        LOG.warnv("No JWT signing key configured, generated a new dev key at {0} - "
            + "never use generated keys in production!", keyFile);
        return KeyLoader.generateAndStore(directory);
    }

    /** Issue an access token using SmallRye JWT Build (platform only) */
    public String createAccessToken(final UserModel user, final Map<String, String> projectRoles,
        final Map<String, String> organizationRoles, final Map<String, String> tenancyProjects,
        final long ttlSeconds) {
        ensureIssuerMode();
        long exp = (System.currentTimeMillis() / 1000) + ttlSeconds;


        JwtClaimsBuilder builder = Jwt
            .subject(user.getId().toString())
            .claim("email", user.getEmail())
            .claim("active", user.isActive())
            .claim("project_roles", projectRoles)
            .claim("organization_roles", organizationRoles)
            .claim("tenancy_projects", tenancyProjects)
            .issuer(issuer)
            .expiresAt(exp);

        if (user.getName() != null) {
            builder.claim("name", user.getName());
        }

        return builder
                .jws()
                .algorithm(SignatureAlgorithm.RS256)
                .header("typ", "JWT")
                .header("kid", keyId)
                .sign(privateKey);
    }

    /** Issue a refresh token using SmallRye JWT Build (platform only) */
    public String createRefreshToken(final UUID userId, final String email,
        final String refreshTokenId, final long ttlSeconds) {
        ensureIssuerMode();
        long exp = (System.currentTimeMillis() / 1000) + ttlSeconds;

        return Jwt
            .subject(userId.toString())
            .claim("email", email)
            .claim("refreshTokenId", refreshTokenId)
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
