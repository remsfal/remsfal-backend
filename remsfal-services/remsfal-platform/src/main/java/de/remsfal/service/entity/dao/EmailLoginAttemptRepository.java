package de.remsfal.service.entity.dao;

import java.time.LocalDateTime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import de.remsfal.service.entity.dto.EmailLoginAttemptEntity;
import de.remsfal.service.entity.dto.EmailLoginAttemptEntity.AttemptResult;
import de.remsfal.service.entity.dto.EmailLoginAttemptEntity.AttemptType;

@ApplicationScoped
public class EmailLoginAttemptRepository extends AbstractRepository<EmailLoginAttemptEntity> {

    /**
     * Logs an attempt in its own transaction, so it is kept even if the login fails afterwards.
     */
    @Transactional(TxType.REQUIRES_NEW)
    public void log(final String email, final String ipAddress, final AttemptType type,
        final AttemptResult result) {
        final EmailLoginAttemptEntity attempt = new EmailLoginAttemptEntity();
        attempt.generateId();
        attempt.setEmail(email);
        attempt.setIpAddress(ipAddress);
        attempt.setType(type);
        attempt.setResult(result);
        persist(attempt);
    }

    /** Counts the attempts of an email address, attempts rejected by the rate limit are not counted. */
    public long countByEmailSince(final String email, final AttemptType type, final LocalDateTime since) {
        return count("email = ?1 and type = ?2 and result <> ?3 and createdAt > ?4",
            email.trim().toLowerCase(), type, AttemptResult.RATE_LIMITED, since);
    }

    /** Counts the attempts of a client, attempts rejected by the rate limit are not counted. */
    public long countByIpAddressSince(final String ipAddress, final AttemptType type, final LocalDateTime since) {
        if (ipAddress == null) {
            return 0;
        }
        return count("ipAddress = ?1 and type = ?2 and result <> ?3 and createdAt > ?4",
            ipAddress, type, AttemptResult.RATE_LIMITED, since);
    }

    @Transactional
    public int deleteOlderThan(final LocalDateTime timestamp) {
        return getEntityManager().createNamedQuery("EmailLoginAttemptEntity.deleteOlderThan")
            .setParameter("timestamp", timestamp)
            .executeUpdate();
    }

}
