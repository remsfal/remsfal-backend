package de.remsfal.chat.entity.dao;

import java.util.Optional;

import de.remsfal.chat.entity.dto.ProjectMembershipEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class ProjectMembershipRepository {

    protected static final String PARAM_USER_ID = "userId";
    protected static final String PARAM_PROJECT_ID = "projectId";

    @PersistenceContext
    EntityManager entityManager;
    
    public Optional<ProjectMembershipEntity> findMembershipByUserIdAndProjectId(final String userId,
                                                                                final String projectId) {
        try {
            return Optional.of(entityManager
                    .createNamedQuery("ProjectMembershipEntity.findByProjectIdAndUserId",
                                      ProjectMembershipEntity.class)
                    .setParameter(PARAM_PROJECT_ID, projectId)
                    .setParameter(PARAM_USER_ID, userId)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

}