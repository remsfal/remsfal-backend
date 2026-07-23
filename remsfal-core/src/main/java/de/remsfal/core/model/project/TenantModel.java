package de.remsfal.core.model.project;

import java.time.LocalDate;

import de.remsfal.core.model.AddressModel;
import de.remsfal.core.model.tenancy.CoTenantModel;

/**
 * Model representing a tenant in a rental agreement.
 * Tenants are independent entities that can optionally be linked to a user account.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface TenantModel extends CoTenantModel {

    String getEmail();

    String getMobilePhoneNumber();

    String getBusinessPhoneNumber();

    String getPrivatePhoneNumber();

    AddressModel getAddress();

    String getPlaceOfBirth();

    LocalDate getDateOfBirth();

}
