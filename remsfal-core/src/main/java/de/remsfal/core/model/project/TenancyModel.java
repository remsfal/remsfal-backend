package de.remsfal.core.model.project;

import de.remsfal.core.model.CustomerModel;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface TenancyModel {

    String getId();

    List<? extends RentModel> getRent();

    CustomerModel getTenant();

    LocalDate getStartOfRental();

    LocalDate getEndOfRental();

}
