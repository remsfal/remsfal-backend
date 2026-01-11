package de.remsfal.service.entity.dao;

import de.remsfal.service.entity.dto.AdditionalEmailEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AdditionalEmailRepository extends AbstractRepository<AdditionalEmailEntity> {

    public boolean existsByEmail(final String email) {
        return count("email", email) > 0;
    }
}
