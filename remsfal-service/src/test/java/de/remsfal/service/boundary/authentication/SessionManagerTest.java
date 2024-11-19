package de.remsfal.service.boundary.authentication;

import de.remsfal.service.boundary.exception.UnauthorizedException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class SessionManagerTest {

    @Inject
    SessionManager sessionManager;

    @Test
    void encryptAndDecryptSessionObject_shouldWorkCorrectly() {
        SessionInfo sessionInfo = SessionInfo.builder()
                .userId("testUser")
                .userEmail("test@example.com")
                .expireAfter(Duration.ofMinutes(30))
                .build();

        String encrypted = sessionManager.encryptSessionObject(sessionInfo);
        SessionInfo decrypted = sessionManager.decryptSessionObject(encrypted);

        assertEquals(sessionInfo.getUserId(), decrypted.getUserId());
        assertEquals(sessionInfo.getUserEmail(), decrypted.getUserEmail());
        assertFalse(decrypted.isExpired());
    }

    @Test
    void encryptSessionCookie_shouldReturnValidCookie() {
        SessionInfo sessionInfo = SessionInfo.builder()
                .userId("testUser")
                .userEmail("test@example.com")
                .expireAfter(Duration.ofMinutes(30))
                .build();

        NewCookie cookie = sessionManager.encryptSessionCookie(sessionInfo);

        assertEquals(SessionManager.COOKIE_NAME, cookie.getName());
        assertNotNull(cookie.getValue());
        assertEquals("/;SameSite=Strict", cookie.getPath());
        assertEquals(30 * 60, cookie.getMaxAge());
    }

    @Test
    void decryptSessionCookie_shouldReturnValidSessionInfo() {
        SessionInfo sessionInfo = SessionInfo.builder()
                .userId("testUser")
                .userEmail("test@example.com")
                .expireAfter(Duration.ofMinutes(30))
                .build();
        NewCookie cookie = sessionManager.encryptSessionCookie(sessionInfo);

        SessionInfo decryptedSessionInfo = sessionManager.decryptSessionCookie(
                new Cookie.Builder(cookie.getName()).value(cookie.getValue()).build()
        );

        assertEquals(sessionInfo.getUserId(), decryptedSessionInfo.getUserId());
        assertEquals(sessionInfo.getUserEmail(), decryptedSessionInfo.getUserEmail());
    }

    @Test
    void renewSessionCookie_shouldExtendExpirationTime() {
        SessionInfo sessionInfo = SessionInfo.builder()
                .userId("testUser")
                .userEmail("test@example.com")
                .expireAfter(Duration.ofMinutes(1)) // Short expiration time
                .build();
        NewCookie cookie = sessionManager.encryptSessionCookie(sessionInfo);

        NewCookie renewedCookie = sessionManager.renewSessionCookie(
                sessionManager.decryptSessionCookie(new Cookie.Builder(cookie.getName()).value(cookie.getValue()).build())
        );

        assertNotEquals(cookie.getValue(), renewedCookie.getValue());
        assertEquals(SessionManager.COOKIE_NAME, renewedCookie.getName());
    }

    @Test
    void removalSessionCookie_shouldReturnEmptyCookie() {
        NewCookie removalCookie = sessionManager.removalSessionCookie();

        assertEquals(SessionManager.COOKIE_NAME, removalCookie.getName());
        assertEquals("", removalCookie.getValue());
        assertEquals(0, removalCookie.getMaxAge());
    }

    @Test
    void findSessionCookie_shouldReturnCorrectCookie() {
        Map<String, Cookie> cookies = new HashMap<>();
        cookies.put(SessionManager.COOKIE_NAME, new Cookie.Builder(SessionManager.COOKIE_NAME).value("testValue").build());

        Cookie foundCookie = sessionManager.findSessionCookie(cookies);

        assertNotNull(foundCookie);
        assertEquals("testValue", foundCookie.getValue());
    }

    @Test
    void decryptSessionCookie_shouldThrowExceptionForInvalidCookieName() {
        Cookie invalidCookie = new Cookie.Builder("invalidCookie").value("testValue").build();

        assertThrows(InternalServerErrorException.class, () ->
                sessionManager.decryptSessionCookie(invalidCookie)
        );
    }

    @Test
    void decryptSessionObject_shouldThrowUnauthorizedExceptionForInvalidObject() {
        String invalidSessionObject = "invalidSessionObject";

        assertThrows(UnauthorizedException.class, () ->
                sessionManager.decryptSessionObject(invalidSessionObject)
        );
    }
}
