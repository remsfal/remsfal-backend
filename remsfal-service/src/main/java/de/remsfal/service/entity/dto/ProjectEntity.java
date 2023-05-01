package de.remsfal.service.entity.dto;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import de.remsfal.core.model.ProjectModel;
import de.remsfal.core.model.UserModel.UserRole;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "PROJECT")
public class ProjectEntity extends AbstractEntity implements ProjectModel {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Type(type = "org.hibernate.type.UUIDCharType")
    @Column(name = "ID", nullable = false, length = 36)
    private UUID id;
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private Set<ProjectMembershipEntity> memberships;
    
    @Column(name = "TITLE")
    private String title;
    
    @Override
    public String getId() {
        return id.toString();
    }

    public void setId(String id) {
        this.id = UUID.fromString(id);
    }

    public Set<ProjectMembershipEntity> getMemberships() {
        return memberships;
    }

    public void setMemberships(Set<ProjectMembershipEntity> memberships) {
        this.memberships = memberships;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void addMember(UserEntity userEntity, UserRole role) {
        if(memberships == null) {
            memberships = new HashSet<>();
        }
        ProjectMembershipEntity member = new ProjectMembershipEntity();
        member.setProject(this);
        member.setUser(userEntity);
        member.setRole(role);
        this.memberships.add(member);
    }

}
