package de.remsfal.service.entity.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Email;

import de.remsfal.core.model.UserModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@NamedQuery(name = "UserEntity.deleteById", query = "delete from UserEntity user where user.id = :id")
@Entity
@Table(name = "USER")
public class UserEntity extends AbstractEntity implements UserModel {

    @Id
    @Column(name = "ID", nullable = false, length = 36)
    private String id;
    
    @OneToMany(mappedBy = "user")
    private Set<ProjectMembershipEntity> memberships;
    
    @Column(name = "NAME")
    private String name;
    
    @Email
    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public Set<ProjectMembershipEntity> getMemberships() {
        return memberships;
    }

    public void setMemberships(Set<ProjectMembershipEntity> memberships) {
        this.memberships = memberships;
    }

    @Override
    public UserRole getRole() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
        return this.getModifiedAt()
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
    }
    
}
