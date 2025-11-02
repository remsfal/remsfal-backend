package de.remsfal.service.control.event;

import java.util.Date;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class AuthenticationEvent {

    private final Date authenticatedAt = new Date();

    private final String googleId;
    
    private final String email;

    public AuthenticationEvent(final String googleId, final String email) {
        this.googleId = googleId;
        this.email = email;
    }

    public Date getAuthenticatedAt() {
        return authenticatedAt;
    }

    public String getGoogleId() {
        return googleId;
    }

    public String getEmail() {
        return email;
    }

}
