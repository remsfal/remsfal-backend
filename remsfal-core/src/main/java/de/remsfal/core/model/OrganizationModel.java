package de.remsfal.core.model;

import jakarta.annotation.Nullable;
import java.util.UUID;

/**
 * Model interface for an organization.
 */
public interface OrganizationModel {
    /**
     * Get the ID of the organization.
     *
     * @return the ID
     */
    UUID getId();

    /**
     * Get the company name of the organization.
     *
     * @return the company name
     */
    String getName();

    /**
     * Get the phone number of the organization.
     *
     * @return the phone number
     */
    String getPhone();

    /**
     * Get the email of the organization.
     *
     * @return the email
     */
    String getEmail();

    /**
     * Get the trade of the organization.
     *
     * @return the trade
     */
    String getTrade();

    /**
     * Get the address of the organization.
     *
     * @return the address
     */
    @Nullable
    AddressModel getAddress();
}
