package de.remsfal.ticketing.entity.dto;

import de.remsfal.core.model.ticketing.MessagePurpose;

import jakarta.nosql.Column;

import java.util.List;
import java.util.UUID;

public abstract class AbstractTimelineEntity extends AbstractEntity {

    @Column("attachment_id")
    protected List<UUID> attachmentIds;

    @Column("sender_id")
    protected UUID senderId;

    @Column("sender_name")
    protected String senderName;

    @Column("purpose")
    protected String purpose;

    @Column("message")
    protected String message;

    public List<UUID> getAttachmentIds() {
        return attachmentIds;
    }

    public void setAttachmentIds(final List<UUID> attachmentIds) {
        this.attachmentIds = attachmentIds;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(final UUID senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(final String senderName) {
        this.senderName = senderName;
    }

    public MessagePurpose getPurpose() {
        return purpose != null ? MessagePurpose.valueOf(purpose) : null;
    }

    public void setPurpose(final MessagePurpose purpose) {
        this.purpose = purpose != null ? purpose.name() : null;
    }

    public void setPurpose(final String purpose) {
        this.purpose = purpose;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

}
