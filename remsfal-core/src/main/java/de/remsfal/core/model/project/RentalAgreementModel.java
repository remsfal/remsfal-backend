package de.remsfal.core.model.project;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface RentalAgreementModel {

    UUID getId();

    List<? extends TenantModel> getTenants();

    LocalDate getStartOfRental();

    LocalDate getEndOfRental();

    List<? extends RentModel> getPropertyRents();

    List<? extends RentModel> getSiteRents();

    List<? extends RentModel> getBuildingRents();

    List<? extends RentModel> getApartmentRents();

    List<? extends RentModel> getStorageRents();

    List<? extends RentModel> getCommercialRents();

    public default Boolean isActive() {
        if (this.getEndOfRental() == null) {
            return true;
        } else {
            return this.getEndOfRental().isAfter(LocalDate.now());
        }
    }

}
