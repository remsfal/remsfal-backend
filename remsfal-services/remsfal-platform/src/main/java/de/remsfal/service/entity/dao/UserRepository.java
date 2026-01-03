package de.remsfal.service.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import de.remsfal.service.entity.dto.UserEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class UserRepository extends AbstractRepository<UserEntity> {

    public Optional<UserEntity> findByTokenId(final String tokenId) {
        return find("tokenId", tokenId).singleResultOptional();
    }

    public Optional<UserEntity> findByEmail(final String email) {
        return find("email", email).singleResultOptional();
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
    public boolean updateAuthenticatedAt(final String googleId, final Date date) {
        return getEntityManager().createNamedQuery("UserEntity.updateAuthenticatedAt")
            .setParameter("timestamp", date)
            .setParameter("tokenId", googleId)
            .executeUpdate() > 0;
    }

}