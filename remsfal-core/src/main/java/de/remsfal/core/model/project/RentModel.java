package de.remsfal.core.model.project;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface RentModel {

    public enum BillingCycle {
        WEEKLY,
        MONTHLY;
    }

    BillingCycle getBillingCycle();

    Float getBasicRent();

    Float getOperatingCostsPrepayment();

    Float getHeatingCostsPrepayment();

}
