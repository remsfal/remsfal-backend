package de.remsfal.chat.boundary.authentication;

import de.remsfal.common.authentication.JWTManager;
import de.remsfal.common.authentication.TokenExpiredException;
import de.remsfal.common.authentication.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Cookie;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;

@ApplicationScoped
public class SessionManager {

    public static final String ACCESS_COOKIE_NAME = "remsfal_access_token";

    @ConfigProperty(name = "de.remsfal.auth.session.cookie-path", defaultValue = "/")
    private String sessionCookiePath;

    @Inject
    private JWTManager jwtManager;

    /**
     * Decrypts the access token cookie and verifies the JWT.
     *
     * @param cookies Access token cookie to decrypt
     * @return SessionInfo containing the claims of the access token
     * @throws TokenExpiredException if the access token is expired
     * @throws UnauthorizedException if the access token is invalid
     */
    public SessionInfo checkValidUserSession(Map<String, Cookie> cookies) {
            Cookie cookie = findAccessTokenCookie(cookies);
            if (cookie == null) {
                throw new TokenExpiredException("No access token provided.");
            }
            return decryptAccessTokenCookie(cookie);
    }

    /**
     * Finds the access token cookie in the given map of cookies.
     *
     * @param cookies Map of cookies to search in
     * @return Cookie containing the access token or null if not found
     */
    private Cookie findAccessTokenCookie(final Map<String, Cookie> cookies) {
        if (cookies.containsKey(ACCESS_COOKIE_NAME)) {
            return cookies.get(ACCESS_COOKIE_NAME);
        }
        return null;
    }

    /**
     * Decrypts the access token cookie and verifies the JWT.
     *
     * @param cookie Access token cookie to decrypt
     * @return SessionInfo containing the claims of the access token
     * @throws TokenExpiredException if the access token is expired
     * @throws UnauthorizedException if the access token is invalid
     */
    private SessionInfo decryptAccessTokenCookie(Cookie cookie) {
        return jwtManager.verifyJWT(cookie.getValue());
    }

}
