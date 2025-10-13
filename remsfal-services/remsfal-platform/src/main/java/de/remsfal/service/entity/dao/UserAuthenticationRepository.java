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

    public void updateRefreshToken(final UserAuthenticationModel userAuthentication) {
        updateRefreshToken(userAuthentication.getId(), userAuthentication.getRefreshToken());
    }

    public void updateRefreshToken(final UUID userId, final String refreshToken) {
        getEntityManager()
            .createNamedQuery("UserAuthenticationEntity.updateRefreshToken")
            .setParameter("refreshToken", refreshToken)
            .setParameter(PARAM_USER_ID, userId)
            .executeUpdate();
    }

    public void deleteRefreshToken(final UUID userId) {
        getEntityManager().createNamedQuery("UserAuthenticationEntity.deleteRefreshToken")
            .setParameter(PARAM_USER_ID, userId).executeUpdate();
    }

}
