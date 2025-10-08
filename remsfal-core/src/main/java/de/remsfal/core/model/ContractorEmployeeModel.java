package de.remsfal.core.model;

import java.util.UUID;

/**
 * Model interface for a contractor employee.
 */
public interface ContractorEmployeeModel {

    /**
     * Get the contractor ID of the contractor employee.
     *
     * @return the contractor ID
     */
    UUID getContractorId();

    /**
     * Get the user ID of the contractor employee.
     *
     * @return the user ID
     */
    UUID getUserId();

    /**
     * Get the responsibility of the contractor employee.
     *
     * @return the responsibility
     */
    String getResponsibility();

    /**
     * Get the user of the contractor employee.
     *
     * @return the user
     */
    UserModel getUser();
}