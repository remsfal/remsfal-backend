package de.remsfal.ticketing.entity.dto;

import jakarta.nosql.Column;
import jakarta.nosql.Embeddable;

import java.util.UUID;

@Embeddable
public class ContractorTimelineKey {

    @Column("request_id")
    private UUID requestId;

    @Column("contractor_id")
    private UUID contractorId;

    @Column("organization_id")
    private UUID organizationId;

    @Column("timeline_id")
    private UUID timelineId;

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(final UUID requestId) {
        this.requestId = requestId;
    }

    public UUID getContractorId() {
        return contractorId;
    }

    public void setContractorId(final UUID contractorId) {
        this.contractorId = contractorId;
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
