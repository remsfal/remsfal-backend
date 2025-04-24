package de.remsfal.chat.entity.dto;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import de.remsfal.core.model.ProjectMemberModel.MemberRole;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 * @deprecated TODO @Eyad Remove this with issue
 * https://github.com/remsfal/remsfal-backend/issues/315
 */
@Entity
@Deprecated
@NamedQuery(name = "ProjectMembershipEntity.findByProjectIdAndUserId",
    query = "SELECT m FROM ProjectMembershipEntity m WHERE m.projectId = :projectId AND m.userId = :userId")
@Table(name = "PROJECT_MEMBERSHIP")
public class ProjectMembershipEntity {

    @Id
    @Column(name = "PROJECT_ID", columnDefinition = "char", nullable = false, length = 36)
    public String projectId;

    @Id
    @Column(name = "USER_ID", columnDefinition = "char", nullable = false, length = 36)
    public String userId;

    @Column(name = "MEMBER_ROLE")
    @Enumerated(EnumType.STRING)
    private MemberRole role;

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
    
    public MemberRole getRole() {
        return role;
    }

    public void setRole(MemberRole role) {
        this.role = role;
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
        if (o instanceof ProjectMembershipEntity e) {
            return Objects.equals(projectId, e.projectId)
                && Objects.equals(userId, e.userId)
                && Objects.equals(role, e.role);
        }
        return false;
    }

}
