package de.remsfal.ticketing.entity.dto;

import de.remsfal.common.util.UUIDv7;
import de.remsfal.core.model.ticketing.QuotationRequestModel;
import de.remsfal.core.model.ticketing.QuotationRequestModel.RequestStatus;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.Optional;
import java.util.UUID;

@Entity("quotation_requests")
public class QuotationRequestEntity extends AbstractEntity implements QuotationRequestModel {

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

    @Column("contractor_name")
    private String contractorName;

    @Column("scope_of_work")
    private String scopeOfWork;

    @Column("project_owner")
    private String projectOwner;

    @Column("project_care_of")
    private String projectCareOf;

    @Column("project_billing_address_1")
    private String projectBillingAddress1;

    @Column("project_billing_address_2")
    private String projectBillingAddress2;

    @Column("project_billing_address_3")
    private String projectBillingAddress3;

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

    @Override
    public String getContractorName() {
        return contractorName;
    }

    public void setContractorName(final String contractorName) {
        this.contractorName = contractorName;
    }

    public String getScopeOfWork() {
        return scopeOfWork;
    }

    public void setScopeOfWork(String scopeOfWork) {
        this.scopeOfWork = scopeOfWork;
    }

    @Override
    public String getProjectOwner() {
        return projectOwner;
    }

    public void setProjectOwner(String projectOwner) {
        this.projectOwner = projectOwner;
    }

    @Override
    public String getProjectCareOf() {
        return projectCareOf;
    }

    public void setProjectCareOf(String projectCareOf) {
        this.projectCareOf = projectCareOf;
    }

    @Override
    public String getProjectBillingAddress1() {
        return projectBillingAddress1;
    }

    public void setProjectBillingAddress1(String projectBillingAddress1) {
        this.projectBillingAddress1 = projectBillingAddress1;
    }

    @Override
    public String getProjectBillingAddress2() {
        return projectBillingAddress2;
    }

    public void setProjectBillingAddress2(String projectBillingAddress2) {
        this.projectBillingAddress2 = projectBillingAddress2;
    }

    @Override
    public String getProjectBillingAddress3() {
        return projectBillingAddress3;
    }

    public void setProjectBillingAddress3(String projectBillingAddress3) {
        this.projectBillingAddress3 = projectBillingAddress3;
    }

    @Override
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
