package de.remsfal.ticketing.entity.dao;

import de.remsfal.core.model.ticketing.QuotationModel;
import de.remsfal.ticketing.entity.dto.QuotationEntity;
import de.remsfal.ticketing.entity.dto.QuotationKey;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing quotation entities in Cassandra.
 * 
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class QuotationRepository extends AbstractRepository<QuotationEntity, QuotationKey> {

    public static final String QUOTATION_ID = "quotation_id";

    public Optional<QuotationEntity> find(final QuotationKey key) {
        return template.select(QuotationEntity.class)
            .where(PROJECT_ID).eq(key.getProjectId())
            .and(ISSUE_ID).eq(key.getIssueId())
            .and(QUOTATION_ID).eq(key.getQuotationId())
            .singleResult();
    }

    public Optional<QuotationEntity> findByQuotationId(final UUID quotationId) {
        return template.select(QuotationEntity.class)
            .where(QUOTATION_ID).eq(quotationId)
            .singleResult();
    }

    public List<? extends QuotationModel> findByIssue(final UUID projectId, final UUID issueId) {
        return template.select(QuotationEntity.class)
            .where(PROJECT_ID).eq(projectId)
            .and(ISSUE_ID).eq(issueId)
            .result();
    }

    public QuotationEntity insert(final QuotationEntity entity) {
        Instant now = Instant.now();
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        return template.insert(entity);
    }

    public QuotationEntity update(final QuotationEntity entity) {
        return template.update(entity);
    }

    public void delete(final QuotationKey key) {
        template.delete(QuotationEntity.class)
            .where(PROJECT_ID).eq(key.getProjectId())
            .and(ISSUE_ID).eq(key.getIssueId())
            .and(QUOTATION_ID).eq(key.getQuotationId())
            .execute();
    }

}
