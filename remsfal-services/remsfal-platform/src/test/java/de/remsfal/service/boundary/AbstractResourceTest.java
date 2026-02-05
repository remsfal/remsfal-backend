package de.remsfal.service.boundary;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.boundary.authentication.SessionManager;
import de.remsfal.test.TestData;
import io.restassured.http.Cookie;
import jakarta.inject.Inject;

public abstract class AbstractResourceTest extends AbstractServiceTest {

    @Inject
    SessionManager sessionManager;

    @Override
    protected Cookie buildManagerCookie() {
        return buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10));
    }

    @Override
    protected Cookie buildManagerCookie(final Map<String, String> projectRoles) {
        // For platform tests, we ignore the projectRoles parameter since the SessionManager
        // will fetch the actual roles from the database based on the user setup
        return buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10));
    }

    protected Cookie buildAccessTokenCookie(final UUID userId, final String userEmail, final Duration ttl) {
        String accessToken;
        try {
            accessToken = sessionManager.generateAccessToken(userId, userEmail).getValue();
        } catch (Exception e) {
            accessToken = "invalid.jwt.token";
        }

        Cookie.Builder cookieBuilder = new Cookie.Builder(SessionManager.ACCESS_COOKIE_NAME, accessToken);

        if (ttl != null) {
            cookieBuilder.setMaxAge(ttl.toSeconds());
        }
        return cookieBuilder.build();
    }

    protected Cookie buildRefreshTokenCookie(final UUID userId, final String userEmail, final Duration ttl) {
        final String refreshToken = sessionManager.generateRefreshToken(userId, userEmail).getValue();
        Cookie.Builder cookieBuilder = new Cookie.Builder(SessionManager.REFRESH_COOKIE_NAME, refreshToken);

        if (ttl != null) {
            cookieBuilder.setMaxAge(ttl.toSeconds());
        }
        return cookieBuilder.build();
    }

    protected Map<String, ?> buildCookies(final UUID userId, final String userEmail, final Duration ttl) {
        return Map.of(
                SessionManager.ACCESS_COOKIE_NAME, buildAccessTokenCookie(userId, userEmail, ttl).getValue(),
                SessionManager.REFRESH_COOKIE_NAME, buildRefreshTokenCookie(userId, userEmail, ttl).getValue()
        );
    }

}
