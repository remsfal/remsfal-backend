package de.remsfal.service.boundary;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

import de.remsfal.common.authentication.UnauthorizedException;
import de.remsfal.core.api.AuthenticationEndpoint;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.boundary.authentication.GoogleAuthenticator;
import de.remsfal.service.boundary.authentication.SessionManager;
import de.remsfal.service.control.UserController;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.jboss.logging.Logger;

import java.net.URI;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class AuthenticationResource implements AuthenticationEndpoint {

    // fix of issue https://github.com/quarkusio/quarkus/pull/8316
    @ConfigProperty(name = "quarkus.http.proxy.enable-forwarded-host", defaultValue = "false")
    public boolean enableForwardedHost;

    @Context
    UriInfo uri;

    @Context
    HttpHeaders headers;

    @Inject
    GoogleAuthenticator authenticator;

    @Inject
    SessionManager sessionManager;

    @Inject
    UserController controller;

    @Inject
    Logger logger;
    @Inject
    HttpHeaders httpHeaders;


    @Override
    @Timed(name = "checksTimerLogin", unit = MetricUnits.MILLISECONDS)
    public Response login(final String route) {
        final String redirectUri = getAbsoluteUri().toASCIIString().replace("/login", "/session");
        final URI redirectUrl = authenticator.getAuthorizationCodeURI(redirectUri, route);
        return redirect(redirectUrl).build();
    }

    @Override
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
        final UserModel user = controller.authenticateUser(payload.getSubject(), payload.getEmail().toLowerCase());
        return createSession(user, state);
    }

    private Response createSession(final UserModel user, final String route) {
        final URI redirectUri = getAbsoluteUriBuilder().replacePath(route).build();
        final NewCookie accessToken = sessionManager.generateAccessToken(
            sessionManager.sessionInfoBuilder(SessionManager.ACCESS_COOKIE_NAME).userId(user.getId())
                .userEmail(user.getEmail()).build());
        final NewCookie refreshToken = sessionManager.generateRefreshToken(user.getId(), user.getEmail());
        return redirect(redirectUri).cookie(accessToken, refreshToken).build();
    }

    @Timed(name = "checksTimerLogout", unit = MetricUnits.MILLISECONDS)
    @Counted(name = "countedLogout")
    @Override
    public Response logout() {
        final URI redirectUri = getAbsoluteUriBuilder().replacePath("/").build();
        sessionManager.logout(httpHeaders.getCookies());
        return redirect(redirectUri).cookie(sessionManager.removalCookie(SessionManager.ACCESS_COOKIE_NAME),
            sessionManager.removalCookie(SessionManager.REFRESH_COOKIE_NAME)).build();
    }

    private Response.ResponseBuilder redirect(final URI redirectUrl) {
        return Response.status(302).header("location", redirectUrl);
    }

    private URI getAbsoluteUri() {
        return getAbsoluteUriBuilder().build();
    }

    private UriBuilder getAbsoluteUriBuilder() {
        final String forwardedHostHeader = headers.getHeaderString("X-Forwarded-Host");
        if (enableForwardedHost && forwardedHostHeader != null) {
            logger.infov("Proxy is enabled. X-Forwarded-Host: {0}", forwardedHostHeader);
            final UriBuilder builder = uri.getAbsolutePathBuilder();
            final String[] parts = forwardedHostHeader.split(":");
            if (parts.length > 0) {
                logger.debugv("Host: {0}", parts[0]);
                builder.host(parts[0]);
            }
            if (parts.length > 1) {
                try {
                    logger.debugv("Port: {0}", parts[1]);
                    builder.port(Integer.parseUnsignedInt(parts[1]));
                } catch (NumberFormatException e) {
                    logger.errorv("Invalid port in X-Forwarded-Host header {0}", parts[1], e);
                }
            }
            return builder;
        }
        return uri.getAbsolutePathBuilder();
    }
}