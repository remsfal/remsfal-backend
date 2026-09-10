package de.remsfal.ticketing.entity.dto;

import jakarta.nosql.Column;
import jakarta.nosql.Embeddable;

import java.util.UUID;

@Embeddable
public class IssueRequestKey {

    @Column("issue_id")
    private UUID issueId;

    @Column("organization_id")
    private UUID organizationId;

    @Column("issue_request_id")
    private UUID issueRequestId;

    public UUID getIssueId() {
        return issueId;
    }

    public void setIssueId(final UUID issueId) {
        this.issueId = issueId;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(final UUID organizationId) {
        this.organizationId = organizationId;
    }

    public UUID getIssueRequestId() {
        return issueRequestId;
    }

    public void setIssueRequestId(final UUID issueRequestId) {
        this.issueRequestId = issueRequestId;
    }
}
