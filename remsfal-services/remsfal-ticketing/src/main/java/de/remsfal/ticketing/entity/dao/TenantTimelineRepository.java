package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dto.TenantTimelineEntity;
import de.remsfal.ticketing.entity.dto.TenantTimelineKey;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.jnosql.databases.cassandra.mapping.CassandraTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TenantTimelineRepository extends AbstractRepository<TenantTimelineEntity, TenantTimelineKey> {

    static final String TENANCY_ID = "tenancy_id";
    static final String TIMELINE_ID = "timeline_id";

    @Inject
    CassandraTemplate cassandraTemplate;

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

    /**
     * Finds all tenant timeline entries for an issue, regardless of {@code tenancy_id}/{@code project_id}
     * (part of the table's partition key). Relies on an SAI index on {@code issue_id}.
     */
    public List<TenantTimelineEntity> findByIssueIdOnly(final UUID issueId) {
        return cassandraTemplate.<TenantTimelineEntity>cql(
            "SELECT * FROM remsfal.tenant_timelines WHERE " + ISSUE_ID + " = ? ALLOW FILTERING", issueId)
            .toList();
    }

    public int deleteByIssueId(final UUID issueId) {
        final List<TenantTimelineEntity> rows = findByIssueIdOnly(issueId);
        for (final TenantTimelineEntity row : rows) {
            template.delete(TenantTimelineEntity.class)
                .where(TENANCY_ID).eq(row.getKey().getTenancyId())
                .and(ISSUE_ID).eq(row.getKey().getIssueId())
                .and(PROJECT_ID).eq(row.getKey().getProjectId())
                .and(TIMELINE_ID).eq(row.getKey().getTimelineId())
                .execute();
        }
        return rows.size();
    }
}
