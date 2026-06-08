package de.remsfal.service.entity.dto;

import de.remsfal.service.entity.dto.superclass.AbstractEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_additional_email")
public class AdditionalEmailEntity extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UserEntity user;

    @Email
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "verified", nullable = false)
    private boolean verified;

    @Column(name = "verification_token", unique = true)
    private String verificationToken;

    @Column(name = "verification_token_expires_at")
    private LocalDateTime verificationTokenExpiresAt;

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(final boolean verified) {
        this.verified = verified;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(final String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public LocalDateTime getVerificationTokenExpiresAt() {
        return verificationTokenExpiresAt;
    }

    public void setVerificationTokenExpiresAt(final LocalDateTime verificationTokenExpiresAt) {
        this.verificationTokenExpiresAt = verificationTokenExpiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof AdditionalEmailEntity e) {
            return super.equals(e)
                    && Objects.equals(user, e.user)
                    && Objects.equals(email, e.email)
                    && Objects.equals(verified, e.verified)
                    && Objects.equals(verificationToken, e.verificationToken)
                    && Objects.equals(verificationTokenExpiresAt, e.verificationTokenExpiresAt);
        }
        return false;
    }

}
