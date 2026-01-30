package de.remsfal.ticketing.entity.dto;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * @author GitHub Copilot
 */
public class IssueAttachmentKey implements Serializable {

    private UUID issueId;
    private UUID attachmentId;

    public IssueAttachmentKey() {
    }

    public IssueAttachmentKey(UUID issueId, UUID attachmentId) {
        this.issueId = issueId;
        this.attachmentId = attachmentId;
    }

    public UUID getIssueId() {
        return issueId;
    }

    public void setIssueId(UUID issueId) {
        this.issueId = issueId;
    }

    public UUID getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(UUID attachmentId) {
        this.attachmentId = attachmentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IssueAttachmentKey that = (IssueAttachmentKey) o;
        return Objects.equals(issueId, that.issueId) &&
               Objects.equals(attachmentId, that.attachmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(issueId, attachmentId);
    }

    @Override
    public String toString() {
        return "IssueAttachmentKey{" +
               "issueId=" + issueId +
               ", attachmentId=" + attachmentId +
               '}';
    }
}
