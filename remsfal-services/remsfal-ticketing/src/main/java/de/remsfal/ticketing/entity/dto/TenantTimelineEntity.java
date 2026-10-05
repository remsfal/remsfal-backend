package de.remsfal.ticketing.entity.dto;

import de.remsfal.core.model.UserContext;
import de.remsfal.core.model.ticketing.TenantTimelineModel;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.Optional;
import java.util.UUID;

@Entity("tenant_timelines")
public class TenantTimelineEntity extends AbstractTimelineEntity implements TenantTimelineModel {

    @Id
    private TenantTimelineKey key;

    @Column("sender_role")
    private String senderRole;

    public TenantTimelineKey getKey() {
        return key;
    }

    public void setKey(final TenantTimelineKey key) {
        this.key = key;
    }

    @Override
    public UUID getIssueId() {
        return Optional.ofNullable(key)
            .map(TenantTimelineKey::getIssueId)
            .orElse(null);
    }

    @Override
    public UUID getTenancyId() {
        return Optional.ofNullable(key)
            .map(TenantTimelineKey::getTenancyId)
            .orElse(null);
    }

    @Override
    public UUID getTimelineId() {
        return Optional.ofNullable(key)
            .map(TenantTimelineKey::getTimelineId)
            .orElse(null);
    }

    @Override
    public UUID getProjectId() {
        return Optional.ofNullable(key)
            .map(TenantTimelineKey::getProjectId)
            .orElse(null);
    }

    public void setProjectId(final UUID projectId) {
        if (this.key == null) {
            this.key = new TenantTimelineKey();
        }
        this.key.setProjectId(projectId);
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
