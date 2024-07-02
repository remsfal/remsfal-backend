package de.remsfal.service.boundary.authentication;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import de.remsfal.service.boundary.exception.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.UriBuilder;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class GoogleAuthenticator {

    @ConfigProperty(name = "de.remsfal.auth.oidc.auth-server-url",
            defaultValue = GoogleOAuthConstants.AUTHORIZATION_SERVER_URL)
    private String authServerURL;

    @ConfigProperty(name = "de.remsfal.auth.oidc.response-type", defaultValue = "code")
    private String authResponseType;

    @ConfigProperty(name = "de.remsfal.auth.oidc.scopes", defaultValue = "openid email")
    private String authScopes;

    @ConfigProperty(name = "de.remsfal.auth.oidc.client-id")
    private String authClientId;

    @ConfigProperty(name = "de.remsfal.auth.oidc.client-secret")
    private String authClientSecret;

    /**
     * For maximum efficiency, a single globally-shared instance of the HTTP transport.
     */
    private final HttpTransport transport = new NetHttpTransport.Builder().build();

    /**
     * For maximum efficiency, a single globally-shared instance of the JSON factory.
     */
    private final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    /**
     * For maximum efficiency, a single globally-shared instance of the GoogleIdTokenVerifier.
     */
    private final GoogleIdTokenVerifier tokenVerifier = new GoogleIdTokenVerifier
        .Builder(transport, jsonFactory)
        .build();

    public URI getAuthorizationCodeURI(final String redirectUri, final String state) {
        return UriBuilder.fromUri(authServerURL)
            .queryParam("response_type", authResponseType)
            .queryParam("client_id", authClientId)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("scope", authScopes)
            .queryParam("state", state)
            .build();
    }

    public GoogleIdToken getIdToken(final String code, final URI redirectUri) {
        try {
            final GoogleTokenResponse response =
                new GoogleAuthorizationCodeTokenRequest(transport, jsonFactory,
                    authClientId, authClientSecret, code, redirectUri.toASCIIString())
                        .execute();
            return verifyIdToken(response.getIdToken());
        } catch (IOException e) {
            throw new ForbiddenException("Unable to extract ID token", e);
        }
    }

    private GoogleIdToken verifyIdToken(final String idToken) throws IOException {
        try {
            if (idToken == null) {
                throw new ForbiddenException("Unable to retrieve ID token");
            }
            return tokenVerifier.verify(idToken);
        } catch (GeneralSecurityException e) {
            throw new UnauthorizedException("Unable to verify signature", e);
        }
    }

}
