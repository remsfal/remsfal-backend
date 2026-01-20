package de.remsfal.service.entity.dto.embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Embeddable
public class ProjectOrganizationKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "project_id", nullable = false, columnDefinition = "uuid")
    public UUID projectId;

    @Column(name = "organization_id", nullable = false, columnDefinition = "uuid")
    public UUID organizationId;

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(final UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(final UUID organizationId) {
        this.organizationId = organizationId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, organizationId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ProjectOrganizationKey e) {
            return Objects.equals(projectId, e.projectId)
                && Objects.equals(organizationId, e.organizationId);
        }
        return false;
    }

}
