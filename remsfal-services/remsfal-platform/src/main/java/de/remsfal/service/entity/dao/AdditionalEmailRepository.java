package de.remsfal.service.entity.dao;

import de.remsfal.service.entity.dto.AdditionalEmailEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AdditionalEmailRepository extends AbstractRepository<AdditionalEmailEntity> {

    public List<AdditionalEmailEntity> findByUserId(UUID userId) {
        return find("user.id", userId).list();
    }

    public Optional<AdditionalEmailEntity> findByEmail(final String email) {
        return find("email", email).singleResultOptional();
    }

    public boolean remove(final UUID id) {
        return getEntityManager()
                .createNamedQuery("AdditionalEmailEntity.deleteById")
                .setParameter("id", id)
                .executeUpdate() > 0;
    }

    @Transactional
    public boolean verify(UUID id) {
        return getEntityManager()
                .createNamedQuery("AdditionalEmailEntity.verifyById")
                .setParameter("id", id)
                .executeUpdate() > 0;
    }


}
