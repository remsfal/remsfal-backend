package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dto.TimelineEntity;
import de.remsfal.ticketing.entity.dto.TimelineKey;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TimelineRepository extends AbstractRepository<TimelineEntity, TimelineKey> {

    private static final String COL_TENANCY_ID = "tenancy_id";
    private static final String COL_TIMELINE_ID = "timeline_id";

    public TimelineEntity insert(final TimelineEntity entity) {
        return template.insert(entity);
    }

    public Optional<TimelineEntity> findById(final TimelineKey key) {
        return template.select(TimelineEntity.class)
            .where(COL_TENANCY_ID).eq(key.getTenancyId())
            .and(ISSUE_ID).eq(key.getIssueId())
            .and(PROJECT_ID).eq(key.getProjectId())
            .and(COL_TIMELINE_ID).eq(key.getTimelineId())
            .singleResult();
    }

    public List<TimelineEntity> findByIssue(final UUID tenancyId, final UUID issueId, final UUID projectId) {
        return template.select(TimelineEntity.class)
            .where(COL_TENANCY_ID).eq(tenancyId)
            .and(ISSUE_ID).eq(issueId)
            .and(PROJECT_ID).eq(projectId)
            .result();
    }
}
