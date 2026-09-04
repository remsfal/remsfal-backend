package de.remsfal.ticketing.entity.dto;

import de.remsfal.core.model.ticketing.TimelineModel;

import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.Optional;
import java.util.UUID;

@Entity("tenant_timelines")
public class TimelineEntity extends AbstractTimelineEntity implements TimelineModel {

    @Id
    private TimelineKey key;

    public TimelineKey getKey() {
        return key;
    }

    public void setKey(final TimelineKey key) {
        this.key = key;
    }

    @Override
    public UUID getIssueId() {
        return Optional.ofNullable(key)
            .map(TimelineKey::getIssueId)
            .orElse(null);
    }

    @Override
    public UUID getTenancyId() {
        return Optional.ofNullable(key)
            .map(TimelineKey::getTenancyId)
            .orElse(null);
    }

    @Override
    public UUID getTimelineId() {
        return Optional.ofNullable(key)
            .map(TimelineKey::getTimelineId)
            .orElse(null);
    }

    @Override
    public UUID getProjectId() {
        return Optional.ofNullable(key)
            .map(TimelineKey::getProjectId)
            .orElse(null);
    }

    public void setProjectId(final UUID projectId) {
        if (this.key == null) {
            this.key = new TimelineKey();
        }
        this.key.setProjectId(projectId);
    }

}
