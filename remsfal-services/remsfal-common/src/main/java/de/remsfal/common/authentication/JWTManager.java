package de.remsfal.common.authentication;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWTClaimsSet;

import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import io.smallrye.jwt.build.Jwt;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Date;

@Default
@ApplicationScoped
public class JWTManager {

    private static final Logger LOG = Logger.getLogger(JWTManager.class);

    @ConfigProperty(name = "de.remsfal.auth.jwt.issuer", defaultValue = "https://remsfal.online")
    private String issuer;

    @ConfigProperty(name = "de.remsfal.auth.jwt.private-key-location", defaultValue = "privateKey.pem")
    private String privateKeyLocation;

    @ConfigProperty(name = "de.remsfal.auth.jwt.public-key-location", defaultValue = "publicKey.pem")
    private String publicKeyLocation;

    @ConfigProperty(name = "de.remsfal.auth.jwt.jwks-url",
        defaultValue = "http://localhost:8080/api/v1/authentication/jwks")
    private String jwksUrl;

    @ConfigProperty(name = "de.remsfal.auth.jwt.key-id", defaultValue = "remsfal-platform-key")
    private String keyId;

    @ConfigProperty(name = "quarkus.http.port", defaultValue = "8080")
    private int httpPort;

    @ConfigProperty(name = "quarkus.http.host", defaultValue = "localhost")
    private String httpHost;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() throws IOException {
        loadPrivateKeyIfPresent();

        if (privateKey != null) {
            // If this service issues tokens, always use local public key.
            publicKey = loadLocalPublicKey();
            return;
        }

        if (jwksUrl != null && !jwksUrl.isBlank()) {
            if (isSelfUrl(jwksUrl)) {
                LOG.warn("JWKS URL points to this service; falling back to local public key.");
                publicKey = loadLocalPublicKey();
            } else {
                publicKey = loadPublicKeyFromJwks(jwksUrl);
            }
        } else {
            publicKey = loadLocalPublicKey();
        }
    }

    private void loadPrivateKeyIfPresent() {
        try {
            privateKey = KeyLoader.loadPrivateKey(privateKeyLocation);
        } catch (IllegalArgumentException | IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            privateKey = null;
        }
    }

    private PublicKey loadLocalPublicKey() {
        try {
            return KeyLoader.loadPublicKey(publicKeyLocation);
        } catch (IllegalArgumentException | IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOG.error("Failed to load local public key", e);
            return null;
        }
    }

    boolean isSelfUrl(String url) {
        try {
            URL parsed = new URL(url);
            int port = parsed.getPort() == -1 ? parsed.getDefaultPort() : parsed.getPort();
            String host = parsed.getHost();

            return port == httpPort && ("localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host) ||
                    "0.0.0.0".equals(host) || host.equalsIgnoreCase(httpHost)
                );
        } catch (MalformedURLException e) {
            return false;
        }
    }

    PublicKey loadPublicKeyFromJwks(String url) throws IOException {
        try {
            JWKSet jwkSet = JWKSet.load(new URL(url));
            if (jwkSet.getKeys().isEmpty()) {
                throw new IllegalArgumentException("No keys found in JWKS");
            }
            JWK jwk = jwkSet.getKeys().get(0);
            return jwk.toRSAKey().toPublicKey();
        } catch (ParseException | JOSEException e) {
            throw new IllegalArgumentException("Invalid JWKS", e);
        }
    }

    public String createJWT(SessionInfo sessionInfo) {
        long expirationTime = System.currentTimeMillis() / 1000 + sessionInfo.getExpireInSeconds();

        return Jwt.claims(sessionInfo.getClaims())
                .issuer(issuer)
                .expiresAt(expirationTime)
                .jws()
                .algorithm(SignatureAlgorithm.RS256)
                .header("typ", "JWT")
                .header("kid", keyId)
                .sign(privateKey);
    }

    public SessionInfo verifyJWT(String jwt) {
        return verifyJWT(jwt, false);
    }

    public SessionInfo verifyJWT(String jwt, boolean isRefreshToken) {
        JWTClaimsSet claimsSet = verifyTokenManually(jwt, publicKey);

        if (claimsSet == null) {
            throw new InvalidTokenException();
        }
        if (isRefreshToken && claimsSet.getClaim("refreshToken") == null) {
            throw new InvalidTokenException("Missing refresh token claim in token");
        }
        if (claimsSet.getExpirationTime().before(new Date())) {
            throw new TokenExpiredException(isRefreshToken ? "Refresh token expired" : "Access token expired");
        }
        SessionInfo sessionInfo = new SessionInfo(claimsSet);

        if (!sessionInfo.isValid()) {
            throw new InvalidTokenException();
        }

        return sessionInfo;
    }

    public JWTClaimsSet verifyTokenManually(String jwt, PublicKey publicKey) {
        try {
            JWSObject jwsObject = JWSObject.parse(jwt);
            RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;

            JWSVerifier verifier = new RSASSAVerifier(rsaPublicKey);

            if (jwsObject.verify(verifier)) {
                return JWTClaimsSet.parse(jwsObject.getPayload().toJSONObject());
            } else {
                throw new InvalidTokenException();
            }
        } catch (Exception e) {
            throw new InvalidTokenException(e);
        }
    }

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
