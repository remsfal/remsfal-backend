package de.remsfal.ticketing.entity.dto;

import de.remsfal.common.util.UUIDv7;
import de.remsfal.core.model.ticketing.QuotationModel;
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

    @Column("offerer_id")
    private UUID offererId;

    @Column("offered_by")
    private String offeredBy;

    @Column("contractor_id")
    private UUID contractorId;

    @Column("contractor_name")
    private String contractorName;

    @Column("organization_id")
    private UUID organizationId;

    @Column("status")
    private String status;

    @Column("attachments")
    private List<UUID> attachments;

    @Column("valid_until")
    private Instant validUntil;

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
    public UUID getOffererId() {
        return offererId;
    }

    public void setOffererId(UUID offererId) {
        this.offererId = offererId;
    }

    @Override
    public String getOfferedBy() {
        return offeredBy;
    }

    public void setOfferedBy(String offeredBy) {
        this.offeredBy = offeredBy;
    }

    @Override
    public UUID getContractorId() {
        return contractorId;
    }

    public void setContractorId(UUID contractorId) {
        this.contractorId = contractorId;
    }

    @Override
    public String getContractorName() {
        return contractorName;
    }

    public void setContractorName(String contractorName) {
        this.contractorName = contractorName;
    }

    @Override
    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    @Override
    public QuotationStatus getStatus() {
        return status != null ? QuotationStatus.valueOf(status) : null;
    }

    public void setStatus(QuotationStatus status) {
        this.status = status != null ? status.name() : null;
    }

    public void setStatus(String status) {
        this.status = status;
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

}
