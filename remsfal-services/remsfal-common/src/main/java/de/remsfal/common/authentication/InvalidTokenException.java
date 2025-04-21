package de.remsfal.common.authentication;

import jakarta.ws.rs.NotAuthorizedException;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class InvalidTokenException extends NotAuthorizedException {

    private static final long serialVersionUID = 1L;

    /**
     * Construct a new "unauthorized" exception.
     */
    public InvalidTokenException() {
        this("Invalid token");
    }

    /**
     * Construct a new "unauthorized" exception.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     */
    public InvalidTokenException(String message) {
        super(message, RemsfalSecurityContext.BEARER);
    }

    /**
     * Construct a new "unauthorized" exception.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the underlying cause of the exception.
     */
    public InvalidTokenException(Throwable cause) {
        super("Invalid token", cause, RemsfalSecurityContext.BEARER);
    }

}
