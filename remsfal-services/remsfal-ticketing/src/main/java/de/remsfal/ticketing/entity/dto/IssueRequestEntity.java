package de.remsfal.ticketing.entity.dto;

import de.remsfal.core.model.ticketing.IssueRequestModel;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity("issue_requests")
public class IssueRequestEntity extends AbstractEntity implements IssueRequestModel {

    @Id
    private IssueRequestKey key;

    @Column("message")
    private String message;

    @Column("attachment_ids")
    private List<UUID> attachmentIds;

    public IssueRequestKey getKey() {
        return key;
    }

    public void setKey(final IssueRequestKey key) {
        this.key = key;
    }

    @Override
    public UUID getIssueId() {
        return Optional.ofNullable(key)
            .map(IssueRequestKey::getIssueId)
            .orElse(null);
    }

    public void setIssueId(final UUID issueId) {
        if (this.key == null) {
            this.key = new IssueRequestKey();
        }
        this.key.setIssueId(issueId);
    }

    @Override
    public UUID getOrganizationId() {
        return Optional.ofNullable(key)
            .map(IssueRequestKey::getOrganizationId)
            .orElse(null);
    }

    public void setOrganizationId(final UUID organizationId) {
        if (this.key == null) {
            this.key = new IssueRequestKey();
        }
        this.key.setOrganizationId(organizationId);
    }

    public UUID getIssueRequestId() {
        return Optional.ofNullable(key)
            .map(IssueRequestKey::getIssueRequestId)
            .orElse(null);
    }

    public void setIssueRequestId(final UUID issueRequestId) {
        if (this.key == null) {
            this.key = new IssueRequestKey();
        }
        this.key.setIssueRequestId(issueRequestId);
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    @Override
    public List<UUID> getAttachmentIds() {
        return attachmentIds;
    }

    public void setAttachmentIds(final List<UUID> attachmentIds) {
        this.attachmentIds = attachmentIds;
    }

}
