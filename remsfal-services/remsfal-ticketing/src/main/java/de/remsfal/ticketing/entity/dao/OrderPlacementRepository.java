package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dto.OrderPlacementEntity;
import de.remsfal.ticketing.entity.dto.OrderPlacementKey;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class OrderPlacementRepository extends AbstractRepository<OrderPlacementEntity, OrderPlacementKey> {

    private static final String ORGANIZATION_ID = "organization_id";

    public OrderPlacementEntity insert(final OrderPlacementEntity entity) {
        Instant now = Instant.now();
        entity.setModifiedAt(now);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        return template.insert(entity);
    }

    public OrderPlacementEntity update(final OrderPlacementEntity entity) {
        entity.setModifiedAt(Instant.now());
        return template.update(entity);
    }

    public List<OrderPlacementEntity> findByIssueId(final UUID issueId) {
        return template.select(OrderPlacementEntity.class)
            .where(ISSUE_ID).eq(issueId)
            .result();
    }

    public Optional<OrderPlacementEntity> findByIssueIdAndQuotationId(final UUID issueId, final UUID quotationId) {
        return findByIssueId(issueId).stream()
            .filter(p -> quotationId.equals(p.getQuotationId()))
            .findFirst();
    }

    public Optional<OrderPlacementEntity> findByIssueIdAndId(final UUID issueId, final UUID id) {
        return findByIssueId(issueId).stream()
            .filter(p -> id.equals(p.getId()))
            .findFirst();
    }

    public List<OrderPlacementEntity> findByOrganizationId(final UUID organizationId) {
        return template.select(OrderPlacementEntity.class)
            .where(ORGANIZATION_ID).eq(organizationId)
            .result();
    }

}
