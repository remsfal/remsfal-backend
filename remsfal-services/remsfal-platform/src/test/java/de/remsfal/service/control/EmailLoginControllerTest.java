package de.remsfal.service.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import de.remsfal.service.AbstractServiceTest;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class EmailLoginControllerTest extends AbstractServiceTest {

    @Inject
    EmailLoginController controller;

    private void insertCode(final String expiresAt, final String usedAt) {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO email_login_codes (id, email, code_hash, code_salt, expires_at, used_at) "
                + "VALUES (?, 'cleanup@example.com', 'hash', 'salt', " + expiresAt + ", " + usedAt + ")")
            .setParameter(1, UUID.randomUUID())
            .executeUpdate());
    }

    private void insertAttempt(final String createdAt) {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO email_login_attempts (id, email, ip_address, type, result, created_at) "
                + "VALUES (?, 'cleanup@example.com', '127.0.0.1', 'REQUEST', 'SENT', " + createdAt + ")")
            .setParameter(1, UUID.randomUUID())
            .executeUpdate());
    }

    private long count(final String entity) {
        return entityManager.createQuery("SELECT count(e) FROM " + entity + " e", Long.class).getSingleResult();
    }

    @Test
    void cleanup_SUCCESS_deletesExpiredAndUsedCodes() {
        insertCode("now() + interval '10 minutes'", "null");
        insertCode("now() - interval '1 minute'", "null");
        insertCode("now() + interval '10 minutes'", "now()");

        controller.cleanup();

        assertEquals(1L, count("EmailLoginCodeEntity"));
    }

    @Test
    void cleanup_SUCCESS_deletesAttemptsAfterRetention() {
        insertAttempt("now()");
        insertAttempt("now() - interval '29 days'");
        insertAttempt("now() - interval '31 days'");

        controller.cleanup();

        assertEquals(2L, count("EmailLoginAttemptEntity"));
    }

}
