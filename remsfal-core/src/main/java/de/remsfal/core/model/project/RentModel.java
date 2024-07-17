package de.remsfal.core.model.project;

import java.time.LocalDate;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface RentModel {

    public enum BillingCycle {
        WEEKLY,
        MONTHLY;
    }

    BillingCycle getBillingCycle();

    LocalDate getFirstPaymentDate();

    LocalDate getLastPaymentDate();

    Float getBasicRent();

    Float getOperatingCostsPrepayment();

    Float getHeatingCostsPrepayment();

}
