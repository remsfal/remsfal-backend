package de.remsfal.service.control.exception;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

/**
 * Rejects a request because the client exceeded a rate limit.
 */
public class TooManyRequestsException extends ClientErrorException {

    private static final long serialVersionUID = 1L;

    /**
     * Construct a new "too many requests" exception.
     *
     * @param message    the detail message
     * @param retryAfter seconds after which the client may retry
     */
    public TooManyRequestsException(final String message, final long retryAfter) {
        super(message, Response.status(Response.Status.TOO_MANY_REQUESTS)
            .header("Retry-After", retryAfter)
            .build());
    }

}
