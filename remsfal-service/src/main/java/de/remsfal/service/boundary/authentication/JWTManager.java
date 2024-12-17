package de.remsfal.service.boundary.authentication;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import de.remsfal.service.boundary.exception.TokenExpiredException;
import de.remsfal.service.boundary.exception.UnauthorizedException;
import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.eclipse.microprofile.jwt.JsonWebToken;
import io.smallrye.jwt.build.Jwt;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

@ApplicationScoped
public class JWTManager {

    @ConfigProperty(name = "de.remsfal.auth.jwt.issuer")
    private String issuer;

    @ConfigProperty(name = "de.remsfal.auth.jwt.private-key-location", defaultValue = "privateKey.pem")
    private String privateKeyLocation;

    @ConfigProperty(name = "de.remsfal.auth.jwt.public-key-location", defaultValue = "publicKey_old.pem")
    private String publicKeyLocation;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
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

        return Jwt.claims(sessionInfo.getClaims())
            .issuer(issuer)
            .expiresAt(expirationTime)
            .jws().algorithm(SignatureAlgorithm.RS256)
            .header("typ", "JWT")
            .sign(privateKey);
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
                throw new UnauthorizedException("Invalid token");
            }
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid token");
        }
    }

    public SessionInfo verifyJWT(String jwt, boolean isRefreshToken) {



            JWTClaimsSet claimsSet = verifyTokenManually(jwt, publicKey);

            if (claimsSet == null) {
                throw new UnauthorizedException("Invalid token");
            }
            if (isRefreshToken && claimsSet.getClaim("refreshToken") == null) {
                throw new UnauthorizedException("Missing refresh token claim in token");
            }
            if (claimsSet.getExpirationTime().before(new Date())) {
                throw new TokenExpiredException(isRefreshToken ? "Refresh token expired" : "Access token expired");
            }
            SessionInfo sessionInfo = new SessionInfo(claimsSet);

            if (!sessionInfo.isValid()) {
                throw new UnauthorizedException("Invalid token");
            }

            return sessionInfo;


    }

}
