package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dto.RequestForQuotationEntity;
import de.remsfal.ticketing.entity.dto.RequestForQuotationKey;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class RequestForQuotationRepository
    extends AbstractRepository<RequestForQuotationEntity, RequestForQuotationKey> {

    private static final String REQUEST_ID = "request_id";
    private static final String ORGANIZATION_ID = "organization_id";

    public RequestForQuotationEntity insert(final RequestForQuotationEntity entity) {
        Instant now = Instant.now();
        entity.setModifiedAt(now);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        return template.insert(entity);
    }

    public RequestForQuotationEntity update(final RequestForQuotationEntity entity) {
        entity.setModifiedAt(Instant.now());
        return template.update(entity);
    }

    public List<RequestForQuotationEntity> findByIssueId(final UUID issueId) {
        return template.select(RequestForQuotationEntity.class)
            .where(ISSUE_ID).eq(issueId)
            .result();
    }

    public Optional<RequestForQuotationEntity> findById(final RequestForQuotationKey key) {
        return template.select(RequestForQuotationEntity.class)
            .where(ISSUE_ID).eq(key.getIssueId())
            .and(REQUEST_ID).eq(key.getRequestId())
            .singleResult();
    }

    public List<RequestForQuotationEntity> findByOrganizationId(final UUID organizationId) {
        return template.select(RequestForQuotationEntity.class)
            .where(ORGANIZATION_ID).eq(organizationId)
            .result();
    }
}
