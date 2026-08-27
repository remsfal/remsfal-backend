package de.remsfal.service.control;

import de.remsfal.service.control.exception.AlreadyExistsException;
import de.remsfal.service.entity.dao.ContractorRepository;
import de.remsfal.service.entity.dao.OrganizationRepository;
import de.remsfal.service.entity.dto.ContractorEntity;
import de.remsfal.service.entity.dto.OrganizationEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Establishes and re-validates the optional link between a {@link ContractorEntity} and a matching
 * {@link OrganizationEntity}, keyed by email.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class ContractorOrganizationLinkController {

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
                .anyMatch(c -> c.getProjectId().equals(contractor.getProjectId()) && !c.getId().equals(contractor.getId()));
            if (alreadyLinkedElsewhere) {
                throw new AlreadyExistsException(
                    "Organization " + organization.getId() + " is already linked to another contractor "
                        + "in this project");
            }
            contractor.setOrganization(organization);
        });
    }

}
