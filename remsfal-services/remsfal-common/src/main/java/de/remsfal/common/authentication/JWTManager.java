package de.remsfal.common.authentication;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

@Default
@ApplicationScoped
public class JWTManager {

    @ConfigProperty(name = "de.remsfal.auth.jwt.issuer", defaultValue = "https://remsfal.online")
    private String issuer;

    @ConfigProperty(name = "de.remsfal.auth.jwt.private-key-location", defaultValue = "privateKey.pem")
    private String privateKeyLocation;

    @ConfigProperty(name = "de.remsfal.auth.jwt.public-key-location", defaultValue = "publicKey.pem")
    private String publicKeyLocation;

    @ConfigProperty(name = "de.remsfal.auth.jwt.key-id", defaultValue = "remsfal-platform-key")
    private String keyId;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        privateKey = KeyLoader.loadPrivateKey(privateKeyLocation);
        publicKey = KeyLoader.loadPublicKey(publicKeyLocation);
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }


    public final String createJWT(SessionInfo sessionInfo) {

        long expirationTime = System.currentTimeMillis() / 1000 + sessionInfo.getExpireInSeconds();

        return Jwt.claims(sessionInfo.getClaims()).issuer(issuer).expiresAt(expirationTime).jws()
                .algorithm(SignatureAlgorithm.RS256).header("typ", "JWT").header("kid", keyId).sign(privateKey);
    }

    public SessionInfo verifyJWT(String jwt) {
        return verifyJWT(jwt, false);
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

    public RSAKey getPublicJwk() {
        RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
        return new RSAKey.Builder(rsaPublicKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID(keyId)
                .build();
    }

}
