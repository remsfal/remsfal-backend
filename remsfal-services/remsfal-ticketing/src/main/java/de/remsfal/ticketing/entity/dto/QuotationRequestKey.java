package de.remsfal.ticketing.entity.dto;

import jakarta.nosql.Embeddable;
import jakarta.nosql.Id;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class QuotationRequestKey {

    @Id("issue_id")
    private UUID issueId;

    @Id("request_id")
    private UUID requestId;

    public UUID getIssueId() {
        return issueId;
    }

    public void setIssueId(UUID issueId) {
        this.issueId = issueId;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(issueId, requestId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof QuotationRequestKey other)) {
            return false;
        }
        return Objects.equals(issueId, other.issueId) && Objects.equals(requestId, other.requestId);
    }

}
