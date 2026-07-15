package de.remsfal.ticketing.entity.dto;

import de.remsfal.core.model.ticketing.tenant.TenantTimelineModel;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity("tenant_timelines")
public class TenantTimelineEntity extends AbstractEntity implements TenantTimelineModel {

    @Id
    private TenantTimelineKey key;

    @Column("attachment_id")
    private List<UUID> attachmentIds;

    @Column("sender_id")
    private UUID senderId;

    @Column("sender_name")
    private String senderName;

    @Column("title")
    private String title;

    @Column("message")
    private String message;

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
    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

}
