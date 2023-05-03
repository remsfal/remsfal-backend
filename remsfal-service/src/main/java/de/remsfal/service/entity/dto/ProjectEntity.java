package de.remsfal.service.entity.dto;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import de.remsfal.core.model.ProjectMemberModel.UserRole;
import de.remsfal.core.model.ProjectModel;
import de.remsfal.core.model.UserModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "PROJECT")
public class ProjectEntity extends AbstractEntity implements ProjectModel {

    @Id
    @Column(name = "ID", nullable = false, length = 36)
    private String id;
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private Set<ProjectMembershipEntity> memberships;
    
    @Column(name = "TITLE")
    private String title;
    
    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Set<ProjectMembershipEntity> getMembers() {
        return memberships;
    }

    public void setMembers(Set<ProjectMembershipEntity> memberships) {
        this.memberships = memberships;
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

    public boolean isMember(UserModel user) {
        Iterator<ProjectMembershipEntity> iter = memberships.iterator();
        while(iter.hasNext()) {
            if(iter.next().getUser().getId().equals(user.getId())) {
                return true;
            }
        }
        return false;
    }

}
