package de.remsfal.service.entity.dto;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import de.remsfal.service.entity.dto.superclass.AbstractEntity;

/**
 * A logged attempt of the passwordless email login, used for rate limiting and abuse detection. Attempts are
 * deleted after the configured retention period.
 */
@NamedQuery(name = "EmailLoginAttemptEntity.deleteOlderThan",
    query = "delete from EmailLoginAttemptEntity attempt where attempt.createdAt < :timestamp")
@Entity
@Table(name = "email_login_attempts")
public class EmailLoginAttemptEntity extends AbstractEntity {

    /**
     * Step of the email login.
     */
    public enum AttemptType {
        REQUEST,
        VERIFY
    }

    /**
     * Outcome of a login attempt.
     */
    public enum AttemptResult {
        SENT,
        SUCCESS,
        INVALID_CODE,
        RATE_LIMITED
    }

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "ip_address")
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AttemptType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false)
    private AttemptResult result;

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email == null ? null : email.trim().toLowerCase();
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public AttemptType getType() {
        return type;
    }

    public void setType(final AttemptType type) {
        this.type = type;
    }

    public AttemptResult getResult() {
        return result;
    }

    public void setResult(final AttemptResult result) {
        this.result = result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof EmailLoginAttemptEntity e) {
            return super.equals(e)
                && Objects.equals(email, e.email)
                && Objects.equals(ipAddress, e.ipAddress)
                && type == e.type
                && result == e.result;
        }
        return false;
    }

}
