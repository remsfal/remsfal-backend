package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dto.QuotationEntity;
import de.remsfal.ticketing.entity.dto.QuotationKey;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;

@ApplicationScoped
public class QuotationRepository extends AbstractRepository<QuotationEntity, QuotationKey> {

    public QuotationEntity insert(final QuotationEntity entity) {
        Instant now = Instant.now();
        entity.setModifiedAt(now);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        return template.insert(entity);
    }

}
