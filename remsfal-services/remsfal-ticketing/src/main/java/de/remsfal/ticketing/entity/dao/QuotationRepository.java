package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dto.QuotationEntity;
import de.remsfal.ticketing.entity.dto.QuotationKey;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public List<QuotationEntity> findByIssueId(final UUID issueId) {
        return template.select(QuotationEntity.class)
            .where(ISSUE_ID).eq(issueId)
            .result();
    }

    public Optional<QuotationEntity> findById(final QuotationKey key) {
        return template.select(QuotationEntity.class)
            .where(ISSUE_ID).eq(key.getIssueId())
            .and("quotation_id").eq(key.getQuotationId())
            .singleResult();
    }

    public List<QuotationEntity> findByOrganizationId(final UUID organizationId) {
        return template.select(QuotationEntity.class)
            .where("organization_id").eq(organizationId)
            .result();
    }

}
