package de.remsfal.service.boundary.authentication;

import de.remsfal.common.authentication.JWTManager;
import de.remsfal.common.authentication.SessionInfo;
import de.remsfal.common.authentication.TokenExpiredException;
import de.remsfal.common.authentication.UnauthorizedException;
import de.remsfal.service.entity.dao.UserAuthenticationRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.UserAuthenticationEntity;
import de.remsfal.service.entity.dto.UserEntity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class SessionManagerTest {

    SessionManager sessionManager;

    UserAuthenticationRepository userAuthRepository;

    UserRepository userRepository;

    JWTManager jwtManager;

    @BeforeEach
    void setup() {
        userRepository = Mockito.mock(UserRepository.class);
        userAuthRepository = Mockito.mock(UserAuthenticationRepository.class);
        jwtManager = Mockito.mock(JWTManager.class);

        sessionManager = new SessionManager("/", NewCookie.SameSite.STRICT, Duration.ofMinutes(5), Duration.ofDays(7),
            jwtManager, userAuthRepository, userRepository);
    }


    @Test
    void checkValidUserSession_withExpiredAccessToken_andValidRefreshToken_shouldPass() {
        // Arrange
        String userId = "testUser";
        String email = "test@example.com";
        String expiredAccessToken = "expiredAccessToken";
        String validRefreshToken = "validRefreshToken";

        Cookie accessTokenCookie =
            new Cookie.Builder(SessionManager.ACCESS_COOKIE_NAME).value(expiredAccessToken).build();
        Cookie refreshTokenCookie =
            new Cookie.Builder(SessionManager.REFRESH_COOKIE_NAME).value(validRefreshToken).build();

        // Simuliere JWTManager Verhalten
        when(jwtManager.verifyJWT(expiredAccessToken)).thenThrow(new TokenExpiredException("Expired"));
        when(jwtManager.verifyJWT(validRefreshToken, true)).thenReturn(SessionInfo.builder()
            .userId(userId)
            .userEmail(email)
            .expireAfter(Duration.ofMinutes(5))
            .claim("refreshToken", validRefreshToken)
            .build());
        UserAuthenticationEntity userAuthEntity = new UserAuthenticationEntity();
        userAuthEntity.setRefreshToken(validRefreshToken);
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail(email);
        userAuthEntity.setUser(userEntity);
        when(userAuthRepository.findByUserId(userId)).thenReturn(Optional.of(userAuthEntity));

        // Act
        SessionInfo sessionInfo = sessionManager.checkValidUserSession(Map.of(
            SessionManager.ACCESS_COOKIE_NAME, accessTokenCookie,
            SessionManager.REFRESH_COOKIE_NAME, refreshTokenCookie)
        );

        // Assert
        assertNotNull(sessionInfo);
        assertEquals(userId, sessionInfo.getUserId());
    }

    @Test
    void checkValidUserSession_withInvalidRefreshToken_shouldThrowException() {
        // Arrange
        String expiredAccessToken = "expiredAccessToken";
        String invalidRefreshToken = "invalidRefreshToken";

        Cookie accessTokenCookie =
            new Cookie.Builder(SessionManager.ACCESS_COOKIE_NAME).value(expiredAccessToken).build();
        Cookie refreshTokenCookie =
            new Cookie.Builder(SessionManager.REFRESH_COOKIE_NAME).value(invalidRefreshToken).build();

        when(jwtManager.verifyJWT(expiredAccessToken)).thenThrow(new TokenExpiredException("Expired"));
        when(jwtManager.verifyJWT(invalidRefreshToken, true)).thenThrow(new UnauthorizedException("Invalid"));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
            sessionManager.checkValidUserSession(Map.of(
                SessionManager.ACCESS_COOKIE_NAME, accessTokenCookie,
                SessionManager.REFRESH_COOKIE_NAME, refreshTokenCookie)
            )
        );
    }

    @Test
    void test_renewTokens() {
        // Arrange
        String userId = "testUser";
        String email = "email@email.de";
        String refreshToken = "refresh_token";
        String accessToken = "access_token";

        Cookie refreshCookie = new Cookie.Builder(SessionManager.REFRESH_COOKIE_NAME).value(refreshToken).build();
        when(jwtManager.verifyJWT(refreshToken, true)).thenReturn(SessionInfo.builder()
            .userId(userId)
            .userEmail(email)
            .expireAfter(Duration.ofMinutes(5))
            .claim("refreshToken", refreshToken)
            .build());
        UserAuthenticationEntity userAuthEntity = new UserAuthenticationEntity();
        userAuthEntity.setRefreshToken(refreshToken);
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail(email);
        userAuthEntity.setUser(userEntity);
        when(userAuthRepository.findByUserId(userId)).thenReturn(Optional.of(userAuthEntity));
        when(jwtManager.createJWT(any())).thenReturn(accessToken).thenReturn(refreshToken);

        // Act
        SessionManager.TokenRenewalResponse response =
            sessionManager.renewTokens(Map.of(SessionManager.REFRESH_COOKIE_NAME, refreshCookie));

        // Assert
        assertEquals(accessToken, response.getAccessToken().getValue());
        assertEquals(refreshToken, response.getRefreshToken().getValue());


        verify(userAuthRepository, times(2)).findByUserId(userId);
        verify(jwtManager, times(1)).verifyJWT(refreshToken, true);
        verify(jwtManager, times(2)).createJWT(any());

    }

    @Test
    void test_renewTokens_noRefreshToken_shouldThrowException() {
        // Arrange
        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> sessionManager.renewTokens(Map.of()));
    }

    @Test
    void test_renewTokens_invalidRefreshToken_shouldThrowException() {
        // Arrange
        String refreshToken = "refresh_token";
        Cookie refreshCookie = new Cookie.Builder(SessionManager.REFRESH_COOKIE_NAME).value(refreshToken).build();
        when(jwtManager.verifyJWT(refreshToken, true)).thenThrow(new UnauthorizedException("Invalid"));

        // Act & Assert
        assertThrows(UnauthorizedException.class,
            () -> sessionManager.renewTokens(Map.of(SessionManager.REFRESH_COOKIE_NAME, refreshCookie)));

        verify(jwtManager, times(1)).verifyJWT(refreshToken, true);

    }

    @Test
    void test_decryptAccessTokenCookie() {
        // Arrange
        String accessToken = "abc";
        when(jwtManager.verifyJWT(accessToken)).thenReturn(
            SessionInfo.builder().userId("test").userEmail("1234@1234.de").build());
        Cookie cookie = new Cookie.Builder(SessionManager.ACCESS_COOKIE_NAME).value(accessToken).build();

        // Act
        SessionInfo sessionInfo = sessionManager.decryptAccessTokenCookie(cookie);

        // Assert
        assertNotNull(sessionInfo);
        assertEquals("test", sessionInfo.getUserId());
        assertEquals("1234@1234.de", sessionInfo.getUserEmail());

        verify(jwtManager, times(1)).verifyJWT(accessToken);

    }

    @Test
    void test_decryptRefreshTokenCookie() {
        // Arrange
        String refreshToken = "abc";
        when(jwtManager.verifyJWT(refreshToken, true)).thenReturn(
            SessionInfo.builder().userId("test").userEmail("1234@1234.de").build());
        Cookie cookie = new Cookie.Builder(SessionManager.REFRESH_COOKIE_NAME).value(refreshToken).build();

        // Act
        SessionInfo sessionInfo = sessionManager.decryptRefreshTokenCookie(cookie);

        // Assert
        assertNotNull(sessionInfo);
        assertEquals("test", sessionInfo.getUserId());
        assertEquals("1234@1234.de", sessionInfo.getUserEmail());

        verify(jwtManager, times(1)).verifyJWT(refreshToken, true);

    }

    @Test
    void test_refreshcookie_get_deleted_when_logout() {
        // Arrange
        String userId = "testUser";
        String email = "1234@1234.de";
        String refreshToken = "refresh_token";

        UserAuthenticationEntity userAuthEntity = new UserAuthenticationEntity();
        userAuthEntity.setRefreshToken(refreshToken);
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail(email);
        userAuthEntity.setUser(userEntity);
        when(userAuthRepository.findByUserId(userId)).thenReturn(Optional.of(userAuthEntity));
        when(jwtManager.verifyJWT(any(), eq(true))).thenReturn(SessionInfo.builder()
            .userId(userId)
            .userEmail(email)
            .expireAfter(Duration.ofMinutes(5))
            .claim("refreshToken", refreshToken)
            .build());


        // Act
        sessionManager.logout(Map.of(SessionManager.ACCESS_COOKIE_NAME,
            new Cookie.Builder(SessionManager.ACCESS_COOKIE_NAME).value("abc").build(),
            SessionManager.REFRESH_COOKIE_NAME,
            new Cookie.Builder(SessionManager.REFRESH_COOKIE_NAME).value("abc").build()));


        // Assert
        verify(userAuthRepository, times(1)).deleteRefreshToken(userId);
    }

    @Test
    void test_get_macthing_sessionInfoBuilder() {
        // Arrange

        // Act
        SessionInfo.Builder builder_acess = sessionManager.sessionInfoBuilder(SessionManager.ACCESS_COOKIE_NAME);
        SessionInfo.Builder builder_refresh = sessionManager.sessionInfoBuilder(SessionManager.REFRESH_COOKIE_NAME);

        // Assert
        assertNotNull(builder_acess);
        assert builder_acess.build().getExpireInSeconds() <= 60 * 5;
        assertNotNull(builder_refresh);
        assert builder_refresh.build().getExpireInSeconds() > 60 * 60 * 24 * 6;

    }

    @Test
    void test_valid_usersession_when_accessToken_is_expired_and_refreshToken_is_valid() {
        // Arrange
        String userId = "testUser";
        String email = "1234@1234.de";
        SessionInfo sessionInfo = SessionInfo.builder()
            .userId(userId)
            .userEmail(email)
            .expireAfter(Duration.ofMinutes(5))
            .claim("refreshToken", "refresh_token")
            .build();

        NewCookie refreshToken =
            new NewCookie.Builder(SessionManager.REFRESH_COOKIE_NAME).value("refresh_token").build();
        NewCookie accessToken =
            new NewCookie.Builder(SessionManager.ACCESS_COOKIE_NAME).value("expiredAccessToken").build();
        when(jwtManager.verifyJWT("expiredAccessToken")).thenThrow(new TokenExpiredException("Expired"));
        when(jwtManager.verifyJWT("refresh_token", true)).thenReturn(sessionInfo);

        UserAuthenticationEntity userAuthEntity = new UserAuthenticationEntity();
        userAuthEntity.setRefreshToken("refresh_token");
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail(email);
        userAuthEntity.setUser(userEntity);
        when(userAuthRepository.findByUserId(userId)).thenReturn(Optional.of(userAuthEntity));

        // Act
        SessionInfo result = sessionManager.checkValidUserSession(Map.of(
            SessionManager.ACCESS_COOKIE_NAME, accessToken,
            SessionManager.REFRESH_COOKIE_NAME, refreshToken)
        );

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(email, result.getUserEmail());
    }

    @Test
    void test_renewTokens_with_no_refreshToken_should_throw_exception() {
        // Arrange
        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> sessionManager.renewTokens(Map.of()));
    }

    @Test
    void test_renewToken_returns_new_tokens() {
        // Arrange
        String userId = "testUser";
        String email = "1234@1234.de";
        String refreshToken = "refresh_token";

        Cookie refreshCookie = new Cookie.Builder(SessionManager.REFRESH_COOKIE_NAME).value(refreshToken).build();
        when(jwtManager.verifyJWT(refreshToken, true)).thenReturn(SessionInfo.builder()
            .userId(userId)
            .userEmail(email)
            .expireAfter(Duration.ofMinutes(5))
            .claim("refreshToken", refreshToken)
            .build());
        UserAuthenticationEntity userAuthEntity = new UserAuthenticationEntity();
        userAuthEntity.setRefreshToken(refreshToken);
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail(email);
        userAuthEntity.setUser(userEntity);
        when(userAuthRepository.findByUserId(userId)).thenReturn(Optional.of(userAuthEntity));
        when(jwtManager.createJWT(any())).thenReturn("new_access_token").thenReturn("new_refresh_token");

        // Act
        SessionManager.TokenRenewalResponse response =
            sessionManager.renewTokens(Map.of(SessionManager.REFRESH_COOKIE_NAME, refreshCookie));

        // Assert
        assertEquals("new_access_token", response.getAccessToken().getValue());
        assertEquals("new_refresh_token", response.getRefreshToken().getValue());

        verify(userAuthRepository, times(2)).findByUserId(userId);
        verify(jwtManager, times(1)).verifyJWT(refreshToken, true);
        verify(jwtManager, times(2)).createJWT(any());

    }

    @Test
    void test_findAccessTokenCookie() {
        // Arrange
        String accessToken = "abc";

        Cookie cookie = new Cookie.Builder(SessionManager.ACCESS_COOKIE_NAME).value(accessToken).build();

        // Act
        Cookie result = sessionManager.findAccessTokenCookie(Map.of(SessionManager.ACCESS_COOKIE_NAME, cookie));

        // Assert
        assertNotNull(result);
        assertEquals(accessToken, result.getValue());

    }

    @Test
    void test_findRefreshTokenCookie() {
        // Arrange
        String refreshToken = "abc";

        Cookie cookie = new Cookie.Builder(SessionManager.REFRESH_COOKIE_NAME).value(refreshToken).build();

        // Act
        Cookie result = sessionManager.findRefreshTokenCookie(Map.of(SessionManager.REFRESH_COOKIE_NAME, cookie));

        // Assert
        assertNotNull(result);
        assertEquals(refreshToken, result.getValue());

    }

}
