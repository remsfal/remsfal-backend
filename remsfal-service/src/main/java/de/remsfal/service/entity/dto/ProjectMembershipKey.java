package de.remsfal.service.entity.dto;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Embeddable
public class ProjectMembershipKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "PROJECT_ID", nullable = false, length = 36)
    private UUID projectId;
    
    @Column(name = "USER_ID", nullable = false, length = 36)
    private UUID userId;

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
    
}
