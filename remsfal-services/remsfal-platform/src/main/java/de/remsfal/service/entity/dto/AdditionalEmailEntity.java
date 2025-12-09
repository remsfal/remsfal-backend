package de.remsfal.service.entity.dto;

import de.remsfal.service.entity.dto.superclass.AbstractEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;

import java.util.Objects;

@NamedQuery(
    name = "AdditionalEmailEntity.deleteById",
    query = "delete from AdditionalEmailEntity ae where ae.id = :id"
)
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
    private boolean verified = false;

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

    public boolean getVerified() {
        return verified;
    }

    public void setVerified(final boolean verified) {
        this.verified = verified;
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
                    && Objects.equals(verified, e.verified);
        }
        return false;
    }

}
