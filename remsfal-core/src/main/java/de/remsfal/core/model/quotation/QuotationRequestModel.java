package de.remsfal.core.model.quotation;

import java.util.UUID;

/**
 * Model interface for a quotation request.
 * 
 * @author GitHub Copilot
 */
public interface QuotationRequestModel {

    /**
     * Get the ID of the request.
     *
     * @return the request ID
     */
    UUID getId();

    /**
     * Get the project ID of the request.
     *
     * @return the project ID
     */
    UUID getProjectId();

    /**
     * Get the issue ID of the request.
     *
     * @return the issue ID
     */
    UUID getIssueId();

    /**
     * Get the contractor ID of the request.
     *
     * @return the contractor ID
     */
    UUID getContractorId();

    /**
     * Get the ID of the user who triggered the request.
     *
     * @return the triggered by user ID
     */
    UUID getTriggeredBy();

    /**
     * Get the description of the request.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Get the status of the request.
     *
     * @return the status
     */
    Status getStatus();

    /**
     * Status enumeration for quotation requests.
     */
    enum Status {
        VALID,
        INVALID
    }

}
