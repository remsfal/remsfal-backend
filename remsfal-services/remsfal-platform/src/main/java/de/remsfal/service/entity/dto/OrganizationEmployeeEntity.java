package de.remsfal.service.entity.dto;

import de.remsfal.core.model.OrganizationEmployeeModel;
import de.remsfal.core.model.OrganizationEmployeeModel.*;
import de.remsfal.core.model.ProjectMemberModel;
import de.remsfal.service.entity.dto.embeddable.OrganizationEmployeeKey;
import de.remsfal.service.entity.dto.superclass.MetaDataEntity;
import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@NamedQuery(name = "OrganizationEmployeeEntity.findByUserId", query = "SELECT o FROM OrganizationEmployeeEntity o WHERE o.user.id = :userId")
@NamedQuery(name = "OrganizationEmployeeEntity.findByOrganizationId", query = "SELECT o FROM OrganizationEmployeeEntity o WHERE o.organization.id = :organizationId")
@NamedQuery(name = "OrganizationEmployeeEntity.findByOrganizationIdAndUserId", query = "SELECT o FROM OrganizationEmployeeEntity o WHERE o.organization.id = :organizationId AND o.user.id = :userId")
@NamedQuery(name = "OrganizationEmployeeEntity.removeByOrganizationIdAndUserId", query = "DELETE FROM OrganizationEmployeeEntity o WHERE o.organization.id = :organizationId AND o.user.id = :userId")
@Table(name = "organization_employees")
public class OrganizationEmployeeEntity extends MetaDataEntity implements OrganizationEmployeeModel {

    @EmbeddedId
    OrganizationEmployeeKey id;

    @ManyToOne
    @MapsId("organizationId")
    @JoinColumn(name = "organization_id", columnDefinition = "UUID")
    private OrganizationEntity organization;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", columnDefinition = "UUID")
    private UserEntity user;

    @Column(name = "employee_role")
    @Enumerated(EnumType.STRING)
    private EmployeeRole role;

    //Getter and Setter
    @Override
    public UUID getId() {
        return user.getId();
    }

    public OrganizationEntity getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationEntity organization) {
        this.organization = organization;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public EmployeeRole getRole() {
        return role;
    }

    public void setRole(EmployeeRole role) {
        this.role = role;
    }

    @Override
    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public Boolean isActive() {
        return user.isActive();
    }

    @Override
    public EmployeeRole getEmployeeRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!(o instanceof OrganizationEmployeeEntity)) {
            return false;
        }
        final OrganizationEmployeeEntity entity = (OrganizationEmployeeEntity) o;
        return Objects.equals(organization, entity.organization) &&
                Objects.equals(user, entity.user) &&
                Objects.equals(role, entity.role);
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("OrganizationEmployeeEntity: {");
        sb.append("id=").append(getId()).append(", ");
        sb.append("email=").append(getEmail()).append(", ");
        sb.append("name=").append(getName()).append(", ");
        sb.append("role=").append(getRole());
        return sb.append("}").toString();
    }
}
