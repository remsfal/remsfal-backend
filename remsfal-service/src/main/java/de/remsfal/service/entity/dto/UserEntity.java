package de.remsfal.service.entity.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Email;

import de.remsfal.core.model.CustomerModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@NamedQuery(name = "UserEntity.updateAuthenticatedAt", 
    query = "update UserEntity user set user.authenticatedAt = :timestamp where user.id = :id")
@NamedQuery(name = "UserEntity.deleteById", 
    query = "delete from UserEntity user where user.id = :id")
@Entity
@Table(name = "USER")
public class UserEntity extends AbstractEntity implements CustomerModel {

    @Id
    @Column(name = "ID", columnDefinition = "char", nullable = false, length = 36)
    private String id;
    
    @Column(name = "TOKEN_ID", unique = true)
    private String tokenId;

    @Column(name = "NAME")
    private String name;
    
    @Email
    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;

    @Column(name = "AUTHENTICATED_AT", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date authenticatedAt;

    @OneToMany(mappedBy = "user")
    private Set<ProjectMembershipEntity> memberships;
    
    @Override
    public String getId() {
        return id;
    }

    @Override
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
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public Date getAuthenticatedAt() {
        return authenticatedAt;
    }

    public void setAuthenticatedAt(final Date authenticatedAt) {
        this.authenticatedAt = authenticatedAt;
    }

    public Set<ProjectMembershipEntity> getMemberships() {
        return memberships;
    }

    public void setMemberships(final Set<ProjectMembershipEntity> memberships) {
        this.memberships = memberships;
    }

    @Override
    public LocalDate getRegisteredDate() {
        return this.getCreatedAt()
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();
    }

    @Override
    public LocalDateTime getLastLoginDate() {
        return this.getAuthenticatedAt()
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!(o instanceof UserEntity)) {
            return false;
        }
        final UserEntity entity = (UserEntity) o;
        return Objects.equals(id, entity.id) &&
            Objects.equals(tokenId, entity.tokenId) &&
            Objects.equals(name, entity.name) &&
            Objects.equals(email, entity.email);
    }

}
