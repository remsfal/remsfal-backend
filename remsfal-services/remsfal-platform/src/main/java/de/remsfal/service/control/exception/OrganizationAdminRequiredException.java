package de.remsfal.service.control.exception;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

/**
 * Thrown if an operation would leave an organization without an active administrator. Every organization must
 * always have at least one active employee with role OWNER or MANAGER.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class OrganizationAdminRequiredException extends ClientErrorException {

    private static final long serialVersionUID = 1L;

    /**
     * Construct a new "conflict" exception.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     */
    public OrganizationAdminRequiredException(final String message) {
        super(message, Response.Status.CONFLICT);
    }

}
