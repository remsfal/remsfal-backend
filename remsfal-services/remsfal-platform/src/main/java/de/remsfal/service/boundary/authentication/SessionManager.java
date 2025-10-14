package de.remsfal.service.boundary.authentication;

import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dao.TenancyRepository;
import de.remsfal.service.entity.dto.ProjectMembershipEntity;
import de.remsfal.service.entity.dto.TenancyEntity;
import de.remsfal.service.entity.dto.UserEntity;
import io.smallrye.jwt.auth.principal.JWTParser;
import de.remsfal.common.authentication.JWTManager;
import de.remsfal.common.authentication.UnauthorizedException;
import de.remsfal.core.model.UserAuthenticationModel;
import de.remsfal.service.entity.dao.UserAuthenticationRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.UserAuthenticationEntity;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.NewCookie.SameSite;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class SessionManager {

    public static final String ACCESS_COOKIE_NAME = "remsfal_access_token";
    public static final String REFRESH_COOKIE_NAME = "remsfal_refresh_token";

    @ConfigProperty(name = "de.remsfal.auth.session.cookie-path", defaultValue = "/")
    String sessionCookiePath;

    @ConfigProperty(name = "de.remsfal.auth.session.cookie-same-site", defaultValue = "STRICT")
    SameSite sessionCookieSameSite;

    @ConfigProperty(name = "de.remsfal.auth.access-token.timeout", defaultValue = "PT5M")
    Duration accessTokenTimeout;

    @ConfigProperty(name = "de.remsfal.auth.refresh-token.timeout", defaultValue = "P7D")
    Duration refreshTokenTimeout;

    @Inject
    JWTManager jwtManager;

    @Inject
    JWTParser jwtParser;

    @Inject
    UserAuthenticationRepository userAuthRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    TenancyRepository tenancyRepository;

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

        UserEntity user = userRepository.findByIdOptional(userId)
            .orElseThrow(() -> new UnauthorizedException("User not found: " + userId));

        Map<String, String> projectRoles = getProjectAuthorization(userId);
        Map<String, String> tenancyProjects = getTenancyAuthorization(userId);

        String jwt = jwtManager.createAccessToken(user, projectRoles, tenancyProjects,
            accessTokenTimeout.getSeconds());
        return buildCookie(ACCESS_COOKIE_NAME, jwt, (int) accessTokenTimeout.getSeconds(), false);
    }

    private Map<String, String> getProjectAuthorization(final UUID userId) {
        List<ProjectMembershipEntity> memberships = projectRepository.findMembershipByUserId(userId, 0,
            Integer.MAX_VALUE);
        return memberships.stream().collect(Collectors.toMap(
            m -> m.getProject().getId().toString(),
            m -> m.getRole().name()
        ));
    }

    private Map<String, String> getTenancyAuthorization(final UUID userId) {
        List<TenancyEntity> tenancies = tenancyRepository.findTenanciesByTenant(userId);
        return tenancies.stream().collect(Collectors.toMap(
            t -> t.getId().toString(),
            t -> t.getProjectId().toString()
        ));
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
        String refreshId = UUID.randomUUID().toString();

        if (isNewUserAuthenticationEntity(userId)) {
            createNewUserAuthentication(userId, refreshId);
        } else {
            updateExistingRefreshToken(userId, refreshId);
        }

        String jwt = jwtManager.createRefreshToken(userId, userEmail, refreshId, refreshTokenTimeout.getSeconds());
        return buildCookie(REFRESH_COOKIE_NAME, jwt, (int) refreshTokenTimeout.getSeconds(), true);
    }

    /**
     * Renews the access and refresh tokens for the given refresh token cookie.
     *
     * @param cookies Map of cookies containing the refresh token
     * @return TokenRenewalResponse containing the new access and refresh tokens
     */
    @Transactional
    public TokenRenewalResponse renewTokens(Map<String, Cookie> cookies) {
        Cookie refreshCookie = cookies.get(REFRESH_COOKIE_NAME);

        if (refreshCookie == null) {
            throw new UnauthorizedException("No refresh token provided.");
        }

        JsonWebToken refreshJwt = parseRefreshToken(refreshCookie.getValue());
        UUID userId = UUID.fromString(refreshJwt.getSubject());
        String email = refreshJwt.getClaim("email");
        String refreshId = refreshJwt.getClaim("refreshToken");

        UserAuthenticationModel userAuth = requireValidRefreshToken(userId, refreshId);

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

    /** Checks if there is an existing user authentication entity for the given user ID */
    private boolean isNewUserAuthenticationEntity(final UUID userId) {
        return userAuthRepository.findByUserId(userId).isEmpty();
    }

    /** Creates a new user authentication entity with the given user ID and refresh token */
    private void createNewUserAuthentication(final UUID userId, final String refreshToken) {
        UserAuthenticationEntity userAuthenticationEntity = new UserAuthenticationEntity();
        userRepository.findByIdOptional(userId).ifPresentOrElse(userAuthenticationEntity::setUser,
            () -> {
                throw new UnauthorizedException("User not found: " + userId);
            });
        userAuthenticationEntity.setRefreshToken(refreshToken);
        userAuthRepository.persist(userAuthenticationEntity);
    }

    /** Updates the existing refresh token for the given user ID */
    private void updateExistingRefreshToken(final UUID userId, final String refreshToken) {
        userAuthRepository.updateRefreshToken(userId, refreshToken);
    }

    /** Validates the refresh token claim against the stored refresh token for the given user ID */
    private UserAuthenticationModel requireValidRefreshToken(final UUID userId, final String refreshTokenClaim) {
        Optional<UserAuthenticationEntity> userAuth = userAuthRepository.findByUserId(userId);

        if (userAuth.isEmpty()) {
            throw new UnauthorizedException("User not found: " + userId);
        }

        String saved = userAuth.get().getRefreshToken();
        if (saved == null || !saved.equals(refreshTokenClaim)) {
            throw new UnauthorizedException("Refresh token mismatch.");
        }

        return userAuth.get();
    }

    /** Builds a new cookie with the given parameters */
    private NewCookie buildCookie(final String name, final String value, int maxAge, boolean httpOnly) {
        return new NewCookie.Builder(name).value(value).path(sessionCookiePath + getSameSiteWorkaround())
            .httpOnly(httpOnly).secure(true).maxAge(maxAge).build();
    }

    @Transactional
    public void logout(final Map<String, Cookie> cookies) {
        Cookie refreshCookie = cookies.get(REFRESH_COOKIE_NAME);

        if (refreshCookie != null) {
            JsonWebToken refresh = parseRefreshToken(refreshCookie.getValue());
            UUID userId = UUID.fromString(refresh.getSubject());
            userAuthRepository.deleteRefreshToken(userId);
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
