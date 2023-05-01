package de.remsfal.service.entity.dto;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.Type;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Embeddable
public class ProjectMembershipKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Type(type = "org.hibernate.type.UUIDCharType")
    @Column(name = "PROJECT_ID", nullable = false, length = 36)
    public UUID projectId;
    
    @Type(type = "org.hibernate.type.UUIDCharType")
    @Column(name = "USER_ID", nullable = false, length = 36)
    public UUID userId;

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
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
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!(o instanceof ProjectMembershipKey)) {
            return false;
        }
        final ProjectMembershipKey key = (ProjectMembershipKey) o;
        return Objects.equals(projectId, key.projectId) &&
            Objects.equals(userId, key.userId);
    }

}
