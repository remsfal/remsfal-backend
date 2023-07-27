package de.remsfal.service.control;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import de.remsfal.service.boundary.authentication.TokenInfo;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Map;

@ApplicationScoped

public class AuthController {

    public String generateJWT(TokenInfo user) {
        Algorithm algorithm = Algorithm.HMAC256("secret");
        String name = "";
        if(user.getName() != null){
            name = user.getName();
        }
        String token = JWT.create()
                .withIssuer("remsfal")
                .withClaim("name", name)
                .withClaim("email", user.getEmail())
                .withClaim("sub", user.getId())
                .sign(algorithm);
        System.out.println(token);
        return token;
    }

    public DecodedJWT getDecodedJWT(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256("secret");
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("remsfal")
                    .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);
            return jwt;
        } catch (JWTVerificationException exception){
            // Invalid signature/claims
            System.out.println("Invalid token");
            return null;
        }
    }

}
