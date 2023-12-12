package de.remsfal.service.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Date;

import de.remsfal.service.entity.dto.UserEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class UserRepository extends AbstractRepository<UserEntity> {

    public UserEntity findByTokenId(final String tokenId) {
        return find("tokenId", tokenId).singleResult();
    }

    public UserEntity findByEmail(final String email) {
        return find("email", email).singleResult();
    }

    public boolean remove(final String userId) {
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