package de.remsfal.service.boundary;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

import com.nimbusds.jose.jwk.JWKSet;

import de.remsfal.common.authentication.JWTManager;
import de.remsfal.common.authentication.UnauthorizedException;
import de.remsfal.core.api.AuthenticationEndpoint;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.boundary.authentication.AbstractAuthenticationResource;
import de.remsfal.service.boundary.authentication.DevLoginResource;
import de.remsfal.service.boundary.authentication.GoogleAuthenticator;
import de.remsfal.service.boundary.authentication.SessionManager;
import de.remsfal.service.control.AuthorizationController;
import de.remsfal.service.control.UserController;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.micrometer.core.annotation.Timed;

import java.net.URI;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class AuthenticationResource extends AbstractAuthenticationResource implements AuthenticationEndpoint {

    @ConfigProperty(name = "de.remsfal.auth.dev-login.enabled", defaultValue = "false")
    public boolean devLoginEnabled;

    @Inject
    GoogleAuthenticator authenticator;

    @Inject
    JWTManager jwtManager;

    @Inject
    AuthorizationController controller;

    @Inject
    UserController userController;

    @Override
    @Timed("checks_timer_login")
    public Response login(final String route) {
        if (devLoginEnabled) {
            final URI devLoginUrl = getAbsoluteUriBuilder()
                .replacePath(DevLoginResource.PATH)
                .queryParam("route", route)
                .build();
            return redirect(devLoginUrl).build();
        }
        final String redirectUri = getAbsoluteUri().toASCIIString().replace("/login", "/session");
        final URI redirectUrl = authenticator.getAuthorizationCodeURI(redirectUri, route);
        return redirect(redirectUrl).build();
    }

    @Override
    @Timed("checks_timer_session")
    public Response session(final String code, final String state, final String error) {
        if (error != null) {
            throw new UnauthorizedException("Error during Google authentication: " + error);
        }
        if (code == null) {
            throw new UnauthorizedException("Invalid authentication code");
        }
        final GoogleIdToken idToken = authenticator.getIdToken(code, getAbsoluteUri());
        if (idToken == null || idToken.getPayload() == null) {
            throw new ForbiddenException("Invalid ID token");
        }
        final Payload payload = idToken.getPayload();
        final UserModel user = controller.authenticateUser(
            payload.getSubject(), payload.getEmail().toLowerCase(), resolveLocale());
        return createSession(user, state);
    }

    @Override
    public Response token(final String appId, final String appToken, final Boolean devService) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @Timed("checks_timer_logout")
    @Override
    public Response logout() {
        final URI redirectUri = getAbsoluteUriBuilder().replacePath("/").build();
        sessionManager.logout(headers.getCookies());
        return redirect(redirectUri)
            .cookie(sessionManager.removalCookie(SessionManager.ACCESS_COOKIE_NAME),
                sessionManager.removalCookie(SessionManager.REFRESH_COOKIE_NAME))
            .header("Clear-Site-Data", "cookies")
            .build();
    }

    @Override
    @Timed("checks_timer_jwks")
    public Response jwks() {
        JWKSet jwkSet = new JWKSet(jwtManager.getPublicJwk());
        return Response.ok(jwkSet.toJSONObject()).build();
    }

    @Override
    public Response refresh(Cookie refreshCookie) {
        if (refreshCookie == null) {
            throw new UnauthorizedException("No refresh token provided.");
        }
        SessionManager.TokenRenewalResponse response = sessionManager
            .renewTokens(refreshCookie);
        return Response.noContent()
            .cookie(response.getAccessToken(), response.getRefreshToken())
            .build();
    }

    @Override
    public Response verifyAdditionalEmail(final String token) {
        userController.verifyAdditionalEmail(token);
        return Response.noContent().build();
    }

}
