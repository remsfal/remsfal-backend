package de.remsfal.ticketing.entity.dto;

import de.remsfal.core.model.ticketing.QuotationModel;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.Optional;
import java.util.UUID;

/**
 * Entity for quotation stored in Cassandra.
 * 
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity("quotations")
public class QuotationEntity extends AbstractEntity implements QuotationModel {

    @Id
    private QuotationKey key;

    @Column("requester_id")
    private UUID requesterId;

    @Column("contractor_id")
    private UUID contractorId;

    @Column("text")
    private String text;

    @Column("status")
    private String status;

    public QuotationKey getKey() {
        return key;
    }

    public void setKey(QuotationKey key) {
        this.key = key;
    }

    @Override
    public UUID getId() {
        return Optional.ofNullable(key)
            .map(QuotationKey::getQuotationId)
            .orElse(null);
    }

    @Override
    public UUID getProjectId() {
        return Optional.ofNullable(key)
            .map(QuotationKey::getProjectId)
            .orElse(null);
    }

    @Override
    public UUID getIssueId() {
        return Optional.ofNullable(key)
            .map(QuotationKey::getIssueId)
            .orElse(null);
    }

    @Override
    public UUID getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(UUID requesterId) {
        this.requesterId = requesterId;
    }

    @Override
    public UUID getContractorId() {
        return contractorId;
    }

    public void setContractorId(UUID contractorId) {
        this.contractorId = contractorId;
    }

    @Override
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public QuotationStatus getStatus() {
        return status != null ? QuotationStatus.valueOf(status) : null;
    }

    public void setStatus(QuotationStatus status) {
        this.status = status != null ? status.name() : null;
    }

    /**
     * Setter for string status used by Cassandra during deserialization.
     * Cassandra stores the status as a string in the database.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    public void generateId() {
        if (this.key == null) {
            this.key = new QuotationKey();
        }
        if (this.key.getQuotationId() == null) {
            this.key.setQuotationId(UUID.randomUUID());
        }
    }

    public void setProjectId(UUID projectId) {
        if (this.key == null) {
            this.key = new QuotationKey();
        }
        this.key.setProjectId(projectId);
    }

    public void setIssueId(UUID issueId) {
        if (this.key == null) {
            this.key = new QuotationKey();
        }
        this.key.setIssueId(issueId);
    }

}
