package de.remsfal.core.model.tenancy;

import java.util.UUID;

/**
 * Model representing a tenant in a rental agreement.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface CoTenantModel {

    UUID getId();

    String getFirstName();

    String getLastName();

    /**
     * Returns the associated user ID if this tenant is linked to a user account.
     *
     * @return user ID or null if not linked
     */
    UUID getUserId();
}
