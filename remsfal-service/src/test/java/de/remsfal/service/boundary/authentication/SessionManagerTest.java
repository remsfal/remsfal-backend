package de.remsfal.service.boundary.authentication;

import de.remsfal.service.boundary.exception.TokenExpiredException;
import de.remsfal.service.boundary.exception.UnauthorizedException;
import de.remsfal.service.entity.dao.UserAuthenticationRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.UserAuthenticationEntity;

import de.remsfal.service.entity.dto.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


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

        Cookie accessTokenCookie = new Cookie.Builder(SessionManager.ACCESS_COOKIE_NAME).value(expiredAccessToken).build();
        Cookie refreshTokenCookie = new Cookie.Builder(SessionManager.REFRESH_COOKIE_NAME).value(validRefreshToken).build();

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

        Cookie accessTokenCookie = new Cookie.Builder(SessionManager.ACCESS_COOKIE_NAME).value(expiredAccessToken).build();
        Cookie refreshTokenCookie = new Cookie.Builder(SessionManager.REFRESH_COOKIE_NAME).value(invalidRefreshToken).build();

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
        SessionManager.TokenRenewalResponse response = sessionManager.renewTokens(Map.of(SessionManager.REFRESH_COOKIE_NAME, refreshCookie));

        // Assert
        assertEquals(accessToken, response.getAccessToken().getValue());
        assertEquals(refreshToken, response.getRefreshToken().getValue());


        verify(userAuthRepository, times(2)).findByUserId(userId);
        verify(jwtManager, times(1)).verifyJWT(refreshToken, true);
        verify(jwtManager, times(2)).createJWT(any());

    }


}
