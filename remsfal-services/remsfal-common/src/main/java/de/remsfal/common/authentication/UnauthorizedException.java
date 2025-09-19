package de.remsfal.common.authentication;

import jakarta.ws.rs.NotAuthorizedException;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class UnauthorizedException extends NotAuthorizedException {

    private static final long serialVersionUID = 1L;
    private static final String BEARER = "Bearer";

    /**
     * Construct a new "unauthorized" exception.
     */
    public UnauthorizedException() {
        super(BEARER);
    }

    /**
     * Construct a new "unauthorized" exception.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     */
    public UnauthorizedException(String message) {
        super(message, BEARER);
    }

    /**
     * Construct a new "unauthorized" exception.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the underlying cause of the exception.
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause, BEARER);
    }

}
