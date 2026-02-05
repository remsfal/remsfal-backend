package de.remsfal.core.model.project;

import java.time.LocalDate;
import java.util.UUID;

import de.remsfal.core.model.AddressModel;

/**
 * Model representing a tenant in a rental agreement.
 * Tenants are independent entities that can optionally be linked to a user account.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface TenantModel {

    UUID getId();

    String getFirstName();

    String getLastName();

    String getEmail();

    String getMobilePhoneNumber();

    String getBusinessPhoneNumber();

    String getPrivatePhoneNumber();

    AddressModel getAddress();

    String getPlaceOfBirth();

    LocalDate getDateOfBirth();

    /**
     * Returns the associated user ID if this tenant is linked to a user account.
     *
     * @return user ID or null if not linked
     */
    UUID getUserId();
}
