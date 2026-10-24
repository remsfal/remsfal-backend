package de.remsfal.service.control.event;

import java.time.LocalDateTime;
import java.util.UUID;

import de.remsfal.service.entity.dto.UserIdentityEntity.IdentityProvider;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class AuthenticationEvent {

    private final LocalDateTime authenticatedAt = LocalDateTime.now();

    private final UUID userId;

    private final IdentityProvider provider;

    private final String subject;

    private final String email;

    public AuthenticationEvent(final UUID userId, final IdentityProvider provider, final String subject,
        final String email) {
        this.userId = userId;
        this.provider = provider;
        this.subject = subject;
        this.email = email;
    }

    public LocalDateTime getAuthenticatedAt() {
        return authenticatedAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public IdentityProvider getProvider() {
        return provider;
    }

    public String getSubject() {
        return subject;
    }

    public String getEmail() {
        return email;
    }

}
