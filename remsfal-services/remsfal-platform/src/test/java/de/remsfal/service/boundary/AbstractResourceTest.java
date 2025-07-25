package de.remsfal.service.boundary;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import de.remsfal.common.authentication.SessionInfo;
import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.boundary.authentication.SessionManager;
import de.remsfal.test.TestData;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.Cookie;
import jakarta.inject.Inject;

public abstract class AbstractResourceTest extends AbstractServiceTest {

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
            TestData.PROPERTY_LOCATION_1, TestData.PROPERTY_DESCRIPTION_1, TestData.PROPERTY_LAND_REGISTRY_1,
            TestData.PROPERTY_CADASTRAL_DESTRICT_1, TestData.PROPERTY_SHEET_NUMBER_1,
            TestData.PROPERTY_PLOT_NUMBER_1, TestData.PROPERTY_CADASTRAL_SECTION_1,
            TestData.PROPERTY_PLOT_1, TestData.PROPERTY_ECONOMY_TYPE_1, TestData.PROPERTY_PLOT_AREA_1);
        insertProperty(TestData.PROPERTY_ID_2, TestData.PROJECT_ID, TestData.PROPERTY_TITLE_2,
            TestData.PROPERTY_LOCATION_2, TestData.PROPERTY_DESCRIPTION_2, TestData.PROPERTY_LAND_REGISTRY_2,
            TestData.PROPERTY_CADASTRAL_DESTRICT_2, TestData.PROPERTY_SHEET_NUMBER_2,
            TestData.PROPERTY_PLOT_NUMBER_2, TestData.PROPERTY_CADASTRAL_SECTION_2,
            TestData.PROPERTY_PLOT_2, TestData.PROPERTY_ECONOMY_TYPE_2, TestData.PROPERTY_PLOT_AREA_2);
        insertProperty(UUID.randomUUID().toString(), TestData.PROJECT_ID_2, TestData.PROPERTY_TITLE_1,
            TestData.PROPERTY_LOCATION_2, TestData.PROPERTY_DESCRIPTION_2, TestData.PROPERTY_LAND_REGISTRY_2,
            TestData.PROPERTY_CADASTRAL_DESTRICT_2, TestData.PROPERTY_SHEET_NUMBER_2,
            TestData.PROPERTY_PLOT_NUMBER_2, TestData.PROPERTY_CADASTRAL_SECTION_2,
            TestData.PROPERTY_PLOT_2, TestData.PROPERTY_ECONOMY_TYPE_2, TestData.PROPERTY_PLOT_AREA_2);
        insertProperty(UUID.randomUUID().toString(), TestData.PROJECT_ID_3, TestData.PROPERTY_TITLE_2,
            TestData.PROPERTY_LOCATION_2, TestData.PROPERTY_DESCRIPTION_2, TestData.PROPERTY_LAND_REGISTRY_2,
            TestData.PROPERTY_CADASTRAL_DESTRICT_2, TestData.PROPERTY_SHEET_NUMBER_2,
            TestData.PROPERTY_PLOT_NUMBER_2, TestData.PROPERTY_CADASTRAL_SECTION_2,
            TestData.PROPERTY_PLOT_2, TestData.PROPERTY_ECONOMY_TYPE_2, TestData.PROPERTY_PLOT_AREA_2);
    }

    protected void setupTestSites() {
        insertAddress(TestData.ADDRESS_ID_5, TestData.ADDRESS_STREET_5, TestData.ADDRESS_CITY_5,
            TestData.ADDRESS_PROVINCE_5, TestData.ADDRESS_ZIP_5, TestData.ADDRESS_COUNTRY_5);
        insertSite(TestData.SITE_ID_1, TestData.PROJECT_ID, TestData.PROPERTY_ID, TestData.SITE_TITLE_1,
            TestData.ADDRESS_ID_5, TestData.SITE_DESCRIPTION, TestData.SITE_OUTDOOR_AREA);
    }

    protected void setupTestBuildings() {
        insertAddress(TestData.ADDRESS_ID_6, TestData.ADDRESS_STREET_6, TestData.ADDRESS_CITY_6,
            TestData.ADDRESS_PROVINCE_6, TestData.ADDRESS_ZIP_6, TestData.ADDRESS_COUNTRY_6);
        insertBuilding(TestData.BUILDING_ID_1, TestData.PROJECT_ID, TestData.PROPERTY_ID,
            TestData.BUILDING_TITLE_1, TestData.BUILDING_DESCRIPTION_1,
            null, null, null, null, null, null, TestData.ADDRESS_ID_6);
        insertAddress(TestData.ADDRESS_ID_7, TestData.ADDRESS_STREET_7, TestData.ADDRESS_CITY_7,
            TestData.ADDRESS_PROVINCE_7, TestData.ADDRESS_ZIP_7, TestData.ADDRESS_COUNTRY_7);
        insertBuilding(TestData.BUILDING_ID_2, TestData.PROJECT_ID, TestData.PROPERTY_ID,
            TestData.BUILDING_TITLE_2, TestData.BUILDING_DESCRIPTION_2,
            null, null, null, null, null, null, TestData.ADDRESS_ID_7);
        insertAddress(TestData.ADDRESS_ID_8, TestData.ADDRESS_STREET_8, TestData.ADDRESS_CITY_8,
            TestData.ADDRESS_PROVINCE_8, TestData.ADDRESS_ZIP_8, TestData.ADDRESS_COUNTRY_8);
        insertBuilding(UUID.randomUUID().toString(), TestData.PROJECT_ID, TestData.PROPERTY_ID_2,
            TestData.BUILDING_TITLE_1, TestData.BUILDING_DESCRIPTION_1,
            null, null, null, TestData.BUILDING_LIVING_SPACE_1,
            TestData.BUILDING_USABLE_SPACE_1, TestData.BUILDING_HEATING_SPACE_1, TestData.ADDRESS_ID_8);
        insertAddress(TestData.ADDRESS_ID_9, TestData.ADDRESS_STREET_9, TestData.ADDRESS_CITY_9,
            TestData.ADDRESS_PROVINCE_9, TestData.ADDRESS_ZIP_9, TestData.ADDRESS_COUNTRY_9);
        insertBuilding(UUID.randomUUID().toString(), TestData.PROJECT_ID, TestData.PROPERTY_ID_2,
            TestData.BUILDING_TITLE_2, TestData.BUILDING_DESCRIPTION_2,
            TestData.BUILDING_GROSS_FLOOR_AREA_2, TestData.BUILDING_NET_FLOOR_AREA_2,
            TestData.BUILDING_CONSTRUCTION_FLOOR_AREA_2, null, null, TestData.BUILDING_HEATING_SPACE_2, TestData.ADDRESS_ID_9);

        insertApartment(TestData.APARTMENT_ID_1, TestData.PROJECT_ID, TestData.BUILDING_ID,
            TestData.APARTMENT_TITLE_1, TestData.APARTMENT_LOCATION_1, TestData.APARTMENT_DESCRIPTION_1,
            TestData.APARTMENT_LIVING_SPACE_1, TestData.APARTMENT_USABLE_SPACE_1, TestData.APARTMENT_HEATING_SPACE_1);
        insertApartment(TestData.APARTMENT_ID_2, TestData.PROJECT_ID, TestData.BUILDING_ID,
            TestData.APARTMENT_TITLE_2, TestData.APARTMENT_LOCATION_2, TestData.APARTMENT_DESCRIPTION_2,
            TestData.APARTMENT_LIVING_SPACE_2, TestData.APARTMENT_USABLE_SPACE_2, TestData.APARTMENT_HEATING_SPACE_2);

        insertCommercial(TestData.COMMERCIAL_ID_1, TestData.PROJECT_ID, TestData.BUILDING_ID,
            TestData.COMMERCIAL_TITLE_1, TestData.COMMERCIAL_LOCATION_1, TestData.COMMERCIAL_DESCRIPTION_1,
            TestData.COMMERCIAL_NET_FLOOR_AREA_1, null, null, null, TestData.COMMERCIAL_HEATING_SPACE_1);
        insertCommercial(TestData.COMMERCIAL_ID_2, TestData.PROJECT_ID, TestData.BUILDING_ID,
            TestData.COMMERCIAL_TITLE_2, TestData.COMMERCIAL_LOCATION_2, TestData.COMMERCIAL_DESCRIPTION_2,
            null, TestData.COMMERCIAL_USABLE_FLOOR_AREA_2, TestData.COMMERCIAL_TECHNICAL_SERVICE_AREA_2,
            TestData.COMMERCIAL_TRAFFIC_AREA_2, TestData.COMMERCIAL_HEATING_SPACE_2);

        insertStorage(TestData.STORAGE_ID_1, TestData.PROJECT_ID, TestData.BUILDING_ID,
            TestData.STORAGE_TITLE_1, TestData.STORAGE_LOCATION_1, TestData.STORAGE_DESCRIPTION_1,
            TestData.STORAGE_USABLE_SPACE_1);
        insertStorage(TestData.STORAGE_ID_2, TestData.PROJECT_ID, TestData.BUILDING_ID,
            TestData.STORAGE_TITLE_2, TestData.STORAGE_LOCATION_2, TestData.STORAGE_DESCRIPTION_2,
            TestData.STORAGE_USABLE_SPACE_2);
    }

    protected void setupAllTestData() {
        setupTestUsers();
        setupTestProjects();
        setupTestProperties();
        setupTestSites();
        setupTestBuildings();
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
                .createNativeQuery("INSERT INTO PROPERTY (ID, PROJECT_ID, TITLE, LOCATION, DESCRIPTION, "
                    + "LAND_REGISTRY, CADASTRAL_DISTRICT, SHEET_NUMBER, PLOT_NUMBER, CADASTRAL_SECTION, "
                    + "PLOT, ECONOMY_TYPE, PLOT_AREA) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)")
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
                .setParameter(11, params[10])
                .setParameter(12, params[11])
                .setParameter(13, params[12])
                .executeUpdate());
    }

    protected void insertSite(Object... params) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO SITE (ID, PROJECT_ID, PROPERTY_ID, TITLE, ADDRESS_ID, DESCRIPTION, OUTDOOR_AREA) VALUES (?,?,?,?,?,?,?)")
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
                .createNativeQuery("INSERT INTO BUILDING (ID, PROJECT_ID, PROPERTY_ID, TITLE, DESCRIPTION, "
                    + "GROSS_FLOOR_AREA, NET_FLOOR_AREA, CONSTRUCTION_FLOOR_AREA, LIVING_SPACE, USABLE_SPACE, "
                    + "HEATING_SPACE, ADDRESS_ID) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)")
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
                .setParameter(11, params[10])
                .setParameter(12, params[11])
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
                .createNativeQuery("INSERT INTO COMMERCIAL (ID, PROJECT_ID, BUILDING_ID, TITLE, LOCATION, "
                    + "DESCRIPTION, NET_FLOOR_AREA, USABLE_FLOOR_AREA, TECHNICAL_SERVICE_AREA, TRAFFIC_AREA, "
                    + "HEATING_SPACE) VALUES (?,?,?,?,?,?,?,?,?,?,?)")
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
                .setParameter(11, params[10])
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
