package de.remsfal.ticketing.entity.dto;

import java.util.Objects;
import java.util.UUID;

import jakarta.nosql.Embeddable;
import jakarta.nosql.Id;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Embeddable
public class IssueAttachmentKey {

    @Id("issue_id")
    private UUID issueId;

    @Id("attachment_id")
    private UUID attachmentId;

    public IssueAttachmentKey() {
        // Default constructor
    }

    public IssueAttachmentKey(final UUID issueId, final UUID attachmentId) {
        this.issueId = issueId;
        this.attachmentId = attachmentId;
    }

    public UUID getIssueId() {
        return issueId;
    }

    public void setIssueId(final UUID issueId) {
        this.issueId = issueId;
    }

    public UUID getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(final UUID attachmentId) {
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
