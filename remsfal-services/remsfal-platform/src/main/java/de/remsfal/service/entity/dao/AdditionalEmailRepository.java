package de.remsfal.service.entity.dao;

import de.remsfal.service.entity.dto.AdditionalEmailEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;
import java.util.List;

@ApplicationScoped
public class AdditionalEmailRepository extends AbstractRepository<AdditionalEmailEntity> {

    public List<AdditionalEmailEntity> findByUserId(UUID userId) {
        return find("user.id", userId).list();
    }

    public void remove(final UUID id) {
        getEntityManager()
                .createNamedQuery("AdditionalEmailEntity.deleteById")
                .setParameter("id", id)
                .executeUpdate();
    }

}
