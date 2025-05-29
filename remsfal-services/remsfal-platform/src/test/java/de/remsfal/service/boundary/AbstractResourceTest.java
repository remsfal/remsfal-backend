package de.remsfal.service.boundary;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

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
        insertAddress(TestData.ADDRESS_ID_1, TestData.ADDRESS_STREET_1, TestData.ADDRESS_CITY_1,
            TestData.ADDRESS_PROVINCE_1, TestData.ADDRESS_ZIP_1, TestData.ADDRESS_COUNTRY_1);
        insertUser(TestData.USER_ID_1, TestData.USER_TOKEN_1, TestData.USER_EMAIL_1,
            TestData.USER_FIRST_NAME_1, TestData.USER_LAST_NAME_1, TestData.ADDRESS_ID_1);
        insertAddress(TestData.ADDRESS_ID_2, TestData.ADDRESS_STREET_2, TestData.ADDRESS_CITY_2,
            TestData.ADDRESS_PROVINCE_2, TestData.ADDRESS_ZIP_2, TestData.ADDRESS_COUNTRY_2);
        insertUser(TestData.USER_ID_2, TestData.USER_TOKEN_2, TestData.USER_EMAIL_2,
            TestData.USER_FIRST_NAME_2, TestData.USER_LAST_NAME_2, TestData.ADDRESS_ID_2);
        insertAddress(TestData.ADDRESS_ID_3, TestData.ADDRESS_STREET_3, TestData.ADDRESS_CITY_3,
            TestData.ADDRESS_PROVINCE_3, TestData.ADDRESS_ZIP_3, TestData.ADDRESS_COUNTRY_3);
        insertUser(TestData.USER_ID_3, TestData.USER_TOKEN_3, TestData.USER_EMAIL_3,
            TestData.USER_FIRST_NAME_3, TestData.USER_LAST_NAME_3, TestData.ADDRESS_ID_3);
        insertAddress(TestData.ADDRESS_ID_4, TestData.ADDRESS_STREET_4, TestData.ADDRESS_CITY_4,
            TestData.ADDRESS_PROVINCE_4, TestData.ADDRESS_ZIP_4, TestData.ADDRESS_COUNTRY_4);
        insertUser(TestData.USER_ID_4, TestData.USER_TOKEN_4, TestData.USER_EMAIL_4,
            TestData.USER_FIRST_NAME_4, TestData.USER_LAST_NAME_4, TestData.ADDRESS_ID_4);
    }

    protected void setupTestProjects() {
        // only the default user is MANAGER in all test projects
        insertProject(TestData.PROJECT_ID_1, TestData.PROJECT_TITLE_1);
        insertProjectMember(TestData.PROJECT_ID_1, TestData.USER_ID, "MANAGER");
        insertProject(TestData.PROJECT_ID_2, TestData.PROJECT_TITLE_2);
        insertProjectMember(TestData.PROJECT_ID_2, TestData.USER_ID, "MANAGER");
        insertProject(TestData.PROJECT_ID_3, TestData.PROJECT_TITLE_3);
        insertProjectMember(TestData.PROJECT_ID_3, TestData.USER_ID, "MANAGER");
        insertProject(TestData.PROJECT_ID_4, TestData.PROJECT_TITLE_4);
        insertProjectMember(TestData.PROJECT_ID_4, TestData.USER_ID, "MANAGER");
        insertProject(TestData.PROJECT_ID_5, TestData.PROJECT_TITLE_5);
        insertProjectMember(TestData.PROJECT_ID_5, TestData.USER_ID, "MANAGER");
    }

    protected void setupTestProperties() {
        insertProperty(TestData.PROPERTY_ID_1, TestData.PROJECT_ID, TestData.PROPERTY_TITLE_1,
            TestData.PROPERTY_REG_ENTRY_1, TestData.PROPERTY_DESCRIPTION_1, TestData.PROPERTY_PLOT_AREA_1);
        insertProperty(TestData.PROPERTY_ID_2, TestData.PROJECT_ID, TestData.PROPERTY_TITLE_2,
            TestData.PROPERTY_REG_ENTRY_2, TestData.PROPERTY_DESCRIPTION_2, TestData.PROPERTY_PLOT_AREA_2);
        insertProperty(UUID.randomUUID().toString(), TestData.PROJECT_ID_2, TestData.PROPERTY_TITLE_1,
            TestData.PROPERTY_REG_ENTRY_1, TestData.PROPERTY_DESCRIPTION_1, TestData.PROPERTY_PLOT_AREA_1);
        insertProperty(UUID.randomUUID().toString(), TestData.PROJECT_ID_3, TestData.PROPERTY_TITLE_2,
            TestData.PROPERTY_REG_ENTRY_2, TestData.PROPERTY_DESCRIPTION_2, TestData.PROPERTY_PLOT_AREA_2);
    }

    protected void setupTestSites() {
        insertAddress(TestData.ADDRESS_ID_5, TestData.ADDRESS_STREET_5, TestData.ADDRESS_CITY_5,
            TestData.ADDRESS_PROVINCE_5, TestData.ADDRESS_ZIP_5, TestData.ADDRESS_COUNTRY_5);
        insertSite(TestData.SITE_ID_1, TestData.PROJECT_ID, TestData.PROPERTY_ID, TestData.SITE_TITLE_1,
            TestData.ADDRESS_ID_5, TestData.SITE_DESCRIPTION, TestData.SITE_USABLE_SPACE);
    }

    protected void insertAddress(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO ADDRESS (ID, STREET, CITY, PROVINCE, ZIP, COUNTRY) VALUES (?,?,?,?,?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .setParameter(3, params[2])
                .setParameter(4, params[3])
                .setParameter(5, params[4])
                .setParameter(6, params[5])
                .executeUpdate());
    }

    protected void insertUser(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, EMAIL, FIRST_NAME, LAST_NAME, ADDRESS_ID) VALUES (?,?,?,?,?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .setParameter(3, params[2])
                .setParameter(4, params[3])
                .setParameter(5, params[4])
                .setParameter(6, params[5])
                .executeUpdate());
    }

    protected void insertProject(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO PROJECT (ID, TITLE) VALUES (?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .executeUpdate());
    }

    protected void insertProjectMember(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, MEMBER_ROLE) VALUES (?,?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .setParameter(3, params[2])
                .executeUpdate());
    }

    protected void insertProperty(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO PROPERTY (ID, PROJECT_ID, TITLE, LAND_REGISTER_ENTRY, DESCRIPTION, PLOT_AREA) VALUES (?,?,?,?,?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .setParameter(3, params[2])
                .setParameter(4, params[3])
                .setParameter(5, params[4])
                .setParameter(6, params[5])
                .executeUpdate());
    }

    protected void insertSite(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO SITE (ID, PROJECT_ID, PROPERTY_ID, TITLE, ADDRESS_ID, DESCRIPTION, USABLE_SPACE) VALUES (?,?,?,?,?,?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .setParameter(3, params[2])
                .setParameter(4, params[3])
                .setParameter(5, params[4])
                .setParameter(6, params[5])
                .setParameter(7, params[6])
                .executeUpdate());
    }

    protected void insertBuilding(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO BUILDING (ID, PROJECT_ID, PROPERTY_ID, TITLE, DESCRIPTION, LIVING_SPACE, COMMERCIAL_SPACE, USABLE_SPACE, HEATING_SPACE, ADDRESS_ID) VALUES (?,?,?,?,?,?,?,?,?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .setParameter(3, params[2])
                .setParameter(4, params[3])
                .setParameter(5, params[4])
                .setParameter(6, params[5])
                .setParameter(7, params[6])
                .setParameter(8, params[7])
                .setParameter(9, params[8])
                .setParameter(10, params[9])
                .executeUpdate());
    }

    protected void insertApartment(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO APARTMENT (ID, PROJECT_ID, BUILDING_ID,TITLE, LOCATION, DESCRIPTION, LIVING_SPACE, USABLE_SPACE, HEATING_SPACE) VALUES (?,?,?,?,?,?,?,?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .setParameter(3, params[2])
                .setParameter(4, params[3])
                .setParameter(5, params[4])
                .setParameter(6, params[5])
                .setParameter(7, params[6])
                .setParameter(8, params[7])
                .setParameter(9, params[8])
                .executeUpdate());
    }

    protected void insertStorage(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO STORAGE (ID, PROJECT_ID, BUILDING_ID, TITLE, LOCATION, DESCRIPTION, USABLE_SPACE) VALUES (?,?,?,?,?,?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .setParameter(3, params[2])
                .setParameter(4, params[3])
                .setParameter(5, params[4])
                .setParameter(6, params[5])
                .setParameter(7, params[6])
                .executeUpdate());
    }

    protected void insertCommercial(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO COMMERCIAL (ID, PROJECT_ID, BUILDING_ID, TITLE, LOCATION, DESCRIPTION, COMMERCIAL_SPACE, USABLE_SPACE, HEATING_SPACE) VALUES (?,?,?,?,?,?,?,?,?)")
                .setParameter(1, params[0])
                .setParameter(2, params[1])
                .setParameter(3, params[2])
                .setParameter(4, params[3])
                .setParameter(5, params[4])
                .setParameter(6, params[5])
                .setParameter(7, params[6])
                .setParameter(8, params[7])
                .setParameter(9, params[8])
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
