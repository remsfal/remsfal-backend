package de.remsfal.service.entity.dao;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import de.remsfal.service.entity.dto.ProjectEntity;
import de.remsfal.service.entity.dto.ProjectMembershipEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class ProjectRepository extends AbstractRepository<ProjectEntity> {

    public List<ProjectMembershipEntity> findMembershipByUserId(final String userId) {
        return getEntityManager().createNamedQuery("ProjectMembershipEntity.findByUserId", ProjectMembershipEntity.class)
            .setParameter(PARAM_USER_ID, userId)
            .getResultList();
    }

}