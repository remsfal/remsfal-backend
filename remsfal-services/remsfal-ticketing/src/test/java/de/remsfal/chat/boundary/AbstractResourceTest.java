package de.remsfal.chat.boundary;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.Cookie;
import jakarta.inject.Inject;
import de.remsfal.chat.AbstractTicketingTest;
import de.remsfal.chat.TicketingTestData;
import de.remsfal.chat.boundary.authentication.SessionManager;
import de.remsfal.common.authentication.JWTManager;

import java.time.Duration;

public abstract class AbstractResourceTest extends AbstractTicketingTest {

    static {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @Inject
    JWTManager jwtManager;

    @Deprecated
    protected void setupTestUsers() {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, EMAIL, FIRST_NAME, LAST_NAME) VALUES (?,?,?,?,?)")
            .setParameter(1, TicketingTestData.USER_ID_1)
            .setParameter(2, TicketingTestData.USER_TOKEN_1)
            .setParameter(3, TicketingTestData.USER_EMAIL_1)
            .setParameter(4, TicketingTestData.USER_FIRST_NAME_1)
            .setParameter(5, TicketingTestData.USER_LAST_NAME_1)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, EMAIL, FIRST_NAME, LAST_NAME) VALUES (?,?,?,?,?)")
            .setParameter(1, TicketingTestData.USER_ID_2)
            .setParameter(2, TicketingTestData.USER_TOKEN_2)
            .setParameter(3, TicketingTestData.USER_EMAIL_2)
            .setParameter(4, TicketingTestData.USER_FIRST_NAME_2)
            .setParameter(5, TicketingTestData.USER_LAST_NAME_2)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, EMAIL, FIRST_NAME, LAST_NAME) VALUES (?,?,?,?,?)")
            .setParameter(1, TicketingTestData.USER_ID_3)
            .setParameter(2, TicketingTestData.USER_TOKEN_3)
            .setParameter(3, TicketingTestData.USER_EMAIL_3)
            .setParameter(4, TicketingTestData.USER_FIRST_NAME_3)
            .setParameter(5, TicketingTestData.USER_LAST_NAME_3)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, EMAIL, FIRST_NAME, LAST_NAME) VALUES (?,?,?,?,?)")
            .setParameter(1, TicketingTestData.USER_ID_4)
            .setParameter(2, TicketingTestData.USER_TOKEN_4)
            .setParameter(3, TicketingTestData.USER_EMAIL_4)
            .setParameter(4, TicketingTestData.USER_FIRST_NAME_4)
            .setParameter(5, TicketingTestData.USER_LAST_NAME_4)
            .executeUpdate());
    }

    @Deprecated
    protected void setupTestMemberships() {
        logger.info("Setting up project memberships");
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, MEMBER_ROLE) VALUES (?,?,?)")
            .setParameter(1, TicketingTestData.PROJECT_ID_1)
            .setParameter(2, TicketingTestData.USER_ID_1)
            .setParameter(3, "MANAGER")
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, MEMBER_ROLE) VALUES (?,?,?)")
            .setParameter(1, TicketingTestData.PROJECT_ID_2)
            .setParameter(2, TicketingTestData.USER_ID_1)
            .setParameter(3, "MANAGER")
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, MEMBER_ROLE) VALUES (?,?,?)")
            .setParameter(1, TicketingTestData.PROJECT_ID_3)
            .setParameter(2, TicketingTestData.USER_ID_1)
            .setParameter(3, "MANAGER")
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, MEMBER_ROLE) VALUES (?,?,?)")
            .setParameter(1, TicketingTestData.PROJECT_ID_4)
            .setParameter(2, TicketingTestData.USER_ID_1)
            .setParameter(3, "MANAGER")
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, MEMBER_ROLE) VALUES (?,?,?)")
            .setParameter(1, TicketingTestData.PROJECT_ID_5)
            .setParameter(2, TicketingTestData.USER_ID_1)
            .setParameter(3, "MANAGER")
            .executeUpdate());
        logger.info("User " + TicketingTestData.USER_ID + " is the manager of all projects.");

        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, MEMBER_ROLE) VALUES (?,?,?)")
            .setParameter(1, TicketingTestData.PROJECT_ID_1)
            .setParameter(2, TicketingTestData.USER_ID_2)
            .setParameter(3, "STAFF")
            .executeUpdate());
        logger.info("User " + TicketingTestData.USER_ID_2 + " is a caretaker in project " + TicketingTestData.PROJECT_ID_1);
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, MEMBER_ROLE) VALUES (?,?,?)")
            .setParameter(1, TicketingTestData.PROJECT_ID_1)
            .setParameter(2, TicketingTestData.USER_ID_3)
            .setParameter(3, "LESSOR")
            .executeUpdate());
        logger.info("User " + TicketingTestData.USER_ID_3 + " is a lessor in project " + TicketingTestData.PROJECT_ID_1);
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, MEMBER_ROLE) VALUES (?,?,?)")
            .setParameter(1, TicketingTestData.PROJECT_ID_1)
            .setParameter(2, TicketingTestData.USER_ID_4)
            .setParameter(3, "PROPRIETOR")
            .executeUpdate());
        logger.info("User " + TicketingTestData.USER_ID_4 + " is a proprietor in project " + TicketingTestData.PROJECT_ID_1);

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
