package de.remsfal.service.boundary.authentication;

import de.remsfal.service.boundary.exception.TokenExpiredException;
import de.remsfal.service.boundary.exception.UnauthorizedException;
import de.remsfal.service.entity.dao.UserAuthenticationRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.UserAuthenticationEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.NewCookie.SameSite;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class SessionManager {

    public static final String ACCESS_COOKIE_NAME = "remsfal_access_token";
    public static final String REFRESH_COOKIE_NAME = "remsfal_refresh_token";


    public static final String COOKIE_NAME = "remsfal_session";

    private String sessionCookiePath;

    private SameSite sessionCookieSameSite;

    private Duration accessTokenTimeout;

    private Duration refreshTokenTimeout;

    private JWTManager jwtManager;

    private UserAuthenticationRepository userAuthRepository;

    private UserRepository userRepository;

    public SessionManager(@ConfigProperty(name = "de.remsfal.auth.session.cookie-path", defaultValue = "/") String sessionCookiePath, @ConfigProperty(name = "de.remsfal.auth.session.cookie-same-site", defaultValue = "STRICT") SameSite sessionCookieSameSite, @ConfigProperty(name = "de.remsfal.auth.access-token.timeout", defaultValue = "PT5M") Duration accessTokenTimeout, @ConfigProperty(name = "de.remsfal.auth.refresh-token.timeout", defaultValue = "P7D") Duration refreshTokenTimeout, JWTManager jwtManager, UserAuthenticationRepository userAuthRepository, UserRepository userRepository) {
        this.sessionCookiePath = sessionCookiePath;
        this.sessionCookieSameSite = sessionCookieSameSite;
        this.accessTokenTimeout = accessTokenTimeout;
        this.refreshTokenTimeout = refreshTokenTimeout;
        this.jwtManager = jwtManager;
        this.userAuthRepository = userAuthRepository;
        this.userRepository = userRepository;
    }


    public NewCookie generateAccessToken(SessionInfo sessionInfo) {
        String jwt = jwtManager.createJWT(SessionInfo.builder().from(sessionInfo)
                .expireAfter(accessTokenTimeout).build());
        return buildCookie(ACCESS_COOKIE_NAME, jwt, (int) accessTokenTimeout.getSeconds(), false);
    }

    public NewCookie generateAccessToken(UserAuthenticationEntity userAuth) {
        return generateAccessToken(SessionInfo.builder().from(userAuth).build());
    }

    @Transactional
    public NewCookie generateRefreshToken(String userId, String userEmail) {
        String refreshToken = UUID.randomUUID().toString();
        Optional<UserAuthenticationEntity> existingAuth = userAuthRepository.findByUserId(userId);
        if (existingAuth.isEmpty()) {
            UserAuthenticationEntity userAuthenticationEntity = new UserAuthenticationEntity();
            userRepository.findByIdOptional(userId).ifPresentOrElse(
                    userAuthenticationEntity::setUser,
                    () -> { throw new UnauthorizedException("User not found: " + userId); }
            );
            userAuthenticationEntity.setRefreshToken(refreshToken);
            userAuthRepository.persist(userAuthenticationEntity);
        } else {
            userAuthRepository.updateRefreshToken(userId, refreshToken);
        }
        String jwt = jwtManager.createJWT(SessionInfo.builder().userId(userId)
                .userEmail(userEmail)
                .claim("refreshToken", refreshToken)
                .expireAfter(refreshTokenTimeout).build());
        return buildCookie(REFRESH_COOKIE_NAME, jwt, (int) refreshTokenTimeout.getSeconds(), true);
    }


    public TokenRenewalResponse renewTokens(Map<String, Cookie> cookies) {
        Cookie refreshCookie = cookies.get(REFRESH_COOKIE_NAME);

        if (refreshCookie == null) {
            throw new UnauthorizedException("No refresh token provided.");
        }

        SessionInfo refreshToken = decryptRefreshTokenCookie(refreshCookie);
        UserAuthenticationEntity userAuth = checkValidRefreshToken(refreshToken);

        return new TokenRenewalResponse(
            generateAccessToken(userAuth),
            generateRefreshToken(refreshToken.getUserId(), refreshToken.getUserEmail())
        );

    }

    public SessionInfo decryptAccessTokenCookie(Cookie cookie) {
        return jwtManager.verifyJWT(cookie.getValue());
    }

    public SessionInfo checkValidUserSession(Map<String, Cookie> cookies) {
        try {
            Cookie cookie = findAccessTokenCookie(cookies);
            if (cookie == null) {
                throw new TokenExpiredException("No access token provided.");
            }
            SessionInfo sessionInfo = decryptAccessTokenCookie(cookie);
            return sessionInfo;
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

    public SessionInfo decryptRefreshTokenCookie(Cookie cookie) {
        return jwtManager.verifyJWT(cookie.getValue(), true);
    }

    private UserAuthenticationEntity checkValidRefreshToken(SessionInfo sessionInfo) {
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
        return new NewCookie.Builder(name)
                .value(value)
                .path(sessionCookiePath + getSameSiteWorkaround())
                .httpOnly(httpOnly)
                .secure(true)
                .maxAge(maxAge)
                .build();
    }


    public NewCookie removalCookie(String cookieName) {
        return new NewCookie.Builder(cookieName)
            .value("")
            .path(sessionCookiePath + getSameSiteWorkaround())
            // sameSite is currently not supported
            .sameSite(sessionCookieSameSite)
            .maxAge(0)
            .build();
    }

    public SessionInfo.Builder sessionInfoBuilder(String CookieName) {
        if (CookieName.equals(ACCESS_COOKIE_NAME)) {
            return SessionInfo.builder().expireAfter(accessTokenTimeout);
        } else if (CookieName.equals(REFRESH_COOKIE_NAME)) {
            return SessionInfo.builder().expireAfter(refreshTokenTimeout);
        } else {
            return SessionInfo.builder();
        }
    }

    public Cookie findAccessTokenCookie(final Map<String, Cookie> cookies) {
        if (cookies.containsKey(ACCESS_COOKIE_NAME)) {
            return cookies.get(ACCESS_COOKIE_NAME);
        }
        return null;
    }

    public Cookie findRefreshTokenCookie(final Map<String, Cookie> cookies) {
        if (cookies.containsKey(REFRESH_COOKIE_NAME)) {
            return cookies.get(REFRESH_COOKIE_NAME);
        }
        return null;
    }


    private String getSameSiteWorkaround() {
        // see: https://github.com/jakartaee/rest/issues/862
        return ";SameSite="
            + sessionCookieSameSite.name().substring(0,1).toUpperCase()
            + sessionCookieSameSite.name().substring(1).toLowerCase();
    }

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
