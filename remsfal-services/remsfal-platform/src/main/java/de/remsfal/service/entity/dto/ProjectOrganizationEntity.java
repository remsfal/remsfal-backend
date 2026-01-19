package de.remsfal.service.entity.dto;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import de.remsfal.core.model.ProjectMemberModel;
import de.remsfal.core.model.project.ProjectOrganizationModel;
import de.remsfal.service.entity.dto.embeddable.ProjectOrganizationKey;
import de.remsfal.service.entity.dto.superclass.MetaDataEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@NamedQuery(name = "ProjectOrganizationEntity.findByProjectId",
    query = "SELECT o FROM ProjectOrganizationEntity o WHERE o.project.id = :projectId")
@NamedQuery(name = "ProjectOrganizationEntity.findByProjectIdAndOrganizationId",
    query = "SELECT o FROM ProjectOrganizationEntity o WHERE o.project.id = :projectId AND o.organization.id = :organizationId")
@NamedQuery(name = "ProjectOrganizationEntity.removeByProjectIdAndOrganizationId",
    query = "DELETE FROM ProjectOrganizationEntity o WHERE o.project.id = :projectId AND o.organization.id = :organizationId")
@Table(name = "project_organizations")
public class ProjectOrganizationEntity extends MetaDataEntity implements ProjectOrganizationModel {

    @EmbeddedId
    private ProjectOrganizationKey id = new ProjectOrganizationKey();

    @ManyToOne
    @MapsId("projectId")
    @JoinColumn(name = "project_id", columnDefinition = "uuid")
    ProjectEntity project;

    @ManyToOne
    @MapsId("organizationId")
    @JoinColumn(name = "organization_id", columnDefinition = "uuid")
    OrganizationEntity organization;

    @Column(name = "organization_role")
    @Enumerated(EnumType.STRING)
    private ProjectMemberModel.MemberRole role;

    @Override
    public UUID getOrganizationId() {
        return organization.getId();
    }

    @Override
    public String getOrganizationName() {
        return organization.getName();
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }

    public OrganizationEntity getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationEntity organization) {
        this.organization = organization;
    }

    @Override
    public ProjectMemberModel.MemberRole getRole() {
        return role;
    }

    public void setRole(ProjectMemberModel.MemberRole role) {
        this.role = role;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!(o instanceof ProjectOrganizationEntity)) {
            return false;
        }
        final ProjectOrganizationEntity entity = (ProjectOrganizationEntity) o;
        return Objects.equals(project, entity.project) &&
            Objects.equals(organization, entity.organization) &&
            Objects.equals(role, entity.role);
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("ProjectOrganizationEntity: {");
        sb.append("organizationId=").append(getOrganizationId()).append(", ");
        sb.append("organizationName=").append(getOrganizationName()).append(", ");
        sb.append("role=").append(getRole());
        return sb.append("}").toString();
    }

}
