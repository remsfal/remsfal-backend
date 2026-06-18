package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dto.RequestForQuotationEntity;
import de.remsfal.ticketing.entity.dto.RequestForQuotationKey;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class RequestForQuotationRepository
    extends AbstractRepository<RequestForQuotationEntity, RequestForQuotationKey> {

    public RequestForQuotationEntity insert(final RequestForQuotationEntity entity) {
        Instant now = Instant.now();
        entity.setModifiedAt(now);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        return template.insert(entity);
    }

    public List<RequestForQuotationEntity> findByIssueId(final UUID issueId) {
        return template.select(RequestForQuotationEntity.class)
            .where(ISSUE_ID).eq(issueId)
            .result();
    }
}
