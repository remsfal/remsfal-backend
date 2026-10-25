package de.remsfal.service.entity.dao;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import de.remsfal.service.entity.dto.EmailLoginCodeEntity;

@ApplicationScoped
public class EmailLoginCodeRepository extends AbstractRepository<EmailLoginCodeEntity> {

    private static final String PARAM_TIMESTAMP = "timestamp";

    /**
     * Stores a new code and invalidates all older codes of the email address.
     */
    @Transactional(TxType.REQUIRES_NEW)
    public EmailLoginCodeEntity create(final String email, final String codeHash, final String codeSalt,
        final LocalDateTime expiresAt, final LocalDateTime timestamp) {
        invalidateActiveByEmail(email, timestamp);
        final EmailLoginCodeEntity code = new EmailLoginCodeEntity();
        code.generateId();
        code.setEmail(email);
        code.setCodeHash(codeHash);
        code.setCodeSalt(codeSalt);
        code.setExpiresAt(expiresAt);
        persist(code);
        return code;
    }

    /** The latest code of the email address that is neither used nor expired. */
    public Optional<EmailLoginCodeEntity> findActiveByEmail(final String email, final LocalDateTime now) {
        return find("email = ?1 and usedAt is null and expiresAt > ?2 order by createdAt desc",
            email.trim().toLowerCase(), now).firstResultOptional();
    }

    @Transactional
    public int invalidateActiveByEmail(final String email, final LocalDateTime timestamp) {
        return getEntityManager().createNamedQuery("EmailLoginCodeEntity.invalidateActiveByEmail")
            .setParameter("email", email.trim().toLowerCase())
            .setParameter(PARAM_TIMESTAMP, timestamp)
            .executeUpdate();
    }

    /**
     * Counts an attempt to enter the code. The update is atomic, so concurrent requests cannot exceed the
     * maximum number of attempts.
     *
     * @return false if the code is used, expired or has no attempts left
     */
    @Transactional
    public boolean incrementAttempts(final UUID id, final int maxAttempts, final LocalDateTime timestamp) {
        return getEntityManager().createNamedQuery("EmailLoginCodeEntity.incrementAttempts")
            .setParameter(PARAM_ID, id)
            .setParameter("maxAttempts", maxAttempts)
            .setParameter(PARAM_TIMESTAMP, timestamp)
            .executeUpdate() > 0;
    }

    /**
     * Marks the code as used.
     *
     * @return false if the code was already used by a concurrent request
     */
    @Transactional
    public boolean markUsed(final UUID id, final LocalDateTime timestamp) {
        return getEntityManager().createNamedQuery("EmailLoginCodeEntity.markUsed")
            .setParameter(PARAM_ID, id)
            .setParameter(PARAM_TIMESTAMP, timestamp)
            .executeUpdate() > 0;
    }

    @Transactional
    public int deleteExpiredOrUsed(final LocalDateTime timestamp) {
        return getEntityManager().createNamedQuery("EmailLoginCodeEntity.deleteExpiredOrUsed")
            .setParameter(PARAM_TIMESTAMP, timestamp)
            .executeUpdate();
    }

}
