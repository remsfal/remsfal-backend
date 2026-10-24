package de.remsfal.service.entity.dto;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import de.remsfal.service.entity.dto.superclass.AbstractEntity;

/**
 * A one-time code of the passwordless email login. Only the salted hash of the code is stored. The code is not
 * related to a user, since the user may not exist before the first login.
 */
@NamedQuery(name = "EmailLoginCodeEntity.invalidateActiveByEmail",
    query = "update EmailLoginCodeEntity code set code.usedAt = :timestamp "
        + "where code.email = :email and code.usedAt is null")
@NamedQuery(name = "EmailLoginCodeEntity.incrementAttempts",
    query = "update EmailLoginCodeEntity code set code.attempts = code.attempts + 1 "
        + "where code.id = :id and code.usedAt is null and code.expiresAt > :timestamp "
        + "and code.attempts < :maxAttempts")
@NamedQuery(name = "EmailLoginCodeEntity.markUsed",
    query = "update EmailLoginCodeEntity code set code.usedAt = :timestamp "
        + "where code.id = :id and code.usedAt is null")
@NamedQuery(name = "EmailLoginCodeEntity.deleteExpiredOrUsed",
    query = "delete from EmailLoginCodeEntity code "
        + "where code.expiresAt < :timestamp or code.usedAt is not null")
@Entity
@Table(name = "email_login_codes")
public class EmailLoginCodeEntity extends AbstractEntity {

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    @Column(name = "code_salt", nullable = false)
    private String codeSalt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email == null ? null : email.trim().toLowerCase();
    }

    public String getCodeHash() {
        return codeHash;
    }

    public void setCodeHash(final String codeHash) {
        this.codeHash = codeHash;
    }

    public String getCodeSalt() {
        return codeSalt;
    }

    public void setCodeSalt(final String codeSalt) {
        this.codeSalt = codeSalt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(final LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(final LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(final int attempts) {
        this.attempts = attempts;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof EmailLoginCodeEntity e) {
            return super.equals(e)
                && Objects.equals(email, e.email)
                && Objects.equals(codeHash, e.codeHash);
        }
        return false;
    }

}
