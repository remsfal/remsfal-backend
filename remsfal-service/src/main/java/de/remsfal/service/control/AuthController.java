package de.remsfal.service.control;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
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
        String token = JWT.create()
                .withIssuer("remsfal")
                .withClaim("name", user.getName())
                .withClaim("email", user.getEmail())
                .withClaim("sub", user.getId())
                .sign(algorithm);
        System.out.println(token);
        return token;
    }

    public ArrayList<Claim> getClaimsFromJWT(String token) {
        // Decode the token
        DecodedJWT jwt = JWT.decode(token);

        // Get the claims
        Map<String, Claim> claims = jwt.getClaims();

        // Convert the claims to an ArrayList
        ArrayList<Claim> claimList = new ArrayList<>(claims.values());

        return claimList;
    }
}
