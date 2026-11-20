package de.remsfal.service.control.exception;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

/**
 * Thrown if an operation would leave a project without a proprietor. Every project must always have at least one
 * proprietor, either a user or an organization.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class ProprietorRequiredException extends ClientErrorException {

    private static final long serialVersionUID = 1L;

    /**
     * Construct a new "conflict" exception.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     */
    public ProprietorRequiredException(final String message) {
        super(message, Response.Status.CONFLICT);
    }

}
