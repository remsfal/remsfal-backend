package de.remsfal.service.boundary.authentication;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import de.remsfal.service.boundary.exception.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@ApplicationScoped
public class TokenValidator {
    private static final String CLIENT_ID = "821093255871-m7fg18oh8je55vaknjur9pgrh8sh4atb.apps.googleusercontent.com";
    
    /**
     * For maximum efficiency, a single globally-shared instance of the HTTP transport.
     */
    private HttpTransport transport = new NetHttpTransport.Builder().build();
    
    /**
     * For maximum efficiency, a single globally-shared instance of the JSON factory.
     */
    private JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    public TokenInfo validate(final String authorizationHeader) {
        String idTokenString = authorizationHeader.replace("Bearer ", "");

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                return new TokenInfo(idToken.getPayload());
            } else {
                return null;
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new UnauthorizedException("Unable to verify signature", e);
        }
    }
}
