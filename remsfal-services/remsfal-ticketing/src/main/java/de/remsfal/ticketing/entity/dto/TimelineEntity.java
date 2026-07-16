package de.remsfal.ticketing.entity.dto;

import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.core.model.ticketing.TimelineModel;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity("tenant_timelines")
public class TimelineEntity extends AbstractEntity implements TimelineModel {

    @Id
    private TimelineKey key;

    @Column("attachment_id")
    private List<UUID> attachmentIds;

    @Column("sender_id")
    private UUID senderId;

    @Column("sender_name")
    private String senderName;

    @Column("purpose")
    private String purpose;

    @Column("message")
    private String message;

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

    @Override
    public List<UUID> getAttachmentIds() {
        return attachmentIds;
    }

    public void setAttachmentIds(final List<UUID> attachmentIds) {
        this.attachmentIds = attachmentIds;
    }

    @Override
    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(final UUID senderId) {
        this.senderId = senderId;
    }

    @Override
    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(final String senderName) {
        this.senderName = senderName;
    }

    @Override
    public MessagePurpose getPurpose() {
        return purpose != null ? MessagePurpose.valueOf(purpose) : null;
    }

    public void setPurpose(final MessagePurpose purpose) {
        this.purpose = purpose != null ? purpose.name() : null;
    }

    public void setPurpose(final String purpose) {
        this.purpose = purpose;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

}
