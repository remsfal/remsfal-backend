package de.remsfal.service.entity.dto;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import de.remsfal.service.entity.dto.superclass.AbstractEntity;

/**
 * An external identity (provider + subject) that belongs to a user. A user can have several identities.
 */
@NamedQuery(name = "UserIdentityEntity.updateLastUsedAt",
    query = "update UserIdentityEntity identity set identity.lastUsedAt = :timestamp "
        + "where identity.provider = :provider and identity.subject = :subject")
@Entity
@Table(name = "user_identities")
public class UserIdentityEntity extends AbstractEntity {

    /**
     * External identity providers a user can authenticate with.
     */
    public enum IdentityProvider {
        GOOGLE,
        APPLE,
        EMAIL
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private IdentityProvider provider;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "email")
    private String email;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    public UserEntity getUser() {
        return user;
    }

    public void setUser(final UserEntity user) {
        this.user = user;
    }

    public IdentityProvider getProvider() {
        return provider;
    }

    public void setProvider(final IdentityProvider provider) {
        this.provider = provider;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email == null ? null : email.trim().toLowerCase();
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(final LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof UserIdentityEntity e) {
            return super.equals(e)
                && Objects.equals(provider, e.provider)
                && Objects.equals(subject, e.subject)
                && Objects.equals(email, e.email);
        }
        return false;
    }

}
