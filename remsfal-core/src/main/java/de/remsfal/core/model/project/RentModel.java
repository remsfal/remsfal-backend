package de.remsfal.core.model.project;

import java.time.LocalDate;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface RentModel {

    UUID getUnitId();

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
