package de.remsfal.ticketing.entity.dto;

import de.remsfal.common.util.UUIDv7;
import de.remsfal.core.model.ticketing.OrderPlacementModel;
import de.remsfal.core.model.ticketing.OrderPlacementModel.OrderPlacementStatus;
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

    @Column("orderer_id")
    private UUID ordererId;

    @Column("ordered_by")
    private String orderedBy;

    @Column("contractor_id")
    private UUID contractorId;

    @Column("confirmor_id")
    private UUID confirmorId;

    @Column("confirmed_by")
    private String confirmedBy;

    @Column("status")
    private String status;

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

    @Override
    public OrderPlacementStatus getStatus() {
        return status != null ? OrderPlacementStatus.valueOfStatus(status) : null;
    }

    public void setStatus(final OrderPlacementStatus status) {
        this.status = status != null ? status.getValue() : null;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

}
