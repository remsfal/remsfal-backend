package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dto.ContractorTimelineEntity;
import de.remsfal.ticketing.entity.dto.ContractorTimelineKey;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ContractorTimelineRepository extends AbstractRepository<ContractorTimelineEntity, ContractorTimelineKey> {

    static final String CONTRACTOR_ID = "contractor_id";
    static final String ORGANIZATION_ID = "organization_id";
    static final String TIMELINE_ID = "timeline_id";

    public ContractorTimelineEntity insert(final ContractorTimelineEntity entity) {
        return template.insert(entity);
    }

    public Optional<ContractorTimelineEntity> findById(final ContractorTimelineKey key) {
        return template.select(ContractorTimelineEntity.class)
            .where(ISSUE_ID).eq(key.getIssueId())
            .and(CONTRACTOR_ID).eq(key.getContractorId())
            .and(ORGANIZATION_ID).eq(key.getOrganizationId())
            .and(TIMELINE_ID).eq(key.getTimelineId())
            .singleResult();
    }

    public List<ContractorTimelineEntity> findByIssue(final UUID issueId, final UUID contractorId,
        final UUID organizationId) {
        return template.select(ContractorTimelineEntity.class)
            .where(ISSUE_ID).eq(issueId)
            .and(CONTRACTOR_ID).eq(contractorId)
            .and(ORGANIZATION_ID).eq(organizationId)
            .result();
    }
}
