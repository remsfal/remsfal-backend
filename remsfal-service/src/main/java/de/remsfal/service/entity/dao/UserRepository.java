package de.remsfal.service.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.transaction.Transactional;

import java.util.Date;

import de.remsfal.service.boundary.authentication.RemsfalPrincipal;
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

    public boolean updateAuthenticatedAt(String userId) {
        return getEntityManager().createNamedQuery("UserEntity.updateAuthenticatedAt")
            .setParameter("timestamp", new Date())
            .setParameter("id", userId)
            .executeUpdate() > 0;
    }

    @Transactional
    public void onPrincipalAuthentication(@ObservesAsync final RemsfalPrincipal principal) {
        //logger.infov("Updating authentication timestamp of user (id = {0})", principal.getId());
        updateAuthenticatedAt(principal.getId());
    }

}