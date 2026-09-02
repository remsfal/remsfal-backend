package de.remsfal.ticketing.entity.dto;

import de.remsfal.core.model.ticketing.ContractorTimelineModel;
import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.core.model.ticketing.ParticipantRole;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity("contractor_timelines")
public class ContractorTimelineEntity extends AbstractEntity implements ContractorTimelineModel {

    @Id
    private ContractorTimelineKey key;

    @Column("issue_id")
    private UUID issueId;

    @Column("attachment_id")
    private List<UUID> attachmentIds;

    @Column("sender_id")
    private UUID senderId;

    @Column("sender_name")
    private String senderName;

    @Column("sender_role")
    private String senderRole;

    @Column("recipient")
    private String recipient;

    @Column("purpose")
    private String purpose;

    @Column("message")
    private String message;

    public ContractorTimelineKey getKey() {
        return key;
    }

    public void setKey(final ContractorTimelineKey key) {
        this.key = key;
    }

    @Override
    public UUID getRequestId() {
        return Optional.ofNullable(key)
            .map(ContractorTimelineKey::getRequestId)
            .orElse(null);
    }

    public void setRequestId(final UUID requestId) {
        if (this.key == null) {
            this.key = new ContractorTimelineKey();
        }
        this.key.setRequestId(requestId);
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
        return issueId;
    }

    public void setIssueId(final UUID issueId) {
        this.issueId = issueId;
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
    public ParticipantRole getSenderRole() {
        return senderRole != null ? ParticipantRole.valueOf(senderRole) : null;
    }

    public void setSenderRole(final ParticipantRole senderRole) {
        this.senderRole = senderRole != null ? senderRole.name() : null;
    }

    public void setSenderRole(final String senderRole) {
        this.senderRole = senderRole;
    }

    @Override
    public ParticipantRole getRecipient() {
        return recipient != null ? ParticipantRole.valueOf(recipient) : null;
    }

    public void setRecipient(final ParticipantRole recipient) {
        this.recipient = recipient != null ? recipient.name() : null;
    }

    public void setRecipient(final String recipient) {
        this.recipient = recipient;
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
