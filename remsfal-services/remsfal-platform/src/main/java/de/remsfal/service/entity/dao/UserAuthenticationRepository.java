package de.remsfal.service.entity.dao;

import de.remsfal.core.model.UserAuthenticationModel;
import de.remsfal.service.entity.dto.UserAuthenticationEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserAuthenticationRepository extends AbstractRepository<UserAuthenticationEntity> {

    public Optional<UserAuthenticationEntity> findByUserId(final UUID userId) {
        return getEntityManager()
            .createNamedQuery("UserAuthenticationEntity.findByUserId", UserAuthenticationEntity.class)
            .setParameter(PARAM_USER_ID, userId)
            .getResultStream()
            .findFirst();
    }

    public void updateRefreshTokenId(final UserAuthenticationModel userAuthentication) {
        updateRefreshTokenId(userAuthentication.getId(), userAuthentication.getRefreshTokenId());
    }

    public void updateRefreshTokenId(final UUID userId, final UUID refreshToken) {
        getEntityManager()
            .createNamedQuery("UserAuthenticationEntity.updateRefreshTokenId")
            .setParameter("refreshTokenId", refreshToken)
            .setParameter(PARAM_USER_ID, userId)
            .executeUpdate();
    }

    public void deleteByUserId(final UUID userId) {
        deleteById(userId);
    }

}
