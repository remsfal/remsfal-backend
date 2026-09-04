package de.remsfal.ticketing.entity.dto;

import jakarta.nosql.Column;
import jakarta.nosql.Embeddable;

import java.util.UUID;

@Embeddable
public class TenantTimelineKey {

    @Column("tenancy_id")
    private UUID tenancyId;

    @Column("issue_id")
    private UUID issueId;

    @Column("timeline_id")
    private UUID timelineId;

    @Column("project_id")
    private UUID projectId;

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(final UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getTenancyId() {
        return tenancyId;
    }

    public void setTenancyId(final UUID tenancyId) {
        this.tenancyId = tenancyId;
    }

    public UUID getIssueId() {
        return issueId;
    }

    public void setIssueId(final UUID issueId) {
        this.issueId = issueId;
    }

    public UUID getTimelineId() {
        return timelineId;
    }

    public void setTimelineId(final UUID timelineId) {
        this.timelineId = timelineId;
    }
}
