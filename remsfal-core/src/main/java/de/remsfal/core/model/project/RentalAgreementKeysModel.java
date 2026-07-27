package de.remsfal.core.model.project;

import java.time.LocalDate;

public interface RentalAgreementKeysModel {

    Integer getAmountOfKeys();

    LocalDate getIssuedAt();

    LocalDate getReturnedAt();

    String getKeyType();

}
