package de.remsfal.core.model.project;

import java.time.LocalDate;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface RentModel {

    LocalDate getFirstPaymentDate();

    LocalDate getLastPaymentDate();

    public enum BillingCycle {
        WEEKLY,
        MONTHLY;
    }

    BillingCycle getBillingCycle();

    Float getBasicRent(); // Nettokaltmiete

    Float getOperatingCostsPrepayment(); // Betriebskostenvorauszahlung

    Float getHeatingCostsPrepayment(); // Heizkostenvorauszahlung

}
