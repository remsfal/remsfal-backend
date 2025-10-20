package de.remsfal.service.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public Map<UUID, UserEntity> findByIds(final List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return getEntityManager()
            .createQuery("SELECT u FROM UserEntity u WHERE u.id IN :ids", UserEntity.class)
            .setParameter("ids", userIds)
            .getResultStream()
            .collect(Collectors.toMap(UserEntity::getId, user -> user));
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