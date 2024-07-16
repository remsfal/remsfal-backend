package de.remsfal.core.model.project;

import de.remsfal.core.model.CustomerModel;

import java.time.LocalDate;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface TenancyModel {

    String getId();

    RentModel getRent();

    CustomerModel getTenant();

    LocalDate getStartOfRental();

    LocalDate getEndOfRental();

}
