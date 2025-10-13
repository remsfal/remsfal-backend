package de.remsfal.ticketing.boundary;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.Cookie;
import io.smallrye.jwt.build.Jwt;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import de.remsfal.ticketing.AbstractTicketingTest;

public abstract class AbstractResourceTest extends AbstractTicketingTest {

    static {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    private static final String ACCESS_COOKIE_NAME = "remsfal_access_token";

    protected Cookie buildCookie(final UUID userId, final String userEmail, final String userName,
                                 final Boolean active, final Map<String, String> projectRoles, final Duration ttl) {
        long exp = (System.currentTimeMillis() / 1000) + ttl.getSeconds();

        String jwt = Jwt.claims()
                .claim("sub", userId)
                .claim("email", userEmail)
                .claim("name", userName)
                .claim("active", active)
                .claim("project_roles", projectRoles)
                .issuer("REMSFAL")
                .expiresAt(exp)
                .sign();

        return new Cookie.Builder(ACCESS_COOKIE_NAME, jwt).setMaxAge(ttl.toSeconds()).build();
    }

}
