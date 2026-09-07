package de.remsfal.ticketing.entity.dto;

import de.remsfal.core.model.ticketing.ContractorTimelineModel;
import de.remsfal.core.model.UserContext;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.Optional;
import java.util.UUID;

@Entity("contractor_timelines")
public class ContractorTimelineEntity extends AbstractTimelineEntity implements ContractorTimelineModel {

    @Id
    private ContractorTimelineKey key;

    @Column("sender_role")
    private String senderRole;

    public ContractorTimelineKey getKey() {
        return key;
    }

    public void setKey(final ContractorTimelineKey key) {
        this.key = key;
    }

    @Override
    public UUID getOrganizationId() {
        return Optional.ofNullable(key)
            .map(ContractorTimelineKey::getOrganizationId)
            .orElse(null);
    }

    public void setOrganizationId(final UUID organizationId) {
        if (this.key == null) {
            this.key = new ContractorTimelineKey();
        }
        this.key.setOrganizationId(organizationId);
    }

    @Override
    public UUID getTimelineId() {
        return Optional.ofNullable(key)
            .map(ContractorTimelineKey::getTimelineId)
            .orElse(null);
    }

    public void setTimelineId(final UUID timelineId) {
        if (this.key == null) {
            this.key = new ContractorTimelineKey();
        }
        this.key.setTimelineId(timelineId);
    }

    @Override
    public UUID getIssueId() {
        return Optional.ofNullable(key)
            .map(ContractorTimelineKey::getIssueId)
            .orElse(null);
    }

    public void setIssueId(final UUID issueId) {
        if (this.key == null) {
            this.key = new ContractorTimelineKey();
        }
        this.key.setIssueId(issueId);
    }

    @Override
    public UUID getTenancyId() {
        return null;
    }

    @Override
    public UUID getProjectId() {
        return null;
    }

    @Override
    public UserContext getSenderRole() {
        return senderRole != null ? UserContext.valueOf(senderRole) : null;
    }

    public void setSenderRole(final UserContext senderRole) {
        this.senderRole = senderRole != null ? senderRole.name() : null;
    }

    public void setSenderRole(final String senderRole) {
        this.senderRole = senderRole;
    }

}
