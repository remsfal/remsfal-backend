package de.remsfal.service.boundary;

import java.time.Duration;
import java.util.Map;

import de.remsfal.common.authentication.SessionInfo;
import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import de.remsfal.service.boundary.authentication.SessionManager;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.Cookie;
import jakarta.inject.Inject;

public abstract class AbstractResourceTest extends AbstractTest {

    static {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @Inject
    SessionManager sessionManager;

    protected void setupTestUsers() {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, EMAIL, FIRST_NAME, LAST_NAME) VALUES (?,?,?,?,?)")
                .setParameter(1, TestData.USER_ID_1)
                .setParameter(2, TestData.USER_TOKEN_1)
                .setParameter(3, TestData.USER_EMAIL_1)
                .setParameter(4, TestData.USER_FIRST_NAME_1)
                .setParameter(5, TestData.USER_LAST_NAME_1)
                .executeUpdate());
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, EMAIL, FIRST_NAME, LAST_NAME) VALUES (?,?,?,?,?)")
                .setParameter(1, TestData.USER_ID_2)
                .setParameter(2, TestData.USER_TOKEN_2)
                .setParameter(3, TestData.USER_EMAIL_2)
                .setParameter(4, TestData.USER_FIRST_NAME_2)
                .setParameter(5, TestData.USER_LAST_NAME_2)
                .executeUpdate());
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, EMAIL, FIRST_NAME, LAST_NAME) VALUES (?,?,?,?,?)")
                .setParameter(1, TestData.USER_ID_3)
                .setParameter(2, TestData.USER_TOKEN_3)
                .setParameter(3, TestData.USER_EMAIL_3)
                .setParameter(4, TestData.USER_FIRST_NAME_3)
                .setParameter(5, TestData.USER_LAST_NAME_3)
                .executeUpdate());
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, EMAIL, FIRST_NAME, LAST_NAME) VALUES (?,?,?,?,?)")
                .setParameter(1, TestData.USER_ID_4)
                .setParameter(2, TestData.USER_TOKEN_4)
                .setParameter(3, TestData.USER_EMAIL_4)
                .setParameter(4, TestData.USER_FIRST_NAME_4)
                .setParameter(5, TestData.USER_LAST_NAME_4)
                .executeUpdate());
    }

    protected Cookie buildAccessTokenCookie(final String userId, final String userEmail, final Duration ttl) {
        // Baue Access Token
        SessionInfo.Builder sessionInfoBuilder = sessionManager.sessionInfoBuilder(SessionManager.ACCESS_COOKIE_NAME);

        if (userId != null) {
            sessionInfoBuilder.userId(userId);
        }
        if (userEmail != null) {
            sessionInfoBuilder.userEmail(userEmail);
        }
        if (ttl != null) {
            sessionInfoBuilder.expireAfter(ttl);
        }

        final String accessToken = sessionManager.generateAccessToken(sessionInfoBuilder.build()).getValue();

        return new Cookie.Builder(SessionManager.ACCESS_COOKIE_NAME, accessToken)
                .setMaxAge(ttl.toSeconds())
                .build();
    }

    protected Cookie buildRefreshTokenCookie(final String userId,final String userEmail, final Duration ttl) {
        // Baue Refresh Token
        final String refreshToken = sessionManager.generateRefreshToken(userId, userEmail).getValue();

        return new Cookie.Builder(SessionManager.REFRESH_COOKIE_NAME, refreshToken)
                .setMaxAge(ttl.toSeconds())
                .build();
    }

    protected Map<String, ?> buildCookies (final String userId, final String userEmail, final Duration ttl) {
        return Map.of(
                SessionManager.ACCESS_COOKIE_NAME, buildAccessTokenCookie(userId, userEmail, ttl).getValue(),
                SessionManager.REFRESH_COOKIE_NAME, buildRefreshTokenCookie(userId, userEmail, ttl).getValue()
        );
    }
}
