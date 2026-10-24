package de.remsfal.service.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import de.remsfal.service.entity.dto.UserEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class UserRepository extends AbstractRepository<UserEntity> {

    public Optional<UserEntity> findByEmail(final String email) {
        return find("email", email == null ? null : email.trim().toLowerCase()).singleResultOptional();
    }

    public Optional<UserEntity> findByIdWithAdditionalEmails(UUID id) {
        return getEntityManager().createNamedQuery("UserEntity.findByIdWithAdditionalEmails", UserEntity.class)
            .setParameter("id", id)
            .getResultStream()
            .findFirst();
    }

    public boolean remove(final UUID userId) {
        return getEntityManager().createNamedQuery("UserEntity.deleteById")
            .setParameter("id", userId)
            .executeUpdate() > 0;
    }

    @Transactional
    public boolean updateAuthenticatedAt(final UUID userId, final LocalDateTime timestamp) {
        return getEntityManager().createNamedQuery("UserEntity.updateAuthenticatedAt")
            .setParameter("timestamp", timestamp)
            .setParameter(PARAM_ID, userId)
            .executeUpdate() > 0;
    }

}
