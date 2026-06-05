package de.remsfal.core.model;

import jakarta.annotation.Nullable;
import java.util.UUID;

/**
 * Model interface for a contractor.
 */
public interface ContractorModel {

    /**
     * Get the ID of the contractor.
     *
     * @return the ID
     */
    UUID getId();

    /**
     * Get the project ID of the contractor.
     *
     * @return the project ID
     */
    UUID getProjectId();

    /**
     * Get the organization ID linked to this contractor.
     *
     * @return the organization ID, or null if not linked
     */
    @Nullable
    UUID getOrganizationId();

    /**
     * Get the company name of the contractor.
     *
     * @return the company name
     */
    String getCompanyName();

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
     * Get the contact person at the contractor company.
     *
     * @return the contact person name, or null
     */
    @Nullable
    String getContactPerson();

    /**
     * Get the remarks for this contractor.
     *
     * @return the remarks, or null
     */
    @Nullable
    String getRemarks();

    /**
     * Get the organization linked to this contractor.
     *
     * @return the organization, or null if not linked
     */
    @Nullable
    OrganizationModel getOrganization();

    /**
     * Get the address of the contractor.
     *
     * @return the address
     */
    @Nullable
    AddressModel getAddress();

}
