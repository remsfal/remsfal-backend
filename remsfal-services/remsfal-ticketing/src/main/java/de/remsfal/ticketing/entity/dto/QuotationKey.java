package de.remsfal.ticketing.entity.dto;

import jakarta.nosql.Column;
import jakarta.nosql.Embeddable;

import java.util.Objects;
import java.util.UUID;

/**
 * Composite key for quotation entities in Cassandra.
 * 
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Embeddable
public class QuotationKey {

    @Column("project_id")
    private UUID projectId;

    @Column("issue_id")
    private UUID issueId;

    @Column("quotation_id")
    private UUID quotationId;

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuotationKey that = (QuotationKey) o;
        return Objects.equals(projectId, that.projectId) &&
               Objects.equals(issueId, that.issueId) &&
               Objects.equals(quotationId, that.quotationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, issueId, quotationId);
    }

}
