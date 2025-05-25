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

    Float getBasicRent(); // Nettokaltmiete

    Float getOperatingCostsPrepayment(); // Betriebskostenvorauszahlung

    Float getHeatingCostsPrepayment(); // Heizkostenvorauszahlung

}
