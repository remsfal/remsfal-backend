package de.remsfal.service.entity.dto;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import de.remsfal.core.model.ProjectMemberModel.MemberRole;
import de.remsfal.core.model.ProjectModel;
import de.remsfal.core.model.UserModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "PROJECT")
public class ProjectEntity extends AbstractEntity implements ProjectModel {

    @Column(name = "TITLE")
    private String title;
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ProjectMembershipEntity> memberships;
    
    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    @Override
    public Set<ProjectMembershipEntity> getMembers() {
        return memberships;
    }

    public void setMembers(final Set<ProjectMembershipEntity> memberships) {
        this.memberships = memberships;
    }

    public void addMember(final UserEntity userEntity, final MemberRole role) {
        if(memberships == null) {
            memberships = new HashSet<>();
        }
        ProjectMembershipEntity member = new ProjectMembershipEntity();
        member.setProject(this);
        member.setUser(userEntity);
        member.setRole(role);
        this.memberships.add(member);
    }

    public boolean isMember(final UserModel user) {
        Iterator<ProjectMembershipEntity> iter = memberships.iterator();
        while(iter.hasNext()) {
            if(iter.next().getUser().getId().equals(user.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!(o instanceof ProjectEntity)) {
            return false;
        }
        final ProjectEntity entity = (ProjectEntity) o;
        return Objects.equals(id, entity.id) &&
            Objects.equals(title, entity.title) &&
            Objects.equals(memberships, entity.memberships);
    }

}
