package de.remsfal.service.boundary.authentication;

import java.util.Date;

import de.remsfal.core.model.UserModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class AuthenticationEvent {

    private final Date authenticatedAt = new Date();

    private final UserModel user;

    public AuthenticationEvent(final UserModel user) {
        this.user = user;
    }

    public Date getAuthenticatedAt() {
        return authenticatedAt;
    }

    public UserModel getUser() {
        return user;
    }


}
