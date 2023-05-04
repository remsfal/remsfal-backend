package de.remsfal.service.entity.dao;

import javax.enterprise.context.ApplicationScoped;

import de.remsfal.service.entity.dto.UserEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class UserRepository extends AbstractRepository<UserEntity> {

    public UserEntity findByEmail(final String email) {
        return find("email", email).singleResult();
    }
    
    public boolean remove(final String userId) {
        return getEntityManager().createNamedQuery("UserEntity.deleteById")
            .setParameter("id", userId)
            .executeUpdate() > 0;
    }

}