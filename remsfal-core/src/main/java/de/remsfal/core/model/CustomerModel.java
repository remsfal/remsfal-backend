package de.remsfal.core.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface CustomerModel extends UserModel {

    LocalDate getRegisteredDate();

    LocalDateTime getLastLoginDate();

}
