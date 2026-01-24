package de.remsfal.core.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface CustomerModel extends UserModel {

    @Override
    default String getName() {
        final String firstName = this.getFirstName() != null ? this.getFirstName() : "";
        final String lastName = this.getLastName() != null ? this.getLastName() : "";

        if (firstName.isEmpty() && lastName.isEmpty()) {
            return this.getEmail();
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

    List<String> getAdditionalEmails();
}
