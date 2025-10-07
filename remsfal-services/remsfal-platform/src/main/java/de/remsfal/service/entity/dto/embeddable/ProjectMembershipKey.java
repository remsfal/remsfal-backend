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
public class ProjectMembershipKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "PROJECT_ID", nullable = false, columnDefinition = "uuid")
    public UUID projectId;
    
    @Column(name = "USER_ID", nullable = false, columnDefinition = "uuid")
    public UUID userId;

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(final UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(final UUID userId) {
        this.userId = userId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(projectId, userId);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ProjectMembershipKey e) {
            return Objects.equals(projectId, e.projectId)
                && Objects.equals(userId, e.userId);
        }
        return false;
    }

}
