package de.remsfal.service.entity.dto;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "PROJECT_MEMBERSHIP")
public class ProjectMembershipEntity extends AbstractEntity {

    @EmbeddedId
    private ProjectMembershipKey id;

    @ManyToOne
    @MapsId("projectId")
    @JoinColumn(name = "PROJECT_ID")
    ProjectEntity project;
    
    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "USER_ID")
    ProjectEntity user;
    
    @Column(name = "ROLE")
    private String role;

    public ProjectMembershipKey getId() {
        return id;
    }

    public void setId(ProjectMembershipKey id) {
        this.id = id;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }

    public ProjectEntity getUser() {
        return user;
    }

    public void setUser(ProjectEntity user) {
        this.user = user;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    
}
