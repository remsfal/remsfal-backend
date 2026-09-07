package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dto.TenantTimelineEntity;
import de.remsfal.ticketing.entity.dto.TenantTimelineKey;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TenantTimelineRepository extends AbstractRepository<TenantTimelineEntity, TenantTimelineKey> {

    static final String TENANCY_ID = "tenancy_id";
    static final String TIMELINE_ID = "timeline_id";

    public TenantTimelineEntity insert(final TenantTimelineEntity entity) {
        return template.insert(entity);
    }

    public Optional<TenantTimelineEntity> findById(final TenantTimelineKey key) {
        return template.select(TenantTimelineEntity.class)
            .where(TENANCY_ID).eq(key.getTenancyId())
            .and(ISSUE_ID).eq(key.getIssueId())
            .and(PROJECT_ID).eq(key.getProjectId())
            .and(TIMELINE_ID).eq(key.getTimelineId())
            .singleResult();
    }

    public List<TenantTimelineEntity> findByIssue(final UUID tenancyId, final UUID issueId, final UUID projectId) {
        return template.select(TenantTimelineEntity.class)
            .where(TENANCY_ID).eq(tenancyId)
            .and(ISSUE_ID).eq(issueId)
            .and(PROJECT_ID).eq(projectId)
            .result();
    }
}
