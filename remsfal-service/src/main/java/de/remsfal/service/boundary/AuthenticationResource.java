package de.remsfal.service.boundary;

import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

import de.remsfal.core.api.AuthenticationEndpoint;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.boundary.authentication.GoogleAuthenticator;
import de.remsfal.service.boundary.authentication.SessionInfo;
import de.remsfal.service.boundary.authentication.SessionManager;
import de.remsfal.service.boundary.exception.UnauthorizedException;
import de.remsfal.service.control.UserController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class AuthenticationResource implements AuthenticationEndpoint {

    @Context
    UriInfo uri;

    @Inject
    GoogleAuthenticator authenticator;

    @Inject
    SessionManager sessionManager;

    @Inject
    UserController controller;

    @Override
    public Response login(final String route) {
        final String redirectUri = uri.getAbsolutePath()
            .toASCIIString().replace("/login", "/session");
        final URI redirectUrl = authenticator.getAuthorizationCodeURI(redirectUri, route);
        return Response.temporaryRedirect(redirectUrl).build();
    }

    @Override
    public Response session(final String code, final String state, final String error) {
      if(error != null) {
        throw new UnauthorizedException("Error during Google authentication: " + error);
      }
      if(code == null) {
        throw new UnauthorizedException("Invalid authentication code");
      }
        final GoogleIdToken idToken = authenticator.getIdToken(code, uri.getAbsolutePath());
        if (idToken == null || idToken.getPayload() == null) {
            throw new ForbiddenException("Invalid ID token");
        }
        final Payload payload = idToken.getPayload();
        final UserModel user = controller.authenticateUser(payload.getSubject(), payload.getEmail().toLowerCase());
        return createSession(user, state);
    }

    private Response createSession(final UserModel user, final String route) {
        final URI redirectUri = uri.getAbsolutePathBuilder()
            .replacePath(route)
            .build();
        final SessionInfo sessionInfo = sessionManager.sessionInfoBuilder()
            .userId(user.getId())
            .userEmail(user.getEmail())
            .build();
        return Response.temporaryRedirect(redirectUri)
            .cookie(sessionManager.encryptSessionCookie(sessionInfo))
            .build();
    }

    @Override
    public Response logout() {
        final URI redirectUri = uri.getAbsolutePathBuilder()
            .replacePath("/")
            .build();
        return Response.temporaryRedirect(redirectUri)
            .cookie(sessionManager.removalSessionCookie())
            .build();
    }

}