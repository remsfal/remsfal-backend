package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dto.QuotationEntity;
import de.remsfal.ticketing.entity.dto.QuotationKey;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.Optional;

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

    public Optional<QuotationEntity> findById(final QuotationKey key) {
        return template.select(QuotationEntity.class)
            .where(ISSUE_ID).eq(key.getIssueId())
            .and("quotation_id").eq(key.getQuotationId())
            .singleResult();
    }

}
