package de.remsfal.chat.boundary;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.Cookie;
import jakarta.inject.Inject;
import de.remsfal.chat.AbstractTest;
import de.remsfal.chat.TestData;
import de.remsfal.chat.boundary.authentication.SessionManager;
import de.remsfal.common.authentication.JWTManager;
import de.remsfal.common.authentication.SessionInfo;

import java.time.Duration;

public abstract class AbstractResourceTest extends AbstractTest {

    static {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @Inject
    JWTManager jwtManager;

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

    protected Cookie buildCookie(final String userId, final String userEmail, final Duration ttl) {
        // Baue Access Token
        SessionInfo.Builder sessionInfoBuilder = SessionInfo.builder();

        if (userId != null) {
            sessionInfoBuilder.userId(userId);
        }
        if (userEmail != null) {
            sessionInfoBuilder.userEmail(userEmail);
        }
        if (ttl != null) {
            sessionInfoBuilder.expireAfter(ttl);
        }

        String jwt = jwtManager.createJWT(sessionInfoBuilder.build());
        return new Cookie.Builder(SessionManager.ACCESS_COOKIE_NAME, jwt)
                .setMaxAge(ttl.toSeconds())
                .build();
    }

}
