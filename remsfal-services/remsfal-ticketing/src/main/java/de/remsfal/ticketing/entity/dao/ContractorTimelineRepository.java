package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dto.ContractorTimelineEntity;
import de.remsfal.ticketing.entity.dto.ContractorTimelineKey;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.jnosql.databases.cassandra.mapping.CassandraTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ContractorTimelineRepository extends AbstractRepository<ContractorTimelineEntity, ContractorTimelineKey> {

    static final String ORGANIZATION_ID = "organization_id";
    static final String TIMELINE_ID = "timeline_id";

    @Inject
    CassandraTemplate cassandraTemplate;

    public ContractorTimelineEntity insert(final ContractorTimelineEntity entity) {
        return template.insert(entity);
    }

    public Optional<ContractorTimelineEntity> findById(final ContractorTimelineKey key) {
        return template.select(ContractorTimelineEntity.class)
            .where(ISSUE_ID).eq(key.getIssueId())
            .and(ORGANIZATION_ID).eq(key.getOrganizationId())
            .and(TIMELINE_ID).eq(key.getTimelineId())
            .singleResult();
    }

    public List<ContractorTimelineEntity> findByIssue(final UUID issueId, final UUID organizationId) {
        return template.select(ContractorTimelineEntity.class)
            .where(ISSUE_ID).eq(issueId)
            .and(ORGANIZATION_ID).eq(organizationId)
            .result();
    }

    /**
     * Finds all contractor timeline entries for an issue, regardless of {@code organization_id}
     * (part of the table's partition key). Relies on an SAI index on {@code issue_id}.
     */
    public List<ContractorTimelineEntity> findByIssueIdOnly(final UUID issueId) {
        return cassandraTemplate.<ContractorTimelineEntity>cql(
            "SELECT * FROM remsfal.contractor_timelines WHERE " + ISSUE_ID + " = ? ALLOW FILTERING", issueId)
            .toList();
    }

    public int deleteByIssueId(final UUID issueId) {
        final List<ContractorTimelineEntity> rows = findByIssueIdOnly(issueId);
        for (final ContractorTimelineEntity row : rows) {
            template.delete(ContractorTimelineEntity.class)
                .where(ISSUE_ID).eq(row.getKey().getIssueId())
                .and(ORGANIZATION_ID).eq(row.getKey().getOrganizationId())
                .and(TIMELINE_ID).eq(row.getKey().getTimelineId())
                .execute();
        }
        return rows.size();
    }
}
