package de.remsfal.core.model;

import jakarta.annotation.Nullable;
import java.util.UUID;

/**
 * Model interface for an organization.
 */
public interface OrganizationModel {
    /**
     * Get the ID of the contractor.
     *
     * @return the ID
     */
    UUID getId();

    /**
     * Get the company name of the contractor.
     *
     * @return the company name
     */
    String getName();

    /**
     * Get the phone number of the contractor.
     *
     * @return the phone number
     */
    String getPhone();

    /**
     * Get the email of the contractor.
     *
     * @return the email
     */
    String getEmail();

    /**
     * Get the trade of the contractor.
     *
     * @return the trade
     */
    String getTrade();

    /**
     * Get the address of the contractor.
     *
     * @return the address
     */
    @Nullable
    AddressModel getAddress();
}
