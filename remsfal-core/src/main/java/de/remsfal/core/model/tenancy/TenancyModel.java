package de.remsfal.core.model.tenancy;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import de.remsfal.core.model.RentalUnitModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface TenancyModel {

    UUID getId();

    List<? extends CoTenantModel> getTenants();

    LocalDate getStartOfRental();

    LocalDate getEndOfRental();

    List<? extends RentalUnitModel> getRentalUnits();

    public default Boolean isActive() {
        if (this.getEndOfRental() == null) {
            return true;
        } else {
            return this.getEndOfRental().isAfter(LocalDate.now());
        }
    }

}
