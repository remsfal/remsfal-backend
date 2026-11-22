package de.remsfal.service.control;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.arc.Arc;

/**
 * Resolves the user who triggers a change, for the {@code actingUserId} of the extension point events.
 */
@ApplicationScoped
public class ActingUserController {

    @Inject
    Instance<JsonWebToken> jwt;

    /**
     * @return the id of the authenticated user of the current request, null outside of a request or without
     *         authentication (e.g. Kafka consumers, schedulers)
     */
    public UUID getId() {
        if (!Arc.container().requestContext().isActive() || !jwt.isResolvable()) {
            return null;
        }
        try {
            final String subject = jwt.get().getSubject();
            return subject != null ? UUID.fromString(subject) : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

}
