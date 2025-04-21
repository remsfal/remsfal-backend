package de.remsfal.chat.entity.dao;

import java.util.Optional;

import de.remsfal.chat.entity.dto.ProjectMembershipEntity;
import de.remsfal.chat.entity.dto.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 * @deprecated TODO @Eyad Remove this with issue https://github.com/remsfal/remsfal-backend/issues/315
 */
@Deprecated
@ApplicationScoped
public class AuthorizationRepository {

    protected static final String PARAM_USER_ID = "userId";
    protected static final String PARAM_PROJECT_ID = "projectId";

    @PersistenceContext
    EntityManager entityManager;

    public Optional<UserEntity> findUser(final String userId) {
        return Optional.of(entityManager
            .find(UserEntity.class, userId));
    }

    public Optional<ProjectMembershipEntity> findMembership(final String userId, final String projectId) {
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