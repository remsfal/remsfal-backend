package de.remsfal.ticketing.entity.dao;

import de.remsfal.core.model.quotation.QuotationRequestModel;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;
import de.remsfal.ticketing.entity.dto.QuotationRequestKey;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class QuotationRequestRepository extends AbstractRepository<QuotationRequestEntity, QuotationRequestKey> {

    private static final String REQUEST_ID = "request_id";
    private static final String CONTRACTOR_ID = "contractor_id";

    public Optional<QuotationRequestEntity> find(final QuotationRequestKey key) {
        return template.select(QuotationRequestEntity.class)
            .where(PROJECT_ID).eq(key.getProjectId())
            .and(REQUEST_ID).eq(key.getRequestId())
            .singleResult();
    }

    public List<? extends QuotationRequestModel> findByIssueId(final UUID issueId) {
        return template.select(QuotationRequestEntity.class)
            .where(ISSUE_ID).eq(issueId)
            .result();
    }

    public List<? extends QuotationRequestModel> findByContractorId(final UUID contractorId) {
        return template.select(QuotationRequestEntity.class)
            .where(CONTRACTOR_ID).eq(contractorId)
            .result();
    }

    public Optional<QuotationRequestEntity> findByRequestId(final UUID requestId) {
        return template.select(QuotationRequestEntity.class)
            .where(REQUEST_ID).eq(requestId)
            .singleResult();
    }

    public List<? extends QuotationRequestModel> findByProjectId(final UUID projectId) {
        return template.select(QuotationRequestEntity.class)
            .where(PROJECT_ID).eq(projectId)
            .result();
    }

    public List<? extends QuotationRequestModel> findAll() {
        return template.select(QuotationRequestEntity.class)
            .result();
    }

    public QuotationRequestEntity insert(final QuotationRequestEntity entity) {
        Instant now = Instant.now();
        entity.setModifiedAt(now);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        return template.insert(entity);
    }

    public QuotationRequestEntity update(final QuotationRequestEntity entity) {
        entity.setModifiedAt(Instant.now());
        return template.update(entity);
    }

    public void delete(final QuotationRequestKey key) {
        template.delete(QuotationRequestEntity.class)
            .where(PROJECT_ID).eq(key.getProjectId())
            .and(REQUEST_ID).eq(key.getRequestId())
            .execute();
    }

}
