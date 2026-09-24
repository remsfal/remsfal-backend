package de.remsfal.service.control;

import de.remsfal.service.control.exception.AlreadyExistsException;
import de.remsfal.service.entity.dao.ContractorRepository;
import de.remsfal.service.entity.dao.OrganizationRepository;
import de.remsfal.service.entity.dto.ContractorEntity;
import de.remsfal.service.entity.dto.OrganizationEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Establishes and re-validates the optional link between a {@link ContractorEntity} and a matching
 * {@link OrganizationEntity}, keyed by email.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class ContractorOrganizationLinkController {

    @Inject
    Logger logger;

    @Inject
    OrganizationRepository organizationRepository;

    @Inject
    ContractorRepository contractorRepository;

    /**
     * Re-evaluates a contractor's link to an organization for a given (already-normalized, lowercase)
     * email. Clears a stale link if the linked organization's email no longer matches, then attempts
     * to (re-)link to an organization matching the new email — but only if that organization is not
     * already linked to a different contractor in the same project.
     *
     * @param contractor the contractor entity to update (its {@code organization} association may be changed)
     * @param normalizedEmail the contractor's current, already-normalized email, or {@code null}/blank
     * @throws AlreadyExistsException if the matching organization is already linked to another
     *         contractor in the same project
     */
    public void relinkByEmail(final ContractorEntity contractor, final String normalizedEmail) {
        final OrganizationEntity currentOrganization = contractor.getOrganization();
        if (currentOrganization != null
            && (normalizedEmail == null || !normalizedEmail.equalsIgnoreCase(currentOrganization.getEmail()))) {
            contractor.setOrganization(null);
        }

        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            return;
        }

        organizationRepository.findByEmail(normalizedEmail).ifPresent(organization -> {
            final boolean alreadyLinkedElsewhere = contractorRepository.findByOrganizationId(organization.getId())
                .stream()
                .anyMatch(c -> c.getProjectId().equals(contractor.getProjectId())
                    && !c.getId().equals(contractor.getId()));
            if (alreadyLinkedElsewhere) {
                throw new AlreadyExistsException(
                    "Organization " + organization.getId() + " is already linked to another contractor "
                        + "in this project");
            }
            contractor.setOrganization(organization);
        });
    }

    /**
     * Links any pre-existing, unlinked contractor records across all projects whose email matches
     * the given organization's email, to that organization. Calls {@link #relinkByEmail} once per
     * candidate contractor, so all the same safety checks apply per contractor. A candidate
     * contractor whose matching would conflict with another contractor already linked to this
     * organization in the same project is skipped (logged) rather than aborting the whole operation.
     * Intended to be called once, synchronously, right after an organization is created.
     *
     * @param organization the newly created organization
     * @return the contractors that ended up linked to {@code organization} as a result of this call
     */
    public List<ContractorEntity> linkContractorsForNewOrganization(final OrganizationEntity organization) {
        final String normalizedEmail = organization.getEmail() == null
            ? null : organization.getEmail().trim().toLowerCase();
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            return List.of();
        }
        final List<ContractorEntity> linked = new ArrayList<>();
        for (final ContractorEntity contractor : contractorRepository.findByEmail(normalizedEmail)) {
            try {
                relinkByEmail(contractor, normalizedEmail);
            } catch (AlreadyExistsException e) {
                logger.warnv("Skipping link of contractor {0} to organization {1}: {2}",
                    contractor.getId(), organization.getId(), e.getMessage());
                continue;
            }
            if (contractor.getOrganization() != null
                && contractor.getOrganization().getId().equals(organization.getId())) {
                linked.add(contractor);
            }
        }
        return linked;
    }

}
