package de.remsfal.service.entity.dto;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import de.remsfal.core.model.UserModel.UserRole;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@NamedQuery(name = "ProjectMembershipEntity.findByUserId", query = "SELECT m FROM ProjectMembershipEntity m WHERE m.user.id = :userId")
@Table(name = "PROJECT_MEMBERSHIP")
public class ProjectMembershipEntity extends AbstractEntity {

    @EmbeddedId
    private ProjectMembershipKey id = new ProjectMembershipKey();

    @ManyToOne
    @MapsId("projectId")
    @JoinColumn(name = "PROJECT_ID")
    ProjectEntity project;
    
    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "USER_ID")
    UserEntity user;
    
    @Column(name = "USER_ROLE")
    @Enumerated(EnumType.STRING)
    private UserRole role;

    public ProjectMembershipKey getId() {
        return id;
    }

    public void setId(ProjectMembershipKey id) {
        this.id = id;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
    
}
