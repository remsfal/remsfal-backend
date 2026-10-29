package de.remsfal.notification.entity.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import de.remsfal.notification.entity.dto.PushSubscriptionEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PushSubscriptionRepository {

    @Inject
    EntityManager em;

    public Optional<PushSubscriptionEntity> findByFid(final String fid) {
        return em.createQuery(
                        "SELECT p FROM PushSubscriptionEntity p WHERE p.fid = :fid",
                        PushSubscriptionEntity.class)
                .setParameter("fid", fid)
                .getResultStream()
                .findFirst();
    }

    public List<PushSubscriptionEntity> findByUserId(final UUID userId) {
        return em.createQuery(
                        "SELECT p FROM PushSubscriptionEntity p WHERE p.userId = :userId",
                        PushSubscriptionEntity.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Transactional
    public void save(final PushSubscriptionEntity entity) {
        em.merge(entity);
    }

    @Transactional
    public void deleteByFid(final String fid) {
        em.createQuery("DELETE FROM PushSubscriptionEntity p WHERE p.fid = :fid")
                .setParameter("fid", fid)
                .executeUpdate();
    }

    @Transactional
    public void deleteByUserId(final UUID userId) {
        em.createQuery("DELETE FROM PushSubscriptionEntity p WHERE p.userId = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
