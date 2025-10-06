package de.remsfal.core.model;

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
}