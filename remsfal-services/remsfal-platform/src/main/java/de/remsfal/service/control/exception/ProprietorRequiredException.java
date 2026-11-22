package de.remsfal.service.control.exception;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

/**
 * Thrown if an operation would leave a project without an active proprietor. Every project must always have at least
 * one active proprietor, either an active user or an organization with an active OWNER or MANAGER.
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
