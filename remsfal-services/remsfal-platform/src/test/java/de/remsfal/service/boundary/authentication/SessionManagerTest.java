package de.remsfal.service.boundary.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;

import de.remsfal.common.authentication.JWTManager;
import de.remsfal.common.authentication.UnauthorizedException;
import de.remsfal.core.model.ProjectMemberModel;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dao.UserAuthenticationRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.ProjectEntity;
import de.remsfal.service.entity.dto.ProjectMembershipEntity;
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
    UserAuthenticationRepository userAuthRepository;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    ProjectRepository projectRepository;

    @InjectMock
    JWTManager jwtManager;

    @InjectMock
    JWTParser jwtParser;

    private List<ProjectMembershipEntity> createMemberships(String p1Role, String p2Role) {
        return List.of(
                mkMembership(TestData.PROJECT_ID_1, ProjectMemberModel.MemberRole.valueOf(p1Role)),
                mkMembership(TestData.PROJECT_ID_2, ProjectMemberModel.MemberRole.valueOf(p2Role))
        );
    }

    private ProjectMembershipEntity mkMembership(UUID projectId, ProjectMemberModel.MemberRole role) {
        ProjectEntity project = new ProjectEntity();
        setPrivate(project, "id", projectId);

        ProjectMembershipEntity membershipEntity = new ProjectMembershipEntity();
        membershipEntity.setProject(project);
        membershipEntity.setRole(role);
        return membershipEntity;
    }

    private static void setPrivate(ProjectEntity projectId, String fieldName, UUID value) {
        try {
            Field f = projectId.getClass().getSuperclass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(projectId, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field '" + fieldName + "' on " + projectId.getClass(), e);
        }
    }

    private JsonWebToken fakeRefreshJwt(UUID subject, String email, String refreshId) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put(Claims.sub.name(), subject.toString());
        claims.put("email", email);
        claims.put("refreshToken", refreshId);

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
        String refreshId = "r-123";
        String refreshTokenValue = "refresh.jwt.token";

        when(jwtParser.parse(refreshTokenValue)).thenReturn(fakeRefreshJwt(TestData.USER_ID, email, refreshId));

        UserEntity user = new UserEntity();
        user.setId(TestData.USER_ID);
        user.setEmail(email);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setTokenId("active-token");

        UserAuthenticationEntity auth = new UserAuthenticationEntity();
        auth.setUser(user);
        auth.setRefreshToken(refreshId);

        when(userAuthRepository.findByUserId(TestData.USER_ID)).thenReturn(Optional.of(auth));
        when(userRepository.findByIdOptional(TestData.USER_ID)).thenReturn(Optional.of(user));
        when(projectRepository.findMembershipByUserId(eq(TestData.USER_ID), anyInt(), anyInt()))
                .thenReturn(createMemberships("MANAGER", "STAFF"));

        when(jwtManager.createAccessToken(eq(user),
                argThat(map -> "MANAGER".equals(map.get(TestData.PROJECT_ID_1.toString()))
                    && "STAFF".equals(map.get(TestData.PROJECT_ID_2.toString()))), anyMap(), eq(300L)))
                .thenReturn("new-access");

        when(jwtManager.createRefreshToken(eq(TestData.USER_ID), eq(email), anyString(), eq(604800L)))
                .thenReturn("new-refresh");

        // Act
        SessionManager.TokenRenewalResponse response =
                sessionManager.renewTokens(Map.of(SessionManager.REFRESH_COOKIE_NAME,
                        new Cookie.Builder(SessionManager.REFRESH_COOKIE_NAME).value(refreshTokenValue).build()));

        // Assert
        assertEquals("new-access", response.getAccessToken().getValue());
        assertEquals("new-refresh", response.getRefreshToken().getValue());

        verify(jwtParser).parse(refreshTokenValue);
        verify(userAuthRepository, times(2)).findByUserId(TestData.USER_ID);
        verify(userRepository).findByIdOptional(TestData.USER_ID);
        verify(projectRepository).findMembershipByUserId(eq(TestData.USER_ID), anyInt(), anyInt());
        verify(jwtManager).createAccessToken(eq(user),
                argThat(map -> "MANAGER".equals(map.get(TestData.PROJECT_ID_1.toString()))
                    && "STAFF".equals(map.get(TestData.PROJECT_ID_2.toString()))), anyMap(), eq(300L));
        verify(jwtManager).createRefreshToken(eq(TestData.USER_ID), eq(email), anyString(), eq(604800L));
    }

    @Test
    void test_renewTokens_throws_whenCookieMissing() {
        assertThrows(UnauthorizedException.class, () -> sessionManager.renewTokens(Map.of()));
    }

    @Test
    void test_generateAccessToken_wrapsCookie() {
        UserEntity user = new UserEntity();
        user.setId(TestData.USER_ID);
        user.setEmail(TestData.USER_EMAIL);
        user.setFirstName("Jane");
        user.setLastName("Roe");
        user.setTokenId("active");

        when(userRepository.findByIdOptional(TestData.USER_ID)).thenReturn(Optional.of(user));
        when(projectRepository.findMembershipByUserId(eq(TestData.USER_ID), anyInt(), anyInt()))
                .thenReturn(createMemberships("MANAGER", "STAFF"));

        when(jwtManager.createAccessToken(eq(user),
                argThat(map -> "MANAGER".equals(map.get(TestData.PROJECT_ID_1.toString()))
                    && "STAFF".equals(map.get(TestData.PROJECT_ID_2.toString()))),
                anyMap(), eq(300L)))
                .thenReturn("access.jwt");

        // Act
        NewCookie cookie = sessionManager.generateAccessToken(TestData.USER_ID, TestData.USER_EMAIL);

        // Assert
        assertEquals("access.jwt", cookie.getValue());
        assertEquals(SessionManager.ACCESS_COOKIE_NAME, cookie.getName());
        assertFalse(cookie.isHttpOnly());
        assertTrue(cookie.getPath().startsWith("/api"));
    }

    @Test
    void test_generateRefreshToken_persistsIdAndWrapsCookie() {
        when(userAuthRepository.findByUserId(TestData.USER_ID)).thenReturn(Optional.empty());

        UserEntity user = new UserEntity();
        user.setId(TestData.USER_ID);

        when(userRepository.findByIdOptional(TestData.USER_ID)).thenReturn(Optional.of(user));
        when(jwtManager.createRefreshToken(eq(TestData.USER_ID), eq("u1@example.com"), anyString(), eq(604800L)))
                .thenReturn("refresh.jwt");

        // Act
        NewCookie cookie = sessionManager.generateRefreshToken(TestData.USER_ID, "u1@example.com");

        // Assert
        assertEquals(SessionManager.REFRESH_COOKIE_NAME, cookie.getName());
        assertEquals("refresh.jwt", cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.getPath().startsWith("/api/v1/authentication"));

        verify(userAuthRepository).persist(any(UserAuthenticationEntity.class));
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

        when(userRepository.findByIdOptional(TestData.USER_ID)).thenReturn(Optional.of(user));
        when(projectRepository.findMembershipByUserId(eq(TestData.USER_ID), anyInt(), anyInt()))
                .thenReturn(createMemberships("MANAGER", "STAFF"));
        when(jwtManager.createAccessToken(eq(user), anyMap(), anyMap(), eq(300L)))
                .thenReturn("access.jwt");

        // Setup for refresh token
        when(userAuthRepository.findByUserId(TestData.USER_ID)).thenReturn(Optional.empty());
        when(jwtManager.createRefreshToken(eq(TestData.USER_ID), eq(TestData.USER_EMAIL), anyString(), eq(604800L)))
                .thenReturn("refresh.jwt");

        // Act
        NewCookie accessCookie = sessionManager.generateAccessToken(TestData.USER_ID, TestData.USER_EMAIL);
        NewCookie refreshCookie = sessionManager.generateRefreshToken(TestData.USER_ID, TestData.USER_EMAIL);

        // Assert - paths should be different
        assertFalse(accessCookie.getPath().equals(refreshCookie.getPath()),
                "Access and refresh tokens should have different paths");
        assertTrue(accessCookie.getPath().startsWith("/api"),
                "Access token path should start with /api");
        assertTrue(refreshCookie.getPath().startsWith("/api/v1/authentication"),
                "Refresh token path should start with /api/v1/authentication");
    }

    @Test
    void test_logout_deletesPersistedRefreshToken_whenCookiePresent() throws ParseException {
        String refreshTokenValue = "refresh.jwt";

        when(jwtParser.parse(refreshTokenValue)).thenReturn(fakeRefreshJwt(TestData.USER_ID, "e@x", "r1"));

        sessionManager.logout(Map.of(SessionManager.REFRESH_COOKIE_NAME,
                new Cookie.Builder(SessionManager.REFRESH_COOKIE_NAME).value(refreshTokenValue).build()));

        verify(userAuthRepository).deleteRefreshToken(TestData.USER_ID);
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
        when(userRepository.findByIdOptional(TestData.USER_ID_4)).thenReturn(Optional.empty());
        assertThrows(UnauthorizedException.class, () -> sessionManager.generateAccessToken(TestData.USER_ID_4, "x@x"));
        verify(jwtManager, never()).createAccessToken(any(UserEntity.class), anyMap(), anyMap(), anyLong());
    }

}