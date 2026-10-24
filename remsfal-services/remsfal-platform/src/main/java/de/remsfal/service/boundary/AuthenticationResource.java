package de.remsfal.service.boundary;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.util.JSONObjectUtils;

import de.remsfal.common.authentication.JWTManager;
import de.remsfal.common.authentication.UnauthorizedException;
import de.remsfal.core.api.AuthenticationEndpoint;
import de.remsfal.core.json.AppleNotificationJson;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.boundary.authentication.AbstractAuthenticationResource;
import de.remsfal.service.boundary.authentication.AppleAuthenticator;
import de.remsfal.service.boundary.authentication.AppleAuthenticator.AppleNotification;
import de.remsfal.service.boundary.authentication.DevLoginResource;
import de.remsfal.service.boundary.authentication.ExternalIdentity;
import de.remsfal.service.boundary.authentication.GoogleAuthenticator;
import de.remsfal.service.boundary.authentication.OAuthStateManager;
import de.remsfal.service.boundary.authentication.SessionManager;
import de.remsfal.service.control.AuthorizationController;
import de.remsfal.service.control.UserController;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie.SameSite;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.micrometer.core.annotation.Timed;

import java.net.URI;
import java.text.ParseException;
import java.util.Map;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class AuthenticationResource extends AbstractAuthenticationResource implements AuthenticationEndpoint {

    static final String APPLE_APP_ID = "apple";

    @ConfigProperty(name = "de.remsfal.auth.dev-login.enabled", defaultValue = "false")
    public boolean devLoginEnabled;

    @Inject
    GoogleAuthenticator googleAuthenticator;

    @Inject
    AppleAuthenticator appleAuthenticator;

    @Inject
    OAuthStateManager stateManager;

    @Inject
    JWTManager jwtManager;

    @Inject
    AuthorizationController controller;

    @Inject
    UserController userController;

    @Override
    @Timed("checks_timer_login")
    public Response login(final String route) {
        return loginWithGoogle(route);
    }

    @Override
    @Timed("checks_timer_login")
    public Response loginWithGoogle(final String route) {
        if (devLoginEnabled) {
            return redirectToDevLogin(route);
        }
        final OAuthStateManager.State state = stateManager.create(sanitizeRoute(route));
        final URI redirectUrl = googleAuthenticator.getAuthorizationCodeURI(
            getCallbackUri("googleSession").toASCIIString(), state.value());
        return redirect(redirectUrl).cookie(state.cookie()).build();
    }

    @Override
    @Timed("checks_timer_login")
    public Response loginWithApple(final String route) {
        if (devLoginEnabled) {
            return redirectToDevLogin(route);
        }
        // Apple posts the authorization response cross-site, a Lax state cookie would not be sent
        final OAuthStateManager.State state = stateManager.create(sanitizeRoute(route), SameSite.NONE);
        final URI redirectUrl = appleAuthenticator.getAuthorizationCodeURI(
            getCallbackUri("appleSession").toASCIIString(), state.value(), state.nonce());
        return redirect(redirectUrl).cookie(state.cookie()).build();
    }

    private Response redirectToDevLogin(final String route) {
        final URI devLoginUrl = getAbsoluteUriBuilder()
            .replacePath(null)
            .path(DevLoginResource.class)
            .queryParam("route", route)
            .build();
        return redirect(devLoginUrl).build();
    }

    /**
     * @deprecated use {@link #googleSession(String, String, String)} instead
     */
    @Override
    @Deprecated(since = "1.0.15", forRemoval = true)
    @Timed("checks_timer_session")
    public Response session(final String code, final String state, final String error) {
        // the code exchange requires the redirect URI the code was obtained with
        return googleSession(code, state, error, getCallbackUri("session"));
    }

    @Override
    @Timed("checks_timer_session")
    public Response googleSession(final String code, final String state, final String error) {
        return googleSession(code, state, error, getCallbackUri("googleSession"));
    }

    private Response googleSession(final String code, final String state, final String error,
        final URI callbackUri) {
        if (error != null) {
            throw new UnauthorizedException("Error during Google authentication: " + error);
        }
        if (code == null) {
            throw new UnauthorizedException("Invalid authentication code");
        }
        final String route = sanitizeRoute(stateManager.validate(state, headers.getCookies()));
        final ExternalIdentity identity = googleAuthenticator.authenticate(code, callbackUri);
        return createSession(authenticate(identity), route, stateManager.removalCookie());
    }

    @Override
    @Timed("checks_timer_session")
    public Response appleSession(final String code, final String state, final String user, final String error) {
        if (error != null) {
            throw new UnauthorizedException("Error during Apple authentication: " + error);
        }
        if (code == null) {
            throw new UnauthorizedException("Invalid authentication code");
        }
        final String route = sanitizeRoute(stateManager.validate(state, headers.getCookies()));
        final String nonce = stateManager.nonce(headers.getCookies());
        final ExternalIdentity identity = appleAuthenticator.authenticate(code, getCallbackUri("appleSession"),
            nonce);
        final AppleUser appleUser = parseAppleUser(user);
        return createSession(authenticate(identity.withName(appleUser.firstName(), appleUser.lastName())), route,
            stateManager.removalCookie());
    }

    /**
     * Authenticates the user of an external identity and persists the data the provider only sends once (name on
     * the first authorization of Apple) and the refresh token required for a later revocation.
     */
    private UserModel authenticate(final ExternalIdentity identity) {
        final UserModel user = controller.authenticateUser(identity.provider(), identity.subject(),
            identity.email(), identity.emailVerified(), resolveLocale());
        if (identity.firstName() != null || identity.lastName() != null) {
            userController.applyProviderProfile(user.getId(), identity.firstName(), identity.lastName());
        }
        if (identity.refreshToken() != null) {
            userController.updateIdentityToken(identity.provider(), identity.subject(), identity.refreshToken(),
                identity.clientId());
        }
        return user;
    }

    private URI getCallbackUri(final String method) {
        return getAbsoluteUriBuilder()
            .replacePath(null)
            .path(AuthenticationEndpoint.class)
            .path(AuthenticationEndpoint.class, method)
            .build();
    }

    /** Name from the user form field Apple sends on the first authorization only. */
    record AppleUser(String firstName, String lastName) {
    }

    AppleUser parseAppleUser(final String user) {
        if (user == null || user.isBlank()) {
            return new AppleUser(null, null);
        }
        try {
            final Map<String, Object> name = JSONObjectUtils.getJSONObject(JSONObjectUtils.parse(user), "name");
            if (name == null) {
                return new AppleUser(null, null);
            }
            return new AppleUser(JSONObjectUtils.getString(name, "firstName"),
                JSONObjectUtils.getString(name, "lastName"));
        } catch (ParseException e) {
            logger.warn("Ignoring malformed user field of Apple authorization response", e);
            return new AppleUser(null, null);
        }
    }

    @Override
    @Timed("checks_timer_token")
    public Response token(final String appId, final String appToken, final Boolean devService,
        final String authorizationCode, final String nonce, final String givenName, final String familyName) {
        if (!APPLE_APP_ID.equals(appId)) {
            return Response.status(Response.Status.NOT_IMPLEMENTED).build();
        }
        final ExternalIdentity identity = appleAuthenticator
            .authenticateNative(appToken, authorizationCode, nonce)
            .withName(givenName, familyName);
        return Response.noContent().cookie(sessionCookies(authenticate(identity))).build();
    }

    @Override
    public Response appleNotification(final AppleNotificationJson notification) {
        final AppleNotification event;
        try {
            event = appleAuthenticator.parseNotification(notification.getPayload());
        } catch (UnauthorizedException e) {
            throw new BadRequestException("Invalid Apple notification", e);
        }
        userController.handleAppleNotification(event.type(), event.subject());
        return Response.ok().build();
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
