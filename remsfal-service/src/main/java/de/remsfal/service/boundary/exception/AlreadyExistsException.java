package de.remsfal.service.boundary.exception;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class AlreadyExistsException extends ClientErrorException {

    private static final long serialVersionUID = 1L;

    /**
     * Construct a new "conflict" exception.
     */
    public AlreadyExistsException() {
        super(Response.Status.CONFLICT);
    }

    /**
     * Construct a new "conflict" exception.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     */
    public AlreadyExistsException(String message) {
        super(message, Response.Status.CONFLICT);
    }

    /**
     * Construct a new "conflict" exception.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the underlying cause of the exception.
     */
    public AlreadyExistsException(String message, Throwable cause) {
        super(message, Response.Status.CONFLICT, cause);
    }

}
