package de.remsfal.service.boundary.authentication;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import de.remsfal.common.authentication.JWTManager;
import de.remsfal.common.authentication.UnauthorizedException;
import de.remsfal.core.model.UserAuthenticationModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.control.AuthorizationController;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.NewCookie.SameSite;

@ApplicationScoped
public class SessionManager {

    public static final String ACCESS_COOKIE_NAME = "remsfal_access_token";
    public static final String REFRESH_COOKIE_NAME = "remsfal_refresh_token";

    // Token renewal threshold: renew access token if it expires in less than 5 minutes
    private static final long TOKEN_RENEWAL_THRESHOLD_SECONDS = 300;

    @ConfigProperty(name = "de.remsfal.auth.session.cookie-same-site", defaultValue = "STRICT")
    SameSite sessionCookieSameSite;

    @ConfigProperty(name = "de.remsfal.auth.session.cookie-secure", defaultValue = "true")
    boolean sessionCookieSecure;

    @ConfigProperty(name = "de.remsfal.auth.access-token.cookie-path", defaultValue = "/")
    String accessTokenCookiePath;

    // TODO: Change to authentication endpoint path
    @ConfigProperty(name = "de.remsfal.auth.refresh-token.cookie-path", defaultValue = "/api")
    String refreshTokenCookiePath;

    @ConfigProperty(name = "de.remsfal.auth.access-token.timeout", defaultValue = "PT25M")
    Duration accessTokenTimeout;

    @ConfigProperty(name = "de.remsfal.auth.refresh-token.timeout", defaultValue = "P7D")
    Duration refreshTokenTimeout;

    @Inject
    JWTManager jwtManager;

    @Inject
    JWTParser jwtParser;

    @Inject
    AuthorizationController controller;

    /**
     * Generates a new access token for the given user authentication entity.
     *
     * @param userAuth User authentication entity to generate the access token for
     * @return NewCookie containing the access token
     */
    public NewCookie generateAccessToken(final UserAuthenticationModel userAuth) {
        return generateAccessToken(userAuth.getId(), userAuth.getEmail());
    }

    /**
     * Generates a signed, short-lived access token and wraps it into the access cookie.
     *
     * @param userId User ID to generate the access token for (subject claim)
     * @param email  User email to generate the access token for (email claim)
     * @return NewCookie containing the access token
     */
    public NewCookie generateAccessToken(final UUID userId, final String email) {
        if (userId == null || email == null) {
            throw new UnauthorizedException("User id and email are required");
        }

        UserModel user = controller.getAuthenticatedUser(userId);

        Map<String, String> projectRoles = controller.getProjectAuthorization(userId);
        Map<String, String> tenancyProjects = controller.getTenancyAuthorization(userId);

        String jwt = jwtManager.createAccessToken(user, projectRoles, tenancyProjects,
                accessTokenTimeout.getSeconds());
        return buildCookie(ACCESS_COOKIE_NAME, jwt, (int) accessTokenTimeout.getSeconds(), false,
                accessTokenCookiePath);
    }

    /**
     * Generates a signed, longer-lived refresh token and wraps it into the refresh cookie.
     * A refresh token identifier is stored in the database and embedded as refresh token claim in the JWT.
     * The refresh token will be used to generate a new access token by matching the stored identifier and the claim.
     *
     * @param userId    User ID to generate the refresh token for (subject claim)
     * @param userEmail User email to generate the refresh token for (email claim)
     * @return NewCookie containing the refresh token
     */
    @Transactional
    public NewCookie generateRefreshToken(final UUID userId, final String userEmail) {
        UUID newRefreshTokenId = UUID.randomUUID();

        if (controller.hasUserRefreshToken(userId)) {
            controller.createRefreshToken(userId, newRefreshTokenId);
        } else {
            controller.updateExistingRefreshToken(userId, newRefreshTokenId);
        }

        String jwt = jwtManager.createRefreshToken(userId, userEmail, newRefreshTokenId.toString(),
                refreshTokenTimeout.getSeconds());
        return buildCookie(REFRESH_COOKIE_NAME, jwt, (int) refreshTokenTimeout.getSeconds(), true,
                refreshTokenCookiePath);
    }

    /**
     * Renews the access and refresh tokens for the given refresh token cookie.
     *
     * @param cookies Map of cookies containing the refresh token
     * @return TokenRenewalResponse containing the new access and refresh tokens
     */
    @Transactional
    public TokenRenewalResponse renewTokens(final Cookie refreshCookie) {
        if (refreshCookie == null) {
            throw new UnauthorizedException("No refresh token provided.");
        }

        JsonWebToken refreshJwt = parseRefreshToken(refreshCookie.getValue());
        UUID userId = UUID.fromString(refreshJwt.getSubject());
        String email = refreshJwt.getClaim("email");
        UUID refreshId = UUID.fromString(refreshJwt.getClaim("refreshTokenId"));

        UserAuthenticationModel userAuth = controller.requireValidRefreshToken(userId, refreshId);

        NewCookie newAccess = generateAccessToken(userAuth);
        NewCookie newRefresh = generateRefreshToken(userId, email);
        return new TokenRenewalResponse(newAccess, newRefresh);
    }

