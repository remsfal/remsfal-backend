package de.remsfal.ticketing.entity.dto;

import de.remsfal.common.util.UUIDv7;
import de.remsfal.core.model.ticketing.QuotationRequestModel;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.Optional;
import java.util.UUID;

@Entity("quotation_requests")
public class QuotationRequestEntity extends AbstractEntity implements QuotationRequestModel {

    public enum RequestStatus {
        VALID,
        INVALID
    }

    @Id
    private QuotationRequestKey key;

    @Column("project_id")
    private UUID projectId;

    @Column("trigger_id")
    private UUID triggerId;

    @Column("contractor_id")
    private UUID contractorId;

    @Column("organization_id")
    private UUID organizationId;

    @Column("free_text")
    private String freeText;

    @Column("status")
    private String status;

    @Override
    public UUID getId() {
        return getRequestId();
    }

    public QuotationRequestKey getKey() {
        return key;
    }

    public void setKey(QuotationRequestKey key) {
        this.key = key;
    }

    public UUID getIssueId() {
        return Optional.ofNullable(key)
            .map(QuotationRequestKey::getIssueId)
            .orElse(null);
    }

    public void setIssueId(UUID issueId) {
        if (this.key == null) {
            this.key = new QuotationRequestKey();
        }
        this.key.setIssueId(issueId);
    }

    public UUID getRequestId() {
        return Optional.ofNullable(key)
            .map(QuotationRequestKey::getRequestId)
            .orElse(null);
    }

    public void generateId() {
        if (this.key == null) {
            this.key = new QuotationRequestKey();
        }
        if (this.key.getRequestId() == null) {
            this.key.setRequestId(UUIDv7.randomUUID());
        }
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(UUID triggerId) {
        this.triggerId = triggerId;
    }

    public UUID getContractorId() {
        return contractorId;
    }

    public void setContractorId(UUID contractorId) {
        this.contractorId = contractorId;
    }

    @Override
    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public String getFreeText() {
        return freeText;
    }

    public void setFreeText(String freeText) {
        this.freeText = freeText;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public RequestStatus getRequestStatus() {
        return status != null ? RequestStatus.valueOf(status) : null;
    }

    public void setStatus(RequestStatus status) {
        this.status = status != null ? status.name() : null;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
