package de.remsfal.service.boundary.authentication;

import de.remsfal.core.authentication.JWTManager;
import de.remsfal.core.authentication.SessionInfo;
import de.remsfal.core.authentication.TokenExpiredException;
import de.remsfal.core.authentication.UnauthorizedException;
import de.remsfal.core.model.UserAuthenticationModel;
import de.remsfal.service.entity.dao.UserAuthenticationRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.UserAuthenticationEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.NewCookie.SameSite;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SessionManager {

    public static final String ACCESS_COOKIE_NAME = "remsfal_access_token";
    public static final String REFRESH_COOKIE_NAME = "remsfal_refresh_token";

    private final String sessionCookiePath;

    private final SameSite sessionCookieSameSite;

    private final Duration accessTokenTimeout;

    private final Duration refreshTokenTimeout;

    private final JWTManager jwtManager;

    private final UserAuthenticationRepository userAuthRepository;

    private final UserRepository userRepository;

    public SessionManager(
        @ConfigProperty(name = "de.remsfal.auth.session.cookie-path", defaultValue = "/") String sessionCookiePath,
        @ConfigProperty(name = "de.remsfal.auth.session.cookie-same-site", defaultValue = "STRICT")
        SameSite sessionCookieSameSite,
        @ConfigProperty(name = "de.remsfal.auth.access-token.timeout", defaultValue = "PT5M")
        Duration accessTokenTimeout,
        @ConfigProperty(name = "de.remsfal.auth.refresh-token.timeout", defaultValue = "P7D")
        Duration refreshTokenTimeout, JWTManager jwtManager, UserAuthenticationRepository userAuthRepository,
        UserRepository userRepository) {
        this.sessionCookiePath = sessionCookiePath;
        this.sessionCookieSameSite = sessionCookieSameSite;
        this.accessTokenTimeout = accessTokenTimeout;
        this.refreshTokenTimeout = refreshTokenTimeout;
        this.jwtManager = jwtManager;
        this.userAuthRepository = userAuthRepository;
        this.userRepository = userRepository;
    }

    /**
     * Generates a new access token for the given session info.
     *
     * @param sessionInfo Session info to generate the access token for
     * @return NewCookie containing the access token
     */
    public NewCookie generateAccessToken(SessionInfo sessionInfo) {
        String jwt =
            jwtManager.createJWT(SessionInfo.builder().from(sessionInfo).expireAfter(accessTokenTimeout).build());
        return buildCookie(ACCESS_COOKIE_NAME, jwt, (int) accessTokenTimeout.getSeconds(), false);
    }

    /**
     * Generates a new access token for the given user authentication entity.
     *
     * @param userAuth User authentication entity to generate the access token for
     * @return NewCookie containing the access token
     */
    public NewCookie generateAccessToken(UserAuthenticationModel userAuth) {
        return generateAccessToken(SessionInfo.builder().from(userAuth).build());
    }

    /**
     * Generates a new refresh token for the given user ID and email. The refresh token is stored in the database.
     * The refresh token is also stored in the JWT. The refresh token will be used to generate a new access token.
     *
     * @param userId    User ID to generate the refresh token for
     * @param userEmail User email to generate the refresh token for
     * @return NewCookie containing the refresh token
     */
    @Transactional
    public NewCookie generateRefreshToken(String userId, String userEmail) {
        String refreshToken = UUID.randomUUID().toString();
        if (isNewUserAuthenticationEntity(userId)) {
            createNewUserAuthentication(userId, refreshToken);
        } else {
            updateExistingRefreshToken(userId, refreshToken);
        }
        String jwt = createRefreshTokenJWT(userId, userEmail, refreshToken);
        return buildCookie(REFRESH_COOKIE_NAME, jwt, (int) refreshTokenTimeout.getSeconds(), true);
    }

    private boolean isNewUserAuthenticationEntity(String userId) {
        return userAuthRepository.findByUserId(userId).isEmpty();
    }

    private void createNewUserAuthentication(String userId, String refreshToken) {
        UserAuthenticationEntity userAuthenticationEntity = new UserAuthenticationEntity();
        userRepository.findByIdOptional(userId).ifPresentOrElse(userAuthenticationEntity::setUser,
            () -> {
                throw new UnauthorizedException("User not found: " + userId);
            });
        userAuthenticationEntity.setRefreshToken(refreshToken);
        userAuthRepository.persist(userAuthenticationEntity);
    }

    private void updateExistingRefreshToken(String userId, String refreshToken) {
        userAuthRepository.updateRefreshToken(userId, refreshToken);
    }

    private String createRefreshTokenJWT(String userId, String userEmail, String refreshToken) {
        return jwtManager.createJWT(
            SessionInfo.builder()
                .userId(userId)
                .userEmail(userEmail)
                .claim("refreshToken", refreshToken)
                .expireAfter(refreshTokenTimeout)
                .build()
        );
    }

    /**
     * Renews the access and refresh tokens for the given refresh token cookie.
     *
     * @param cookies Map of cookies containing the refresh token
     * @return TokenRenewalResponse containing the new access and refresh tokens
     */
    public TokenRenewalResponse renewTokens(Map<String, Cookie> cookies) {
        Cookie refreshCookie = cookies.get(REFRESH_COOKIE_NAME);

        if (refreshCookie == null) {
            throw new UnauthorizedException("No refresh token provided.");
        }

        SessionInfo refreshToken = decryptRefreshTokenCookie(refreshCookie);
        UserAuthenticationModel userAuth = checkValidRefreshToken(refreshToken);

        return new TokenRenewalResponse(generateAccessToken(userAuth),
            generateRefreshToken(refreshToken.getUserId(), refreshToken.getUserEmail()));

    }

    /**
     * Decrypts the access token cookie and verifies the JWT.
     *
     * @param cookie Access token cookie to decrypt
     * @return SessionInfo containing the claims of the access token
     * @throws TokenExpiredException if the access token is expired
     * @throws UnauthorizedException if the access token is invalid
     */
    public SessionInfo decryptAccessTokenCookie(Cookie cookie) throws TokenExpiredException, UnauthorizedException {
        return jwtManager.verifyJWT(cookie.getValue());
    }

    /**
     * Decrypts the access token cookie and verifies the JWT.
     *
     * @param cookies Access token cookie to decrypt
     * @return SessionInfo containing the claims of the access token
     * @throws TokenExpiredException if the access token is expired
     * @throws UnauthorizedException if the access token is invalid
     */
    public SessionInfo checkValidUserSession(Map<String, Cookie> cookies) {
        try {
            Cookie cookie = findAccessTokenCookie(cookies);
            if (cookie == null) {
                throw new TokenExpiredException("No access token provided.");
            }
            return decryptAccessTokenCookie(cookie);
        } catch (TokenExpiredException e) {
            Cookie refreshCookie = findRefreshTokenCookie(cookies);
            if (refreshCookie != null) {
                SessionInfo sessionInfo = decryptRefreshTokenCookie(refreshCookie);
                checkValidRefreshToken(sessionInfo);
                return sessionInfo;
            } else {
                throw new UnauthorizedException("Invalid access token.");
            }
        }
    }

    /**
     * Decrypts the refresh token cookie and verifies the JWT.
     *
     * @param cookie Refresh token cookie to decrypt
     * @return SessionInfo containing the claims of the refresh token
     * @throws UnauthorizedException if the refresh token is invalid
     * @throws TokenExpiredException if the refresh token is expired
     */
    public SessionInfo decryptRefreshTokenCookie(Cookie cookie) throws UnauthorizedException, TokenExpiredException {
        return jwtManager.verifyJWT(cookie.getValue(), true);
    }

    private UserAuthenticationModel checkValidRefreshToken(SessionInfo sessionInfo) {
        Optional<UserAuthenticationEntity> userAuth = userAuthRepository.findByUserId(sessionInfo.getUserId());
        if (userAuth.isEmpty()) {
            throw new UnauthorizedException("User not found: " + sessionInfo.getUserId());
        }
        String refreshToken = userAuth.get().getRefreshToken();
        if (refreshToken == null || !refreshToken.equals(sessionInfo.getClaims().getOrDefault("refreshToken", null))) {

            throw new UnauthorizedException("Invalid refresh token.");
        }
        return userAuth.get();
    }

    private NewCookie buildCookie(String name, String value, int maxAge, boolean httpOnly) {
        return new NewCookie.Builder(name).value(value).path(sessionCookiePath + getSameSiteWorkaround())
            .httpOnly(httpOnly).secure(true).maxAge(maxAge).build();
    }

    @Transactional
    public void logout(Map<String, Cookie> cookies) {
        Cookie refreshCookie = findRefreshTokenCookie(cookies);
        if (refreshCookie != null) {
            SessionInfo sessionInfo = decryptRefreshTokenCookie(refreshCookie);
            userAuthRepository.deleteRefreshToken(sessionInfo.getUserId());
        }
    }

    /**
     * Removes the given cookie from the client.
     *
     * @param cookieName Name of the cookie to remove
     * @return NewCookie to remove the cookie
     */
    public NewCookie removalCookie(String cookieName) {

        return new NewCookie.Builder(cookieName).value("").path(sessionCookiePath + getSameSiteWorkaround())
            // sameSite is currently not supported
            .sameSite(sessionCookieSameSite).maxAge(0).build();
    }

    /**
     * Builder for session info with default expiration time.
     *
     * @param CookieName Name of the cookie, which should be used to determine the expiration time
     * @return SessionInfo.Builder with default expiration time
     */
    public SessionInfo.Builder sessionInfoBuilder(String CookieName) {
        if (CookieName.equals(ACCESS_COOKIE_NAME)) {
            return SessionInfo.builder().expireAfter(accessTokenTimeout);
        } else if (CookieName.equals(REFRESH_COOKIE_NAME)) {
            return SessionInfo.builder().expireAfter(refreshTokenTimeout);
        } else {
            return SessionInfo.builder();
        }
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
