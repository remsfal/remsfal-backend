package de.remsfal.service.entity.dto;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Embeddable
public class ProjectMembershipKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "PROJECT_ID", columnDefinition = "char", nullable = false, length = 36)
    public String projectId;
    
    @Column(name = "USER_ID", columnDefinition = "char", nullable = false, length = 36)
    public String userId;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
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
