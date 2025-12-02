package de.remsfal.core.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface CustomerModel extends UserModel {

    @Override
    default String getName() {
        final var firstName = this.getFirstName() != null ? this.getFirstName() : "";
        final var lastName = this.getLastName() != null ? this.getLastName() : "";

        if (firstName.isEmpty() && lastName.isEmpty()) {
            return null;
        }

        return String.format("%s %s", this.getFirstName(), this.getLastName()).trim();
    }

    String getFirstName();

    String getLastName();

    AddressModel getAddress();

    String getMobilePhoneNumber();

    String getBusinessPhoneNumber();

    String getPrivatePhoneNumber();

    LocalDate getRegisteredDate();

    LocalDateTime getLastLoginDate();

    String getLocale();

}
