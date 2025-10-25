package de.remsfal.service.boundary.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;

import de.remsfal.common.authentication.JWTManager;
import de.remsfal.common.authentication.UnauthorizedException;
import de.remsfal.service.control.AuthorizationController;
import de.remsfal.service.entity.dto.UserAuthenticationEntity;
import de.remsfal.service.entity.dto.UserEntity;
import de.remsfal.test.TestData;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;

@QuarkusTest
class SessionManagerTest {

    @Inject
    SessionManager sessionManager;

    @InjectMock
    AuthorizationController controller;

    @InjectMock
    JWTManager jwtManager;

    @InjectMock
    JWTParser jwtParser;

    private Map<String, String> createMemberships(String p1Role, String p2Role) {
        return Map.of(
                TestData.PROJECT_ID_1.toString(), p1Role,
                TestData.PROJECT_ID_2.toString(), p2Role
        );
    }

    private JsonWebToken fakeRefreshJwt(UUID subject, String email, String refreshId) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put(Claims.sub.name(), subject.toString());
        claims.put("email", email);
        claims.put("refreshTokenId", refreshId);

        return new JsonWebToken() {
            @Override public String getSubject() { return subject.toString(); }
            @Override public String getName() { return subject.toString(); }
            @Override public Set<String> getClaimNames() { return new LinkedHashSet<>(claims.keySet()); }
            @SuppressWarnings("unchecked")
            @Override public <T> T getClaim(String claim) { return (T) claims.get(claim); }
            @Override public String getRawToken() { return null; }
            @Override public Set<String> getAudience() { return Collections.emptySet(); }
            @Override public String getIssuer() { return null; }
            @Override public long getExpirationTime() { return 0L; }
            @Override public long getIssuedAtTime() { return 0L; }
            @Override public Set<String> getGroups() { return Collections.emptySet(); }
            @Override public String getTokenID() { return null; }
        };
    }

    @Test
    void test_renewTokens_returnsNewCookies_whenRefreshCookieValid() throws ParseException {
        String email = "u1@example.com";
        UUID refreshId = UUID.randomUUID();
        String refreshTokenValue = "refresh.jwt.token";

        when(jwtParser.parse(refreshTokenValue)).thenReturn(fakeRefreshJwt(TestData.USER_ID, email, refreshId.toString()));

        UserEntity user = new UserEntity();
        user.setId(TestData.USER_ID);
        user.setEmail(email);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setTokenId("active-token");

        UserAuthenticationEntity auth = new UserAuthenticationEntity();
        auth.setUser(user);
        auth.setRefreshTokenId(refreshId);

        when(controller.requireValidRefreshToken(TestData.USER_ID, refreshId))
            .thenReturn(auth);
        when(controller.getAuthenticatedUser(TestData.USER_ID)).thenReturn(user);
        when(controller.getProjectAuthorization(eq(TestData.USER_ID)))
            .thenReturn(createMemberships("MANAGER", "STAFF"));
        when(controller.getTenancyAuthorization(eq(TestData.USER_ID)))
            .thenReturn(Map.of());

        when(jwtManager.createAccessToken(eq(user), anyMap(), anyMap(), anyLong()))
                .thenReturn("new-access");

        when(jwtManager.createRefreshToken(eq(TestData.USER_ID), eq(email), anyString(), eq(604800L)))
                .thenReturn(refreshId.toString());

        // Act
        SessionManager.TokenRenewalResponse response =
                sessionManager.renewTokens(new Cookie.Builder(SessionManager.REFRESH_COOKIE_NAME)
                    .value(refreshTokenValue).build());

        // Assert
        assertEquals("new-access", response.getAccessToken().getValue());
        assertEquals(refreshId.toString(), response.getRefreshToken().getValue());

        verify(controller).requireValidRefreshToken(TestData.USER_ID, refreshId);
        verify(jwtParser).parse(refreshTokenValue);
        verify(jwtManager).createAccessToken(eq(user), anyMap(), anyMap(), anyLong());
        verify(jwtManager).createRefreshToken(eq(TestData.USER_ID), eq(email), anyString(), eq(604800L));
    }

    @Test
    void test_renewTokens_throws_whenCookieMissing() {
        assertThrows(UnauthorizedException.class, () -> sessionManager.renewTokens(null));
    }

    @Test
    void test_generateAccessToken_wrapsCookie() {
        UserEntity user = new UserEntity();
        user.setId(TestData.USER_ID);
        user.setEmail(TestData.USER_EMAIL);
        user.setFirstName("Jane");
        user.setLastName("Roe");
        user.setTokenId("active");

        when(controller.getAuthenticatedUser(TestData.USER_ID)).thenReturn(user);
        when(controller.getProjectAuthorization(eq(TestData.USER_ID)))
            .thenReturn(createMemberships("MANAGER", "STAFF"));
        when(controller.getTenancyAuthorization(eq(TestData.USER_ID)))
            .thenReturn(Map.of());

        when(jwtManager.createAccessToken(eq(user),
            anyMap(), anyMap(), anyLong()))
                .thenReturn("access.jwt");

        // Act
        NewCookie cookie = sessionManager.generateAccessToken(TestData.USER_ID, TestData.USER_EMAIL);

        // Assert
        assertEquals("access.jwt", cookie.getValue());
        assertEquals(SessionManager.ACCESS_COOKIE_NAME, cookie.getName());
        assertFalse(cookie.isHttpOnly());
        assertTrue(cookie.getPath().startsWith("/"));
    }

    @Test
    void test_generateRefreshToken_persistsIdAndWrapsCookie() {
        UserEntity user = new UserEntity();
        user.setId(TestData.USER_ID);

        when(controller.hasUserRefreshToken(TestData.USER_ID)).thenReturn(true);
        when(jwtManager.createRefreshToken(eq(TestData.USER_ID), eq("u1@example.com"), anyString(), eq(604800L)))
                .thenReturn("refresh.jwt");

        // Act
        NewCookie cookie = sessionManager.generateRefreshToken(TestData.USER_ID, "u1@example.com");

        // Assert
        assertEquals(SessionManager.REFRESH_COOKIE_NAME, cookie.getName());
        assertEquals("refresh.jwt", cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.getPath().startsWith("/api"));

        verify(controller).createRefreshToken(eq(TestData.USER_ID), any(UUID.class));
    }

    @Test
    void test_cookiePaths_areDifferentForAccessAndRefresh() {
        // Setup for access token
        UserEntity user = new UserEntity();
        user.setId(TestData.USER_ID);
        user.setEmail(TestData.USER_EMAIL);
        user.setFirstName("Jane");
        user.setLastName("Roe");
        user.setTokenId("active");

        when(controller.getAuthenticatedUser(TestData.USER_ID)).thenReturn(user);
        when(controller.getProjectAuthorization(eq(TestData.USER_ID)))
                .thenReturn(createMemberships("MANAGER", "STAFF"));
        when(controller.getTenancyAuthorization(eq(TestData.USER_ID)))
                .thenReturn(Map.of());
        when(jwtManager.createAccessToken(eq(user), anyMap(), anyMap(), eq(300L)))
                .thenReturn("access.jwt");
        when(jwtManager.createRefreshToken(eq(TestData.USER_ID), eq(TestData.USER_EMAIL), anyString(), eq(604800L)))
                .thenReturn("refresh.jwt");

        // Act
        NewCookie accessCookie = sessionManager.generateAccessToken(TestData.USER_ID, TestData.USER_EMAIL);
        NewCookie refreshCookie = sessionManager.generateRefreshToken(TestData.USER_ID, TestData.USER_EMAIL);

        // Assert - paths should be different
        assertFalse(accessCookie.getPath().equals(refreshCookie.getPath()),
                "Access and refresh tokens should have different paths");
        assertTrue(accessCookie.getPath().startsWith("/"),
                "Access token path should start with /");
        assertTrue(refreshCookie.getPath().startsWith("/api"),
                "Refresh token path should start with /api");
    }

    @Test
    void test_logout_deletesPersistedRefreshToken_whenCookiePresent() throws ParseException {
        String refreshTokenValue = "refresh.jwt";

        when(jwtParser.parse(refreshTokenValue)).thenReturn(fakeRefreshJwt(TestData.USER_ID, "e@x", "r1"));

        sessionManager.logout(Map.of(SessionManager.REFRESH_COOKIE_NAME,
                new Cookie.Builder(SessionManager.REFRESH_COOKIE_NAME).value(refreshTokenValue).build()));

        verify(controller).deleteRefreshToken(TestData.USER_ID);
    }

    @Test
    void test_findTokenCookie_returnsCookie_whenPresent() {
        Cookie cookieA = new Cookie.Builder(SessionManager.ACCESS_COOKIE_NAME).value("cookieA").build();
        Cookie cookieR = new Cookie.Builder(SessionManager.REFRESH_COOKIE_NAME).value("cookieR").build();

        assertEquals("cookieA", sessionManager.findAccessTokenCookie(Map.of(
                SessionManager.ACCESS_COOKIE_NAME, cookieA)).getValue());
        assertEquals("cookieR", sessionManager.findRefreshTokenCookie(Map.of(
                SessionManager.REFRESH_COOKIE_NAME, cookieR)).getValue());
    }

    @Test
    void test_generateAccessToken_throws_whenUserMissing() {
        when(controller.getAuthenticatedUser(TestData.USER_ID_4)).thenThrow(UnauthorizedException.class);  
        assertThrows(UnauthorizedException.class, () -> sessionManager.generateAccessToken(TestData.USER_ID_4, "x@x"));
        verify(jwtManager, never()).createAccessToken(any(UserEntity.class), anyMap(), anyMap(), anyLong());
    }

    @Test
    void test_needsRenewal_returnsTrueWhenTokenNull() {
        assertTrue(sessionManager.needsRenewal(null));
    }

    @Test
    void test_needsRenewal_returnsTrueWhenTokenExpiresSoon() throws ParseException {
        // Create a token that expires in 4 minutes (240 seconds)
        long currentTime = System.currentTimeMillis() / 1000;
        long expirationTime = currentTime + 240;

        JsonWebToken mockJwt = mock(JsonWebToken.class);
        when(mockJwt.getExpirationTime()).thenReturn(expirationTime);

        Cookie accessToken = new Cookie.Builder(SessionManager.ACCESS_COOKIE_NAME).value("token.jwt").build();
        when(jwtParser.parse("token.jwt")).thenReturn(mockJwt);

        assertTrue(sessionManager.needsRenewal(accessToken));
    }

    @Test
    void test_needsRenewal_returnsFalseWhenTokenStillValid() throws ParseException {
        // Create a token that expires in 10 minutes (600 seconds)
        long currentTime = System.currentTimeMillis() / 1000;
        long expirationTime = currentTime + 600;

        JsonWebToken mockJwt = mock(JsonWebToken.class);
        when(mockJwt.getExpirationTime()).thenReturn(expirationTime);

        Cookie accessToken = new Cookie.Builder(SessionManager.ACCESS_COOKIE_NAME).value("token.jwt").build();
        when(jwtParser.parse("token.jwt")).thenReturn(mockJwt);

        assertFalse(sessionManager.needsRenewal(accessToken));
    }

    @Test
    void test_needsRenewal_returnsTrueWhenTokenInvalid() throws ParseException {
        Cookie accessToken = new Cookie.Builder(SessionManager.ACCESS_COOKIE_NAME).value("invalid.jwt").build();
        when(jwtParser.parse("invalid.jwt")).thenThrow(new ParseException("Invalid token"));

        assertTrue(sessionManager.needsRenewal(accessToken));
    }

}