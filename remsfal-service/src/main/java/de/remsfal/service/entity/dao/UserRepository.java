package de.remsfal.service.entity.dao;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import de.remsfal.service.entity.dto.UserEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class UserRepository extends AbstractRepository<UserEntity> {

    @PersistenceContext
    EntityManager entityManager;
    
    public boolean remove(final String userId) {
        return entityManager.createNamedQuery("UserEntity.deleteById")
            .setParameter("id", userId)
            .executeUpdate() > 0;
    }

    @Transactional(TxType.SUPPORTS)
    public EntityManager getEntityManager() {
        return entityManager;
    }
    
}