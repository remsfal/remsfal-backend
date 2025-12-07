package de.remsfal.ticketing.entity.dto;

import de.remsfal.core.model.quotation.QuotationRequestModel;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Entity("requests_for_quotation")
public class QuotationRequestEntity extends AbstractEntity implements QuotationRequestModel {

    @Id
    private QuotationRequestKey key;

    @Column("issue_id")
    private UUID issueId;

    @Column("contractor_id")
    private UUID contractorId;

    @Column("triggered_by")
    private UUID triggeredBy;

    @Column("description")
    private String description;

    @Column("status")
    private String status;

    public QuotationRequestKey getKey() {
        return key;
    }

    public void setKey(QuotationRequestKey key) {
        this.key = key;
    }

    @Override
    public UUID getId() {
        return Optional.ofNullable(key)
            .map(QuotationRequestKey::getRequestId)
            .orElse(null);
    }

    @Override
    public UUID getProjectId() {
        return Optional.ofNullable(key)
            .map(QuotationRequestKey::getProjectId)
            .orElse(null);
    }

    @Override
    public UUID getIssueId() {
        return issueId;
    }

    public void setIssueId(UUID issueId) {
        this.issueId = issueId;
    }

    @Override
    public UUID getContractorId() {
        return contractorId;
    }

    public void setContractorId(UUID contractorId) {
        this.contractorId = contractorId;
    }

    @Override
    public UUID getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(UUID triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Status getStatus() {
        return status != null ? Status.valueOf(status) : null;
    }

    public void setStatus(Status status) {
        this.status = status != null ? status.name() : null;
    }

    // Setter for string status for Cassandra mapping
    public void setStatus(String status) {
        this.status = status;
    }

    public void generateId() {
        if (this.key == null) {
            this.key = new QuotationRequestKey();
        }
        if (this.key.getRequestId() == null) {
            this.key.setRequestId(UUID.randomUUID());
        }
    }

    public void setProjectId(UUID projectId) {
        if (this.key == null) {
            this.key = new QuotationRequestKey();
        }
        this.key.setProjectId(projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

}
