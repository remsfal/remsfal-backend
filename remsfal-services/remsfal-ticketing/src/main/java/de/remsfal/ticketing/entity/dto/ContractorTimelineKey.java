package de.remsfal.ticketing.entity.dto;

import jakarta.nosql.Column;
import jakarta.nosql.Embeddable;

import java.util.UUID;

@Embeddable
public class ContractorTimelineKey {

    @Column("issue_id")
    private UUID issueId;

    @Column("organization_id")
    private UUID organizationId;

    @Column("timeline_id")
    private UUID timelineId;

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

    public UUID getTimelineId() {
        return timelineId;
    }

    public void setTimelineId(final UUID timelineId) {
        this.timelineId = timelineId;
    }
}
