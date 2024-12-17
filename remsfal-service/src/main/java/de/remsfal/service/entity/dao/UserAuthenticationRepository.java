package de.remsfal.service.entity.dao;

import de.remsfal.core.model.UserAuthenticationModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.entity.dto.UserAuthenticationEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class UserAuthenticationRepository extends AbstractRepository<UserAuthenticationEntity> {

    public Optional<UserAuthenticationEntity> findByUserId(final String userId) {
        return getEntityManager().createNamedQuery("UserAuthenticationEntity.findByUserId", UserAuthenticationEntity.class)
            .setParameter(PARAM_USER_ID, userId)
            .getResultStream()
            .findFirst();
    }

    public Optional<UserAuthenticationEntity> findByUserAuthentication(final UserModel user) {
        return findByUserId(user.getId());
    }

    public void updateRefreshToken(final String userId, final String refreshToken) {
        getEntityManager().createNamedQuery("UserAuthenticationEntity.updateRefreshToken")
            .setParameter("refreshToken", refreshToken)
            .setParameter(PARAM_USER_ID, userId)
                .executeUpdate();
    }

    public void updateRefreshToken(final UserAuthenticationModel userAuthentication) {
        updateRefreshToken(userAuthentication.getUser().getId(), userAuthentication.getRefreshToken());
    }

    public void updateRefreshToken(final UserModel user, final String refreshToken) {
        updateRefreshToken(user.getId(), refreshToken);
    }

    public void deleteRefreshToken(final String userId) {
        getEntityManager().createNamedQuery("UserAuthenticationEntity.deleteRefreshToken")
            .setParameter(PARAM_USER_ID, userId)
                .executeUpdate();
    }

    public void deleteRefreshToken(final UserAuthenticationModel userAuthentication) {
        deleteRefreshToken(userAuthentication.getUser().getId());
    }

    public void deleteRefreshToken(final UserModel user) {
        deleteRefreshToken(user.getId());
    }
}
