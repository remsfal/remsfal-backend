package de.remsfal.ticketing.entity.dto;

import de.remsfal.common.util.UUIDv7;
import de.remsfal.core.model.ticketing.QuotationModel;
import de.remsfal.core.model.ticketing.QuotationModel.QuotationStatus;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity("quotations")
public class QuotationEntity extends AbstractEntity implements QuotationModel {

    @Id
    private QuotationKey key;

    @Column("request_id")
    private UUID requestId;

    @Column("project_id")
    private UUID projectId;

    @Column("trigger_id")
    private UUID triggerId;

    @Column("contractor_id")
    private UUID contractorId;

    @Column("attachments")
    private List<UUID> attachments;

    @Column("valid_until")
    private Instant validUntil;

    @Column("status")
    private String status;

    @Override
    public UUID getId() {
        return getQuotationId();
    }

    public QuotationKey getKey() {
        return key;
    }

    public void setKey(QuotationKey key) {
        this.key = key;
    }

    @Override
    public UUID getIssueId() {
        return Optional.ofNullable(key)
            .map(QuotationKey::getIssueId)
            .orElse(null);
    }

    public void setIssueId(UUID issueId) {
        if (this.key == null) {
            this.key = new QuotationKey();
        }
        this.key.setIssueId(issueId);
    }

    public UUID getQuotationId() {
        return Optional.ofNullable(key)
            .map(QuotationKey::getQuotationId)
            .orElse(null);
    }

    public void generateId() {
        if (this.key == null) {
            this.key = new QuotationKey();
        }
        if (this.key.getQuotationId() == null) {
            this.key.setQuotationId(UUIDv7.randomUUID());
        }
    }

    @Override
    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    @Override
    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    @Override
    public UUID getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(UUID triggerId) {
        this.triggerId = triggerId;
    }

    @Override
    public UUID getContractorId() {
        return contractorId;
    }

    public void setContractorId(UUID contractorId) {
        this.contractorId = contractorId;
    }

    @Override
    public List<UUID> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<UUID> attachments) {
        this.attachments = attachments;
    }

    @Override
    public Instant getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }

    @Override
    public QuotationStatus getStatus() {
        return status != null ? QuotationStatus.valueOf(status) : null;
    }

    public void setStatus(QuotationStatus status) {
        this.status = status != null ? status.name() : null;
    }

}
