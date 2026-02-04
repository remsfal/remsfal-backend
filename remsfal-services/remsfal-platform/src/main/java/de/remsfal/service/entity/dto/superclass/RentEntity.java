package de.remsfal.service.entity.dto.superclass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
    @Column(name = "agreement_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID agreementId;

    @Id
    @Column(name = "first_payment", columnDefinition = "date", nullable = false)
    private LocalDate firstPaymentDate;

    @Column(name = "last_payment", columnDefinition = "date")
    private LocalDate lastPaymentDate;

    @Column(name = "billing_cycle", nullable = false)
    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle;

    @Column(name = "basic_rent", columnDefinition = "numeric(6,2)", precision=6, scale=2)
    private BigDecimal basicRent;

    @Column(name = "operating_costs_prepayment", columnDefinition = "numeric(6,2)", precision=6, scale=2)
    private BigDecimal operatingCostsPrepayment;

    @Column(name = "heating_costs_prepayment", columnDefinition = "numeric(6,2)",  precision=6, scale=2)
    private BigDecimal heatingCostsPrepayment;

    public UUID getAgreementId() {
        return agreementId;
    }

    public void setAgreementId(final UUID agreementId) {
        this.agreementId = agreementId;
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
        return Optional.ofNullable(basicRent)
            .map(BigDecimal::floatValue)
            .orElse(null);
    }

    public void setBasicRent(final Float basicRent) {
        this.basicRent = BigDecimal.valueOf(basicRent).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Float getOperatingCostsPrepayment() {
        return Optional.ofNullable(operatingCostsPrepayment)
            .map(BigDecimal::floatValue)
            .orElse(null);
    }

    public void setOperatingCostsPrepayment(final Float operatingCostsPrepayment) {
        this.operatingCostsPrepayment = BigDecimal.valueOf(operatingCostsPrepayment).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Float getHeatingCostsPrepayment() {
        return Optional.ofNullable(heatingCostsPrepayment)
            .map(BigDecimal::floatValue)
            .orElse(null);
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
                && Objects.equals(agreementId, e.agreementId)
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
