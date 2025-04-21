package de.remsfal.chat.entity.dto;

import java.util.Objects;

import de.remsfal.core.model.UserModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 * @deprecated TODO @Eyad Remove this with issue 
 * https://github.com/remsfal/remsfal-backend/issues/315
 */
@Entity
@Deprecated
@Table(name = "USER")
public class UserEntity implements UserModel {

    @Id
    @Column(name = "ID", columnDefinition = "char", nullable = false, length = 36)
    protected String id;

    @Column(name = "TOKEN_ID", unique = true)
    private String tokenId;

    @Email
    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Override
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(final String tokenId) {
        this.tokenId = tokenId;
    }

    @Override
    public Boolean isActive() {
        return tokenId != null;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String getName() {
        return String.format("%s %s", this.getFirstName(), this.getLastName()).trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof UserEntity e) {
            return Objects.equals(id, e.id)
                && Objects.equals(email, e.email)
                && Objects.equals(firstName, e.firstName)
                && Objects.equals(lastName, e.lastName);
        }
        return false;
    }

}
