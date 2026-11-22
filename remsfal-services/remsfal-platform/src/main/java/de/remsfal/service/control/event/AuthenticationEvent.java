package de.remsfal.service.control.event;

import java.time.LocalDateTime;
import java.util.UUID;

import de.remsfal.service.entity.dto.UserIdentityEntity.IdentityProvider;

/**
 * Fired asynchronously when a user has authenticated with an external identity.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public record AuthenticationEvent(UUID userId, IdentityProvider provider, String subject, String email,
    LocalDateTime authenticatedAt) {

    /**
     * Creates an event for an authentication that happens now.
     */
    public static AuthenticationEvent of(final UUID userId, final IdentityProvider provider, final String subject,
        final String email) {
        return new AuthenticationEvent(userId, provider, subject, email, LocalDateTime.now());
    }

}
