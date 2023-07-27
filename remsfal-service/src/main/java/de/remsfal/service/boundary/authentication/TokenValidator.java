package de.remsfal.service.boundary.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import de.remsfal.core.dto.ImmutableUserJson;
import de.remsfal.core.model.UserModel;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

@ApplicationScoped
public class TokenValidator {
    private static final String CLIENT_ID = "821093255871-m7fg18oh8je55vaknjur9pgrh8sh4atb.apps.googleusercontent.com";

    public TokenInfo validate(final String authorizationHeader) {
        System.out.println("authHeader" + authorizationHeader);
        String idTokenString = authorizationHeader.replace("Bearer ", "");

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                String userId = payload.getSubject();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                final UserModel user = ImmutableUserJson.builder()
                        .id(userId)
                        .email(email)
                        .name(name)
                        .build();

                System.out.println("uname" +user.getEmail());

                return new TokenInfo(user);
            }
        } catch (GeneralSecurityException | IOException e) {
            System.out.println("Invalid ID token.");
        }

        return null;
    }



}
