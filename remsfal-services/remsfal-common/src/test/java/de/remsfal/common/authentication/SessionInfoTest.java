package de.remsfal.common.authentication;

import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTClaimsSet;

import de.remsfal.test.AbstractTest;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class SessionInfoTest extends AbstractTest {

    @Test
    void sessionInfo_shouldContainUserIdAndEmail() {
        String userId = "testUser";
        String userEmail = "test@example.com";
        SessionInfo sessionInfo = SessionInfo.builder()
            .userId(userId)
            .userEmail(userEmail)
            .expireAfter(Duration.ofMinutes(30))
            .build();

        assertEquals(userId, sessionInfo.getUserId());
        assertEquals(userEmail, sessionInfo.getUserEmail());
        assertFalse(sessionInfo.isExpired());
        assertTrue(sessionInfo.isValid());
    }

    @Test
    void sessionInfo_shouldExpireCorrectly() {
        SessionInfo sessionInfo = SessionInfo.builder()
            .userId("testUser")
            .userEmail("test@example.com")
            .expireAfter(Duration.ofMillis(500))
            .build();

        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertTrue(sessionInfo.isExpired());
        assertFalse(sessionInfo.isValid());
    }

    @Test
    void sessionInfo_shouldRenewBeforeExpiration() {

        SessionInfo sessionInfo = SessionInfo.builder()
            .userId("testUser")
            .userEmail("test@example.com")
            .expireAfter(Duration.ofMinutes(4))
            .build();

        boolean shouldRenew = sessionInfo.shouldRenew();

        assertTrue(shouldRenew);
    }

    @Test
    void sessionInfo_shouldNotRenewIfSufficientTimeLeft() {

        SessionInfo sessionInfo = SessionInfo.builder()
            .userId("testUser")
            .userEmail("test@example.com")
            .expireAfter(Duration.ofMinutes(10))
            .build();

        boolean shouldRenew = sessionInfo.shouldRenew();

        assertFalse(shouldRenew);
    }

    @Test
    void sessionInfo_shouldConvertToPayloadAndBack() throws ParseException {

        String userId = "testUser";
        String userEmail = "test@example.com";
        SessionInfo sessionInfo = SessionInfo.builder()
            .userId(userId)
            .userEmail(userEmail)
            .expireAfter(Duration.ofMinutes(30))
            .build();

        Payload payload = sessionInfo.toPayload();
        SessionInfo reconstructedSessionInfo = new SessionInfo(payload);

        assertEquals(sessionInfo.getUserId(), reconstructedSessionInfo.getUserId());
        assertEquals(sessionInfo.getUserEmail(), reconstructedSessionInfo.getUserEmail());
        assertFalse(reconstructedSessionInfo.isExpired());
    }

    @Test
    void sessionInfo_builderShouldCopyValuesCorrectly() {

        SessionInfo originalSessionInfo = SessionInfo.builder()
            .userId("originalUser")
            .userEmail("original@example.com")
            .expireAfter(Duration.ofMinutes(30))
            .build();

        SessionInfo copiedSessionInfo = SessionInfo.builder().from(originalSessionInfo).build();

        assertEquals(originalSessionInfo.getUserId(), copiedSessionInfo.getUserId());
        assertEquals(originalSessionInfo.getUserEmail(), copiedSessionInfo.getUserEmail());
        assertFalse(copiedSessionInfo.isExpired());
    }

    @Test
    void sessionInfo_shouldHandleNullExpirationTime() {

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .subject("testUser")
            .claim("email", "test@example.com")
            .build();
        SessionInfo sessionInfo = new SessionInfo(claimsSet);

        assertTrue(sessionInfo.isExpired());
        assertFalse(sessionInfo.isValid());
    }
}
