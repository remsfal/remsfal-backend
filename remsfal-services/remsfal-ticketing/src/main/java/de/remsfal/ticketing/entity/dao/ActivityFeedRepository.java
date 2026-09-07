package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dto.ActivityFeedEntity;
import de.remsfal.ticketing.entity.dto.ActivityFeedKey;
import de.remsfal.ticketing.entity.filter.ActivityFeedFilter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.jnosql.databases.cassandra.mapping.CassandraTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ActivityFeedRepository extends AbstractRepository<ActivityFeedEntity, ActivityFeedKey> {

    private static final String AND = " AND ";

    static final String USER_ID     = "user_id";
    static final String ACTIVITY_ID = "activity_id";

    @Inject
    CassandraTemplate cassandraTemplate;

    public Optional<ActivityFeedEntity> findByUserIdAndActivityId(final UUID userId, final UUID activityId) {
        return template.select(ActivityFeedEntity.class)
            .where(USER_ID).eq(userId)
            .and(ACTIVITY_ID).eq(activityId)
            .singleResult();
    }

    /**
     * Fetches at most {@code limit} activities of a single user partition, ordered by
     * {@code activity_id} descending (the table's native {@code CLUSTERING ORDER BY}, which is
     * also creation order since {@code activity_id} is a UUIDv7). When {@code cursor} is given,
     * only activities with an {@code activity_id} strictly smaller than the cursor are returned,
     * i.e. continuing after the last activity of a previous page. No {@code ORDER BY} is added
     * explicitly, relying on the table's native clustering order instead, mirroring
     * {@link IssueRepository#findByQuery}.
     *
     * <p>The partition ({@code user_id}) is always bound, so the additional {@code ALLOW
     * FILTERING} predicates stay scoped to a single, small partition rather than a full-table
     * scan.
     */
    public List<ActivityFeedEntity> findByQuery(final UUID userId, final ActivityFeedFilter filter,
        final UUID cursor, final Integer limit) {
        final StringBuilder cql = new StringBuilder("SELECT * FROM remsfal.activity_feeds WHERE ")
            .append(USER_ID).append(" = ?");
        final List<Object> params = new ArrayList<>();
        params.add(userId);

        if (filter.projectId() != null) {
            cql.append(AND).append("project_id = ?");
            params.add(filter.projectId());
        }
        if (filter.issueId() != null) {
            cql.append(AND).append("issue_id = ?");
            params.add(filter.issueId());
        }
        if (filter.agreementId() != null) {
            cql.append(AND).append("agreement_id = ?");
            params.add(filter.agreementId());
        }
        if (filter.organizationId() != null) {
            cql.append(AND).append("organization_id = ?");
            params.add(filter.organizationId());
        }
        if (filter.contractorId() != null) {
            cql.append(AND).append("contractor_id = ?");
            params.add(filter.contractorId());
        }
        if (filter.assigneeId() != null) {
            cql.append(AND).append("assignee_id = ?");
            params.add(filter.assigneeId());
        }
        if (cursor != null) {
            cql.append(AND).append(ACTIVITY_ID).append(" < ?");
            params.add(cursor);
        }
        cql.append(" LIMIT ? ALLOW FILTERING");
        params.add(limit);

        return cassandraTemplate.<ActivityFeedEntity>cql(cql.toString(), params.toArray())
            .toList();
    }

    public ActivityFeedEntity insert(final ActivityFeedEntity entity) {
        final Instant now = Instant.now();
        entity.setModifiedAt(now);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        return template.insert(entity);
    }

    public ActivityFeedEntity update(final ActivityFeedEntity entity) {
        entity.setModifiedAt(Instant.now());
        return template.update(entity);
    }

    public void delete(final ActivityFeedKey key) {
        template.delete(ActivityFeedEntity.class)
            .where(USER_ID).eq(key.getUserId())
            .and(ACTIVITY_ID).eq(key.getActivityId())
            .execute();
    }

}
