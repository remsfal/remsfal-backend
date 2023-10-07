package de.remsfal.service.entity.dto;

import java.util.Objects;

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

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@NamedQuery(name = "ProjectMembershipEntity.findByUserId",
    query = "SELECT m FROM ProjectMembershipEntity m WHERE m.user.id = :userId")
@NamedQuery(name = "ProjectMembershipEntity.findByProjectIdAndUserId",
    query = "SELECT m FROM ProjectMembershipEntity m WHERE m.project.id = :projectId AND m.user.id = :userId")
@NamedQuery(name = "ProjectMembershipEntity.removeByProjectIdAndUserId",
    query = "DELETE FROM ProjectMembershipEntity m WHERE m.project.id = :projectId AND m.user.id = :userId")
@Table(name = "PROJECT_MEMBERSHIP")
public class ProjectMembershipEntity extends AbstractEntity implements ProjectMemberModel {

    @EmbeddedId
    private ProjectMembershipKey id = new ProjectMembershipKey();

    @ManyToOne
    @MapsId("projectId")
    @JoinColumn(name = "PROJECT_ID", columnDefinition = "char")
    ProjectEntity project;
    
    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "USER_ID", columnDefinition = "char")
    UserEntity user;
    
    @Column(name = "USER_ROLE")
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Override
    public String getId() {
        return user.getId();
    }

    @Override
    public void setId(String id) {
        throw new IllegalArgumentException("This entiy uses a composite key!");
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    @Override
    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public String getEmail() {
        return user.getEmail();
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
        if (!(o instanceof ProjectMembershipEntity)) {
            return false;
        }
        final ProjectMembershipEntity entity = (ProjectMembershipEntity) o;
        return Objects.equals(project, entity.project) &&
            Objects.equals(user, entity.user) &&
            Objects.equals(role, entity.role);
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("ProjectMembershipEntity: {");
        sb.append("id=").append(getId()).append(", ");
        sb.append("email=").append(getEmail()).append(", ");
        sb.append("name=").append(getName()).append(", ");
        sb.append("role=").append(getRole());
        return sb.append("}").toString();
    }

}
