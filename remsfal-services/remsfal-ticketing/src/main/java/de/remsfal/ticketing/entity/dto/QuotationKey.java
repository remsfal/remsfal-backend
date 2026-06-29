package de.remsfal.ticketing.entity.dto;

import jakarta.nosql.Embeddable;
import jakarta.nosql.Id;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class QuotationKey {

    @Id("issue_id")
    private UUID issueId;

    @Id("quotation_id")
    private UUID quotationId;

    public UUID getIssueId() {
        return issueId;
    }

    public void setIssueId(UUID issueId) {
        this.issueId = issueId;
    }

    public UUID getQuotationId() {
        return quotationId;
    }

    public void setQuotationId(UUID quotationId) {
        this.quotationId = quotationId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(issueId, quotationId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof QuotationKey other)) {
            return false;
        }
        return Objects.equals(issueId, other.issueId) && Objects.equals(quotationId, other.quotationId);
    }

}
