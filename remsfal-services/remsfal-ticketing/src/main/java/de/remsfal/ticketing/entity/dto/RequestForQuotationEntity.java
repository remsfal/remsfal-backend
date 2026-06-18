package de.remsfal.ticketing.entity.dto;

import de.remsfal.common.util.UUIDv7;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.Optional;
import java.util.UUID;

@Entity("requests_for_quotation")
public class RequestForQuotationEntity extends AbstractEntity {

    public enum RequestStatus {
        VALID,
        INVALID
    }

    @Id
    private RequestForQuotationKey key;

    @Column("project_id")
    private UUID projectId;

    @Column("trigger_id")
    private UUID triggerId;

    @Column("contractor_id")
    private UUID contractorId;

    @Column("free_text")
    private String freeText;

    @Column("status")
    private String status;

    public RequestForQuotationKey getKey() {
        return key;
    }

    public void setKey(RequestForQuotationKey key) {
        this.key = key;
    }

    public UUID getIssueId() {
        return Optional.ofNullable(key)
            .map(RequestForQuotationKey::getIssueId)
            .orElse(null);
    }

    public void setIssueId(UUID issueId) {
        if (this.key == null) {
            this.key = new RequestForQuotationKey();
        }
        this.key.setIssueId(issueId);
    }

    public UUID getRequestId() {
        return Optional.ofNullable(key)
            .map(RequestForQuotationKey::getRequestId)
            .orElse(null);
    }

    public void generateId() {
        if (this.key == null) {
            this.key = new RequestForQuotationKey();
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

    public String getFreeText() {
        return freeText;
    }

    public void setFreeText(String freeText) {
        this.freeText = freeText;
    }

    public RequestStatus getStatus() {
        return status != null ? RequestStatus.valueOf(status) : null;
    }

    public void setStatus(RequestStatus status) {
        this.status = status != null ? status.name() : null;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
