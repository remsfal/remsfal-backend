package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dto.ContractorTimelineEntity;
import de.remsfal.ticketing.entity.dto.ContractorTimelineKey;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ContractorTimelineRepository extends AbstractRepository<ContractorTimelineEntity, ContractorTimelineKey> {

    private static final String COL_REQUEST_ID = "request_id";
    private static final String COL_TIMELINE_ID = "timeline_id";

    public ContractorTimelineEntity insert(final ContractorTimelineEntity entity) {
        return template.insert(entity);
    }

    public Optional<ContractorTimelineEntity> findById(final ContractorTimelineKey key) {
        return template.select(ContractorTimelineEntity.class)
            .where(COL_REQUEST_ID).eq(key.getRequestId())
            .and(COL_TIMELINE_ID).eq(key.getTimelineId())
            .singleResult();
    }

    public List<ContractorTimelineEntity> findByRequest(final UUID requestId) {
        return template.select(ContractorTimelineEntity.class)
            .where(COL_REQUEST_ID).eq(requestId)
            .result();
    }
}
