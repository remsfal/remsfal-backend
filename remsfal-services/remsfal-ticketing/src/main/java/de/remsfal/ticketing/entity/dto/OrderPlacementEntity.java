package de.remsfal.ticketing.entity.dto;

import de.remsfal.common.util.UUIDv7;
import de.remsfal.core.model.ticketing.OrderPlacementModel;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.Optional;
import java.util.UUID;

@Entity("order_placements")
public class OrderPlacementEntity extends AbstractEntity implements OrderPlacementModel {

    @Id
    private OrderPlacementKey key;

    @Column("quotation_id")
    private UUID quotationId;

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

    @Column("orderer_id")
    private UUID ordererId;

    @Column("ordered_by")
    private String orderedBy;

    @Column("contractor_id")
    private UUID contractorId;

    @Column("contractor_name")
    private String contractorName;

    @Column("organization_id")
    private UUID organizationId;

    @Column("status")
    private String status;

    @Column("confirmor_id")
    private UUID confirmorId;

    @Column("confirmed_by")
    private String confirmedBy;

    @Override
    public UUID getId() {
        return getPlacementId();
    }

    public OrderPlacementKey getKey() {
        return key;
    }

    public void setKey(final OrderPlacementKey key) {
        this.key = key;
    }

    @Override
    public UUID getIssueId() {
        return Optional.ofNullable(key)
            .map(OrderPlacementKey::getIssueId)
            .orElse(null);
    }

    public void setIssueId(final UUID issueId) {
        if (this.key == null) {
            this.key = new OrderPlacementKey();
        }
        this.key.setIssueId(issueId);
    }

    public UUID getPlacementId() {
        return Optional.ofNullable(key)
            .map(OrderPlacementKey::getPlacementId)
            .orElse(null);
    }

    public void generateId() {
        if (this.key == null) {
            this.key = new OrderPlacementKey();
        }
        if (this.key.getPlacementId() == null) {
            this.key.setPlacementId(UUIDv7.randomUUID());
        }
    }

    @Override
    public UUID getQuotationId() {
        return quotationId;
    }

    public void setQuotationId(final UUID quotationId) {
        this.quotationId = quotationId;
    }

    @Override
    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(final UUID projectId) {
        this.projectId = projectId;
    }

    @Override
    public String getProjectOwner() {
        return projectOwner;
    }

    public void setProjectOwner(final String projectOwner) {
        this.projectOwner = projectOwner;
    }

    @Override
    public String getProjectCareOf() {
        return projectCareOf;
    }

    public void setProjectCareOf(final String projectCareOf) {
        this.projectCareOf = projectCareOf;
    }

    @Override
    public String getProjectBillingAddress1() {
        return projectBillingAddress1;
    }

    public void setProjectBillingAddress1(final String projectBillingAddress1) {
        this.projectBillingAddress1 = projectBillingAddress1;
    }

    @Override
    public String getProjectBillingAddress2() {
        return projectBillingAddress2;
    }

    public void setProjectBillingAddress2(final String projectBillingAddress2) {
        this.projectBillingAddress2 = projectBillingAddress2;
    }

    @Override
    public String getProjectBillingAddress3() {
        return projectBillingAddress3;
    }

    public void setProjectBillingAddress3(final String projectBillingAddress3) {
        this.projectBillingAddress3 = projectBillingAddress3;
    }

    @Override
    public UUID getOrdererId() {
        return ordererId;
    }

    public void setOrdererId(final UUID ordererId) {
        this.ordererId = ordererId;
    }

    @Override
    public String getOrderedBy() {
        return orderedBy;
    }

    public void setOrderedBy(final String orderedBy) {
        this.orderedBy = orderedBy;
    }

    @Override
    public UUID getContractorId() {
        return contractorId;
    }

    public void setContractorId(final UUID contractorId) {
        this.contractorId = contractorId;
    }

    @Override
    public String getContractorName() {
        return contractorName;
    }

    public void setContractorName(final String contractorName) {
        this.contractorName = contractorName;
    }

    @Override
    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(final UUID organizationId) {
        this.organizationId = organizationId;
    }

    @Override
    public OrderPlacementStatus getStatus() {
        return status != null ? OrderPlacementStatus.valueOf(status) : null;
    }

    public void setStatus(final OrderPlacementStatus status) {
        this.status = status != null ? status.name() : null;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    @Override
    public UUID getConfirmorId() {
        return confirmorId;
    }

    public void setConfirmorId(final UUID confirmorId) {
        this.confirmorId = confirmorId;
    }

    @Override
    public String getConfirmedBy() {
        return confirmedBy;
    }

    public void setConfirmedBy(final String confirmedBy) {
        this.confirmedBy = confirmedBy;
    }

}
