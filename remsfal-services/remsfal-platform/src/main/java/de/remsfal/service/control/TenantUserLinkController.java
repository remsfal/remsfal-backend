package de.remsfal.service.control;

import de.remsfal.service.control.exception.AlreadyExistsException;
import de.remsfal.service.entity.dao.TenantRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.TenantEntity;
import de.remsfal.service.entity.dto.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Establishes and re-validates the optional link between a {@link TenantEntity} and a matching
 * {@link UserEntity}, keyed by email.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class TenantUserLinkController {

    @Inject
    Logger logger;

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

    /**
     * Links any pre-existing, unlinked tenant records across all projects whose email matches the
     * given user's email, to that user. Calls {@link #relinkByEmail} once per candidate tenant, so
     * all the same safety checks apply per tenant. A candidate tenant whose matching would conflict
     * with another tenant already linked to this user in the same project is skipped (logged) rather
     * than aborting the whole operation. Intended to be called once, synchronously, right after a
     * user account is created or reactivated.
     *
     * @param user the newly created/reactivated user
     * @return the tenants that ended up linked to {@code user} as a result of this call
     */
    public List<TenantEntity> linkTenantsForNewUser(final UserEntity user) {
        final String normalizedEmail = user.getEmail() == null ? null : user.getEmail().trim().toLowerCase();
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            return List.of();
        }
        final List<TenantEntity> linked = new ArrayList<>();
        for (final TenantEntity tenant : tenantRepository.findByEmail(normalizedEmail)) {
            try {
                relinkByEmail(tenant, normalizedEmail);
            } catch (AlreadyExistsException e) {
                logger.warnv("Skipping link of tenant {0} to user {1}: {2}",
                    tenant.getId(), user.getId(), e.getMessage());
                continue;
            }
            if (tenant.getUser() != null && tenant.getUser().getId().equals(user.getId())) {
                linked.add(tenant);
            }
        }
        return linked;
    }

}
