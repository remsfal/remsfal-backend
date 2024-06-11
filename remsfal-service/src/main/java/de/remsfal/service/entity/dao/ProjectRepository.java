package de.remsfal.service.entity.dao;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import de.remsfal.service.entity.dto.ProjectEntity;
import de.remsfal.service.entity.dto.ProjectMembershipEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class ProjectRepository extends AbstractRepository<ProjectEntity> {

    public List<ProjectMembershipEntity> findMembershipByUserId(final String userId,
                                                                final int offset, final int limit) {
        return getEntityManager().createNamedQuery("ProjectMembershipEntity.findByUserId",
                        ProjectMembershipEntity.class)
            .setParameter(PARAM_USER_ID, userId)
            .setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();
    }

    public long countMembershipByUserId(final String userId) {
        return getEntityManager()
            .createNamedQuery("ProjectMembershipEntity.countByUserId", Long.class)
            .setParameter(PARAM_USER_ID, userId)
            .getSingleResult();
    }

    public ProjectMembershipEntity findMembershipByUserIdAndProjectId(final String userId, final String projectId) {
        return getEntityManager()
            .createNamedQuery("ProjectMembershipEntity.findByProjectIdAndUserId",
                    ProjectMembershipEntity.class)
            .setParameter(PARAM_PROJECT_ID, projectId)
            .setParameter(PARAM_USER_ID, userId)
            .getSingleResult();
    }

    public ProjectEntity findProjectByUserId(final String userId, final String projectId) {
        return findMembershipByUserIdAndProjectId(userId, projectId).getProject();
    }

    public boolean removeMembershipByUserIdAndProjectId(final String userId, final String projectId) {
        return getEntityManager()
            .createNamedQuery("ProjectMembershipEntity.removeByProjectIdAndUserId")
            .setParameter(PARAM_PROJECT_ID, projectId)
            .setParameter(PARAM_USER_ID, userId)
            .executeUpdate() > 0;
    }

}