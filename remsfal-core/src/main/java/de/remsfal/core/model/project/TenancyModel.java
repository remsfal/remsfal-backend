package de.remsfal.core.model.project;

import de.remsfal.core.model.CustomerModel;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface TenancyModel {

    UUID getId();

    List<? extends CustomerModel> getTenants();

    LocalDate getStartOfRental();

    LocalDate getEndOfRental();

    public default Boolean isActive() {
        if (this.getEndOfRental() == null) {
            return true;
        } else {
            return this.getEndOfRental().isAfter(LocalDate.now());
        }
    }

}
