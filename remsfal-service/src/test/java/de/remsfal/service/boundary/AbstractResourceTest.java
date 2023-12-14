package de.remsfal.service.boundary;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.Cookie;
import jakarta.inject.Inject;
import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import de.remsfal.service.boundary.authentication.SessionInfo;
import de.remsfal.service.boundary.authentication.SessionManager;

import java.time.Duration;

public abstract class AbstractResourceTest extends AbstractTest {

    static {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @Inject
    SessionManager sessionManager;

    void setupTestUsers() {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, NAME, EMAIL) VALUES (?,?,?,?)")
            .setParameter(1, TestData.USER_ID_1)
            .setParameter(2, TestData.USER_TOKEN_1)
            .setParameter(3, TestData.USER_NAME_1)
            .setParameter(4, TestData.USER_EMAIL_1)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, NAME, EMAIL) VALUES (?,?,?,?)")
            .setParameter(1, TestData.USER_ID_2)
            .setParameter(2, TestData.USER_TOKEN_2)
            .setParameter(3, TestData.USER_NAME_2)
            .setParameter(4, TestData.USER_EMAIL_2)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, NAME, EMAIL) VALUES (?,?,?,?)")
            .setParameter(1, TestData.USER_ID_3)
            .setParameter(2, TestData.USER_TOKEN_3)
            .setParameter(3, TestData.USER_NAME_3)
            .setParameter(4, TestData.USER_EMAIL_3)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, NAME, EMAIL) VALUES (?,?,?,?)")
            .setParameter(1, TestData.USER_ID_4)
            .setParameter(2, TestData.USER_TOKEN_4)
            .setParameter(3, TestData.USER_NAME_4)
            .setParameter(4, TestData.USER_EMAIL_4)
            .executeUpdate());
    }

    protected Cookie buildCookie(final String userId, final String userEmail, final Duration ttl) {
        final SessionInfo sessionInfo = SessionInfo.builder()
            .expireAfter(ttl)
            .userId(userId)
            .userEmail(userEmail)
            .build();
        final String value = sessionManager.encryptSessionObject(sessionInfo);
        return new Cookie.Builder("remsfal_session", value)
            .setMaxAge(ttl.toSeconds())
            .build();
    }

}