    /** Parses and validates the given refresh token using SmallRye JWT */
    private JsonWebToken parseRefreshToken(String token) {
        try {
            // Validates token signature, issuer and exp using platform’s MP-JWT configuration
            return jwtParser.parse(token);
        } catch (ParseException e) {
            throw new UnauthorizedException("Invalid refresh token.", e);
        }
    }

    /** Builds a new cookie with the given parameters */
    private NewCookie buildCookie(final String name, final String value, int maxAge, boolean httpOnly,
                                  String cookiePath) {
        return new NewCookie.Builder(name).value(value).path(cookiePath + getSameSiteWorkaround())
                .httpOnly(httpOnly).secure(sessionCookieSecure).maxAge(maxAge).build();
    }

    @Transactional
    public void logout(final Map<String, Cookie> cookies) {
        Cookie refreshCookie = cookies.get(REFRESH_COOKIE_NAME);

        if (refreshCookie != null) {
            JsonWebToken refresh = parseRefreshToken(refreshCookie.getValue());
            UUID userId = UUID.fromString(refresh.getSubject());
            controller.deleteRefreshToken(userId);
        }
    }

    /**
     * Removes the given cookie from the client.
     *
     * @param cookieName Name of the cookie to remove
     * @return NewCookie to remove the cookie
     */
    public NewCookie removalCookie(String cookieName) {
        String cookiePath = ACCESS_COOKIE_NAME.equals(cookieName) ? accessTokenCookiePath : refreshTokenCookiePath;
        return new NewCookie.Builder(cookieName).value("").path(cookiePath + getSameSiteWorkaround())
                // sameSite is currently not supported
                .sameSite(sessionCookieSameSite).maxAge(0).build();
    }

    /**
     * Finds the access token cookie in the given map of cookies.
     *
     * @param cookies Map of cookies to search in
     * @return Cookie containing the access token or null if not found
     */
    public Cookie findAccessTokenCookie(final Map<String, Cookie> cookies) {
        if (cookies.containsKey(ACCESS_COOKIE_NAME)) {
            return cookies.get(ACCESS_COOKIE_NAME);
        }
        return null;
    }

    /**
     * Finds the refresh token cookie in the given map of cookies.
     *
     * @param cookies Map of cookies to search in
     * @return Cookie containing the refresh token or null if not found
     */
    public Cookie findRefreshTokenCookie(final Map<String, Cookie> cookies) {
        if (cookies.containsKey(REFRESH_COOKIE_NAME)) {
            return cookies.get(REFRESH_COOKIE_NAME);
        }
        return null;
    }

    /**
     * Checks if the access token needs renewal based on its expiration time.
     * A token needs renewal if it expires in less than 5 minutes.
     *
     * @param accessToken The access token cookie to check
     * @return true if the token needs renewal, false otherwise
     */
    public boolean needsRenewal(final Cookie accessToken) {
        if (accessToken == null) {
            return true;
        }

        try {
            JsonWebToken jwt = jwtParser.parse(accessToken.getValue());
            long expirationTime = jwt.getExpirationTime();
            long currentTime = System.currentTimeMillis() / 1000;
            long timeUntilExpiration = expirationTime - currentTime;

            // Renew if token expires in less than the threshold (5 minutes)
            return timeUntilExpiration < TOKEN_RENEWAL_THRESHOLD_SECONDS;
        } catch (ParseException e) {
            // If token is invalid or cannot be parsed, it needs renewal
            return true;
        }
    }

    private String getSameSiteWorkaround() {
        // see: https://github.com/jakartaee/rest/issues/862
        return ";SameSite=" + sessionCookieSameSite.name().substring(0, 1).toUpperCase() +
                sessionCookieSameSite.name().substring(1).toLowerCase();
    }

    /**
     * Response containing the new access and refresh tokens.
     *
     * @see SessionManager#renewTokens(Map)
     */
    public static class TokenRenewalResponse {
        private final NewCookie accessToken;
        private final NewCookie refreshToken;

        public TokenRenewalResponse(NewCookie accessToken, NewCookie refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public NewCookie getAccessToken() {
            return accessToken;
        }

        public NewCookie getRefreshToken() {
            return refreshToken;
        }
    }

}