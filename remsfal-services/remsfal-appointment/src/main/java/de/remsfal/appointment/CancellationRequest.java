package de.remsfal.appointment;

import jakarta.validation.constraints.Size;

/**
 * Represents a request to cancel an appointment, including an optional reason for cancellation.
 */

public class CancellationRequest {
    
    @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
    public String reason;
    
    public CancellationRequest() {
    }
    
    public CancellationRequest(String reason) {
        this.reason = reason;
    }
}
