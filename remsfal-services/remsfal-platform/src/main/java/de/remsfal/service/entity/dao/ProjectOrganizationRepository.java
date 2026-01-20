package de.remsfal.service.entity.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;

import de.remsfal.service.entity.dto.ProjectOrganizationEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class ProjectOrganizationRepository extends AbstractRepository<ProjectOrganizationEntity> {

    public List<ProjectOrganizationEntity> findByProjectId(final UUID projectId) {
        return getEntityManager().createNamedQuery("ProjectOrganizationEntity.findByProjectId",
            ProjectOrganizationEntity.class)
            .setParameter(PARAM_PROJECT_ID, projectId)
            .getResultList();
    }

    public Optional<ProjectOrganizationEntity> findByProjectIdAndOrganizationId(final UUID projectId,
        final UUID organizationId) {
        try {
            return Optional.of(getEntityManager()
                .createNamedQuery("ProjectOrganizationEntity.findByProjectIdAndOrganizationId",
                    ProjectOrganizationEntity.class)
                .setParameter(PARAM_PROJECT_ID, projectId)
                .setParameter("organizationId", organizationId)
                .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public boolean removeByProjectIdAndOrganizationId(final UUID projectId, final UUID organizationId) {
        return getEntityManager()
            .createNamedQuery("ProjectOrganizationEntity.removeByProjectIdAndOrganizationId")
            .setParameter(PARAM_PROJECT_ID, projectId)
            .setParameter("organizationId", organizationId)
            .executeUpdate() > 0;
    }

    public ProjectOrganizationEntity merge(final ProjectOrganizationEntity entity) {
        return getEntityManager().merge(entity);
    }

    public List<ProjectOrganizationEntity> findByUserId(final UUID userId) {
        return getEntityManager()
            .createNamedQuery("ProjectOrganizationEntity.findByUserId", ProjectOrganizationEntity.class)
            .setParameter("userId", userId)
            .getResultList();
    }

}
