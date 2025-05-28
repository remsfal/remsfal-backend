package de.remsfal.service.entity.dto.superclass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

import de.remsfal.core.model.project.RentModel;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@MappedSuperclass
public abstract class RentEntity extends MetaDataEntity implements RentModel {

    @Id
    @Column(name = "TENANCY_ID", columnDefinition = "char", nullable = false, updatable = false, length = 36)
    private String tenancyId;

    @Id
    @Column(name = "FIRST_PAYMENT", columnDefinition = "date", nullable = false)
    private LocalDate firstPaymentDate;

    @Column(name = "LAST_PAYMENT", columnDefinition = "date")
    private LocalDate lastPaymentDate;

    @Column(name = "BILLING_CYCLE", nullable = false)
    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle;

    @Column(name = "BASIC_RENT", columnDefinition = "decimal", precision=6, scale=2)
    private BigDecimal basicRent;

    @Column(name = "OPERATING_COSTS_PREPAYMENT", columnDefinition = "decimal", precision=6, scale=2)
    private BigDecimal operatingCostsPrepayment;

    @Column(name = "HEATING_COSTS_PREPAYMENT", columnDefinition = "decimal",  precision=6, scale=2)
    private BigDecimal heatingCostsPrepayment;

    public String getTenancyId() {
        return tenancyId;
    }

    public void setTenancyId(final String tenancyId) {
        this.tenancyId = tenancyId;
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
    public BillingCycle getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(final BillingCycle billingCycle) {
        this.billingCycle = billingCycle;
    }

    @Override
    public Float getBasicRent() {
        return basicRent.floatValue();
    }

    public void setBasicRent(final Float basicRent) {
        this.basicRent = BigDecimal.valueOf(basicRent).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Float getOperatingCostsPrepayment() {
        return operatingCostsPrepayment.floatValue();
    }

    public void setOperatingCostsPrepayment(final Float operatingCostsPrepayment) {
        this.operatingCostsPrepayment = BigDecimal.valueOf(operatingCostsPrepayment).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Float getHeatingCostsPrepayment() {
        return heatingCostsPrepayment.floatValue();
    }

    public void setHeatingCostsPrepayment(final Float heatingCostsPrepayment) {
        this.heatingCostsPrepayment = BigDecimal.valueOf(heatingCostsPrepayment).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof RentEntity e) {
            return super.equals(e)
                && Objects.equals(tenancyId, e.tenancyId)
                && Objects.equals(firstPaymentDate, e.firstPaymentDate)
                && Objects.equals(lastPaymentDate, e.lastPaymentDate)
                && Objects.equals(billingCycle, e.billingCycle)
                && Objects.equals(basicRent, e.basicRent)
                && Objects.equals(operatingCostsPrepayment, e.operatingCostsPrepayment)
                && Objects.equals(heatingCostsPrepayment, e.heatingCostsPrepayment);
        }
        return false;
    }

}
