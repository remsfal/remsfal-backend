package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dto.IssueRequestEntity;
import de.remsfal.ticketing.entity.dto.IssueRequestKey;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.jnosql.databases.cassandra.mapping.CassandraTemplate;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class IssueRequestRepository extends AbstractRepository<IssueRequestEntity, IssueRequestKey> {

    static final String ORGANIZATION_ID = "organization_id";

    @Inject
    CassandraTemplate cassandraTemplate;

    public IssueRequestEntity insert(final IssueRequestEntity entity) {
        return template.insert(entity);
    }

    public List<IssueRequestEntity> findByIssue(final UUID issueId, final UUID organizationId) {
        return template.select(IssueRequestEntity.class)
            .where(ISSUE_ID).eq(issueId)
            .and(ORGANIZATION_ID).eq(organizationId)
            .result();
    }

    /**
     * Finds all issue requests for an issue, regardless of {@code organization_id} (part of the
     * table's partition key). Relies on an SAI index on {@code issue_id}.
     */
    public List<IssueRequestEntity> findByIssueIdOnly(final UUID issueId) {
        return cassandraTemplate.<IssueRequestEntity>cql(
            "SELECT * FROM remsfal.issue_requests WHERE " + ISSUE_ID + " = ? ALLOW FILTERING", issueId)
            .toList();
    }

}
