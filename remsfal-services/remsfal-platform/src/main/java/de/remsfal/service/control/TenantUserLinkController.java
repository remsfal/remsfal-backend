package de.remsfal.service.control;

import de.remsfal.service.control.exception.AlreadyExistsException;
import de.remsfal.service.entity.dao.TenantRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.TenantEntity;
import de.remsfal.service.entity.dto.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Establishes and re-validates the optional link between a {@link TenantEntity} and a matching
 * {@link UserEntity}, keyed by email.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class TenantUserLinkController {

    @Inject
    UserRepository userRepository;

    @Inject
    TenantRepository tenantRepository;

    /**
     * Re-evaluates a tenant's link to a user account for a given (already-normalized, lowercase) email.
     * Clears a stale link if the linked user's email no longer matches, then attempts to (re-)link to a
     * user matching the new email — but only if that user is not already linked to a different tenant in
     * the same project.
     *
     * @param tenant the tenant entity to update (its {@code user} association may be changed)
     * @param normalizedEmail the tenant's current, already-normalized email, or {@code null}/blank
     * @throws AlreadyExistsException if the matching user is already linked to another tenant in the
     *         same project
     */
    public void relinkByEmail(final TenantEntity tenant, final String normalizedEmail) {
        final UserEntity currentUser = tenant.getUser();
        if (currentUser != null
            && (normalizedEmail == null || !normalizedEmail.equalsIgnoreCase(currentUser.getEmail()))) {
            tenant.setUser(null);
        }

        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            return;
        }

        userRepository.findByEmail(normalizedEmail).ifPresent(user -> {
            final boolean alreadyLinkedElsewhere = tenantRepository.findByUserId(user.getId()).stream()
                .anyMatch(t -> t.getProjectId().equals(tenant.getProjectId()) && !t.getId().equals(tenant.getId()));
            if (alreadyLinkedElsewhere) {
                throw new AlreadyExistsException(
                    "User " + user.getId() + " is already linked to another tenant in this project");
            }
            tenant.setUser(user);
        });
    }

}
