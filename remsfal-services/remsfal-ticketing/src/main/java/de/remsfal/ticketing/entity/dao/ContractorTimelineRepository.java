package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dto.ContractorTimelineEntity;
import de.remsfal.ticketing.entity.dto.ContractorTimelineKey;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ContractorTimelineRepository extends AbstractRepository<ContractorTimelineEntity, ContractorTimelineKey> {

    static final String REQUEST_ID = "request_id";
    static final String CONTRACTOR_ID = "contractor_id";
    static final String ORGANIZATION_ID = "organization_id";
    static final String TIMELINE_ID = "timeline_id";

    public ContractorTimelineEntity insert(final ContractorTimelineEntity entity) {
        return template.insert(entity);
    }

    public Optional<ContractorTimelineEntity> findById(final ContractorTimelineKey key) {
        return template.select(ContractorTimelineEntity.class)
            .where(REQUEST_ID).eq(key.getRequestId())
            .and(CONTRACTOR_ID).eq(key.getContractorId())
            .and(ORGANIZATION_ID).eq(key.getOrganizationId())
            .and(TIMELINE_ID).eq(key.getTimelineId())
            .singleResult();
    }

    public List<ContractorTimelineEntity> findByRequest(final UUID requestId, final UUID contractorId,
        final UUID organizationId) {
        return template.select(ContractorTimelineEntity.class)
            .where(REQUEST_ID).eq(requestId)
            .and(CONTRACTOR_ID).eq(contractorId)
            .and(ORGANIZATION_ID).eq(organizationId)
            .result();
    }
}
