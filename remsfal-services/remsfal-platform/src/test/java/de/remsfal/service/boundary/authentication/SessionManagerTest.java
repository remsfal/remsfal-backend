package de.remsfal.service.boundary.authentication;

import de.remsfal.common.authentication.JWTManager;
import de.remsfal.common.authentication.UnauthorizedException;
import de.remsfal.service.entity.dto.UserEntity;
import de.remsfal.service.entity.dao.UserAuthenticationRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.UserAuthenticationEntity;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@QuarkusTest
class SessionManagerTest {

    SessionManager sessionManager;

    UserAuthenticationRepository userAuthRepository;

    UserRepository userRepository;

    JWTManager jwtManager;

    JWTParser jwtParser;

    @BeforeEach
    void setup() {
        userRepository = Mockito.mock(UserRepository.class);
        userAuthRepository = Mockito.mock(UserAuthenticationRepository.class);
        jwtManager = Mockito.mock(JWTManager.class);
        jwtParser = Mockito.mock(JWTParser.class);

        sessionManager = new SessionManager("/", NewCookie.SameSite.STRICT, Duration.ofMinutes(5),
            Duration.ofDays(7), jwtManager, userAuthRepository, userRepository, jwtParser);
    }

    private JsonWebToken fakeRefreshJwt(String subject, String email, String refreshId) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put(Claims.sub.name(), subject);
        claims.put("email", email);
        claims.put("refreshToken", refreshId);

        return new JsonWebToken() {
            @Override public String getSubject() { return subject; }
            @Override public String getName() { return subject; }
            @Override public Set<String> getClaimNames() { return new LinkedHashSet<>(claims.keySet()); }
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
        String userId = "u1";
        String email = "u1@example.com";
        String refreshId = "r-123";
        String refreshTokenValue = "refresh.jwt.token";

        Cookie refreshCookie = new Cookie.Builder(SessionManager.REFRESH_COOKIE_NAME).value(refreshTokenValue).build();

        when(jwtParser.parse(refreshTokenValue)).thenReturn(fakeRefreshJwt(userId, email, refreshId));

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setEmail(email);

        UserAuthenticationEntity auth = new UserAuthenticationEntity();
        auth.setUser(user);
        auth.setRefreshToken(refreshId);

        when(userAuthRepository.findByUserId(userId)).thenReturn(Optional.of(auth));
        when(userAuthRepository.findByUserId(userId)).thenReturn(Optional.of(auth)).thenReturn(Optional.of(auth));

        when(jwtManager.createAccessToken(userId, email, 300L)).thenReturn("new-access");
        when(jwtManager.createRefreshToken(eq(userId), eq(email), anyString(), eq(604800L)))
                .thenReturn("new-refresh");

        // Act
        SessionManager.TokenRenewalResponse response =
            sessionManager.renewTokens(Map.of(SessionManager.REFRESH_COOKIE_NAME, refreshCookie));

        // Assert
        assertEquals("new-access", response.getAccessToken().getValue());
        assertEquals("new-refresh", response.getRefreshToken().getValue());

        verify(jwtParser).parse(refreshTokenValue);
        verify(userAuthRepository, times(2)).findByUserId(userId);
        verify(jwtManager).createAccessToken(userId, email, 300L);
        verify(jwtManager).createRefreshToken(eq(userId), eq(email), anyString(), eq(604800L));
    }

    @Test
    void test_renewTokens_throws_whenCookieMissing() {
        assertThrows(UnauthorizedException.class, () -> sessionManager.renewTokens(Map.of()));
    }

    @Test
    void test_generateAccessToken_wrapsCookie() {
        when(jwtManager.createAccessToken("u1", "u1@example.com", 300L)).
                thenReturn("access.jwt");

        // Act
        NewCookie cookie = sessionManager.generateAccessToken("u1", "u1@example.com");

        // Assert
        assertEquals("access.jwt", cookie.getValue());
        assertEquals(SessionManager.ACCESS_COOKIE_NAME, cookie.getName());
        assertFalse(cookie.isHttpOnly());
    }

    @Test
    void test_generateRefreshToken_persistsIdAndWrapsCookie() {
        when(userAuthRepository.findByUserId("u1")).thenReturn(Optional.empty());

        UserEntity user = new UserEntity();
        user.setId("u1");

        when(userRepository.findByIdOptional("u1")).thenReturn(Optional.of(user));
        when(jwtManager.createRefreshToken(eq("u1"), eq("u1@example.com"), anyString(), eq(604800L)))
                .thenReturn("refresh.jwt");

        // Act
        NewCookie cookie = sessionManager.generateRefreshToken("u1", "u1@example.com");

        // Assert
        assertEquals(SessionManager.REFRESH_COOKIE_NAME, cookie.getName());
        assertEquals("refresh.jwt", cookie.getValue());
        assertTrue(cookie.isHttpOnly());

        verify(userAuthRepository).persist(any(UserAuthenticationEntity.class));
    }

    @Test
    void test_logout_deletesPersistedRefreshToken_whenCookiePresent() throws ParseException {
        String refreshTokenValue = "refresh.jwt";

        when(jwtParser.parse(refreshTokenValue)).thenReturn(fakeRefreshJwt("u1", "e@x", "r1"));

        sessionManager.logout(Map.of(SessionManager.REFRESH_COOKIE_NAME,
                new Cookie.Builder(SessionManager.REFRESH_COOKIE_NAME).value(refreshTokenValue).build()));

        verify(userAuthRepository).deleteRefreshToken("u1");
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

}
