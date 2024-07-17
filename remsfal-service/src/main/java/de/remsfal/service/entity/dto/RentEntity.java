package de.remsfal.service.entity.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import de.remsfal.core.model.project.RentModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "RENT")
public class RentEntity extends AbstractEntity implements RentModel {

    @Id
    @Column(name = "ID", columnDefinition = "char", nullable = false, length = 36)
    private String id;

    @Column(name = "BILLING_CYCLE", nullable = false)
    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle;

    @Column(name = "FIRST_PAYMENT", columnDefinition = "date", nullable = false)
    private LocalDate firstPaymentDate;

    @Column(name = "LAST_PAYMENT", columnDefinition = "date")
    private LocalDate lastPaymentDate;

    @Column(name = "BASIC_RENT", columnDefinition = "decimal", precision=6, scale=2)
    private BigDecimal basicRent;

    @Column(name = "OPERATING_COSTS_PREPAYMENT", columnDefinition = "decimal", precision=6, scale=2)
    private BigDecimal operatingCostsPrepayment;

    @Column(name = "HEATING_COSTS_PREPAYMENT", columnDefinition = "decimal",  precision=6, scale=2)
    private BigDecimal heatingCostsPrepayment;

    public String getId() {
        return id;
    }

    @Override
    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public BillingCycle getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(final BillingCycle billingCycle) {
        this.billingCycle = billingCycle;
    }

    @Override
    public LocalDate getFirstPaymentDate() {
        return firstPaymentDate;
    }

    public void setFirstPaymentDate(final LocalDate firstPaymentDate) {
        this.firstPaymentDate = firstPaymentDate;
    }

    @Override
    public LocalDate getLastPaymentDate() {
        return lastPaymentDate;
    }

    public void setLastPaymentDate(final LocalDate lastPaymentDate) {
        this.lastPaymentDate = lastPaymentDate;
    }

    @Override
    public Float getBasicRent() {
        return basicRent.floatValue();
    }

    public void setBasicRent(final Float basicRent) {
        this.basicRent = BigDecimal.valueOf(basicRent);
    }

    @Override
    public Float getOperatingCostsPrepayment() {
        return operatingCostsPrepayment.floatValue();
    }

    public void setOperatingCostsPrepayment(final Float operatingCostsPrepayment) {
        this.operatingCostsPrepayment = BigDecimal.valueOf(operatingCostsPrepayment);
    }

    @Override
    public Float getHeatingCostsPrepayment() {
        return heatingCostsPrepayment.floatValue();
    }

    public void setHeatingCostsPrepayment(final Float heatingCostsPrepayment) {
        this.heatingCostsPrepayment = BigDecimal.valueOf(heatingCostsPrepayment);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof RentEntity e) {
            return super.equals(e)
                && Objects.equals(id, e.id)
                && Objects.equals(billingCycle, e.billingCycle)
                && Objects.equals(firstPaymentDate, e.firstPaymentDate)
                && Objects.equals(lastPaymentDate, e.lastPaymentDate)
                && Objects.equals(basicRent, e.basicRent)
                && Objects.equals(operatingCostsPrepayment, e.operatingCostsPrepayment)
                && Objects.equals(heatingCostsPrepayment, e.heatingCostsPrepayment);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
