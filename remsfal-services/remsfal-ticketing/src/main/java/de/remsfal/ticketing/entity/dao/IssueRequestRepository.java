package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dto.IssueRequestEntity;
import de.remsfal.ticketing.entity.dto.IssueRequestKey;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class IssueRequestRepository extends AbstractRepository<IssueRequestEntity, IssueRequestKey> {

    static final String ORGANIZATION_ID = "organization_id";
    static final String ISSUE_REQUEST_ID = "issue_request_id";

    public IssueRequestEntity insert(final IssueRequestEntity entity) {
        return template.insert(entity);
    }

    /**
     * Looks up a single issue request, scoped to the given {@code organization_id} for
     * authorization. Relies on the SAI index on {@code organization_id}, since it is no longer
     * part of the table's primary key.
     */
    public Optional<IssueRequestEntity> findById(final IssueRequestKey key) {
        return template.select(IssueRequestEntity.class)
            .where(ISSUE_ID).eq(key.getIssueId())
            .and(ORGANIZATION_ID).eq(key.getOrganizationId())
            .and(ISSUE_REQUEST_ID).eq(key.getIssueRequestId())
            .singleResult();
    }

    /**
     * Deletes by the table's actual primary key ({@code issue_id}, {@code issue_request_id}).
     * Cassandra does not allow filtering a DELETE by non-primary-key columns (even indexed ones),
     * so authorization against {@code organization_id} must happen beforehand, e.g. via {@link
     * #findById(IssueRequestKey)}.
     */
    public void delete(final IssueRequestKey key) {
        template.delete(IssueRequestEntity.class)
            .where(ISSUE_ID).eq(key.getIssueId())
            .and(ISSUE_REQUEST_ID).eq(key.getIssueRequestId())
            .execute();
    }

    public List<IssueRequestEntity> findByIssue(final UUID issueId, final UUID organizationId) {
        return template.select(IssueRequestEntity.class)
            .where(ISSUE_ID).eq(issueId)
            .and(ORGANIZATION_ID).eq(organizationId)
            .result();
    }

    public List<IssueRequestEntity> findByIssueIdOnly(final UUID issueId) {
        return template.select(IssueRequestEntity.class)
            .where(ISSUE_ID).eq(issueId)
            .result();
    }

}
