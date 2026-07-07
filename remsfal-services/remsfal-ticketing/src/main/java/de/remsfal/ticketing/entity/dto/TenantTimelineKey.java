package de.remsfal.ticketing.entity.dto;

import java.util.Objects;
import java.util.UUID;

import jakarta.nosql.Embeddable;
import jakarta.nosql.Id;

@Embeddable
public class TenantTimelineKey {

	@Column("tenant_id")
	private UUID tenantId;

	@Column("issue_id")
	private UUID issueId;

	@Column("timeline_id")
	private UUID timelineId;

	public UUID getTenantId() {
		return tenantId;
	}

	public void setTenantId(UUID tenantId) {
		this.tenantId = tenantId;
	}

	public UUID getIssueId() {
		return issueId;
	}

	public void setIssueId(UUID issueId) {
		this.issueId = issueId;
	}

	public UUID getTimelineId() {
		return timelineId;
	}

	public void setTimelineId(UUID timelineId) {
		this.timelineId = timelineId;
	}
}