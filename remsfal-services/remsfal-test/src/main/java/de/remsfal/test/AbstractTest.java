package de.remsfal.test;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import jakarta.inject.Inject;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.Cookie;
import io.smallrye.jwt.build.Jwt;

public abstract class AbstractTest {

    static {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    private static final String ACCESS_COOKIE_NAME = "remsfal_access_token";

    @Inject
    protected Logger logger;

    @BeforeEach
    void printTestMethod(final TestInfo testInfo) {
        String method = testInfo.getDisplayName();
        if (method.length() > 100) {
            method = method.substring(0, 100);
        }
        final String line = String.join("", Collections.nCopies(104, "#"));
        final String title = "# " +
            method + String.join("", Collections.nCopies(100 - method.length(), " ")) +
            " #";
        logger.info(line);
        logger.info(title);
        logger.info(line);
    }

    protected Cookie buildManagerCookie() {
        final Map<String, String> managerProjectRoles = Map.of(
            TestData.PROJECT_ID_1.toString(), "MANAGER",
            TestData.PROJECT_ID_2.toString(), "MANAGER",
            TestData.PROJECT_ID_3.toString(), "MANAGER",
            TestData.PROJECT_ID_4.toString(), "MANAGER",
            TestData.PROJECT_ID_5.toString(), "MANAGER"
        );
        return buildManagerCookie(managerProjectRoles);
    }

    protected Cookie buildManagerCookie(final Map<String, String> projectRoles) {
        return buildCookie(TestData.USER_ID, TestData.USER_EMAIL, TestData.USER_NAME, true,
            projectRoles, Map.of(), Map.of(), Duration.ofMinutes(10));
    }

    protected Cookie buildCookie(final UUID userId, final String userEmail, final String userName,
        final Map<String, String> projectRoles, final Map<String, String> organizationRoles,
        final Map<String, String> tenancyProjects) {
        return buildCookie(userId, userEmail, userName, true, projectRoles,
            organizationRoles, tenancyProjects, Duration.ofMinutes(10));
    }

    protected Cookie buildCookie(final UUID userId, final String userEmail, final String userName,
        final Boolean active, final Map<String, String> projectRoles, final Map<String, String> organizationRoles,
        final Map<String, String> tenancyProjects, final Duration ttl) {

        long exp = (System.currentTimeMillis() / 1000) + ttl.getSeconds();

        String jwt = Jwt.claims()
            .claim("sub", userId)
            .claim("email", userEmail)
            .claim("name", userName)
            .claim("active", active)
            .claim("project_roles", projectRoles)
            .claim("organization_roles", organizationRoles)
            .claim("tenancy_projects", tenancyProjects)
            .issuer("REMSFAL")
            .expiresAt(exp)
            .sign();

        return new Cookie.Builder(ACCESS_COOKIE_NAME, jwt).setMaxAge(ttl.toSeconds()).build();
    }

}
