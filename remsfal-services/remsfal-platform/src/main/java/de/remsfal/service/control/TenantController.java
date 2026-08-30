package de.remsfal.service.control;

import de.remsfal.core.json.project.TenantJson;
import de.remsfal.core.model.project.TenantModel;
import de.remsfal.service.control.exception.AlreadyExistsException;
import de.remsfal.service.entity.dao.RentalAgreementRepository;
import de.remsfal.service.entity.dao.TenantRepository;
import de.remsfal.service.entity.dto.RentalAgreementEntity;
import de.remsfal.service.entity.dto.TenantEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequestScoped
public class TenantController {
    @Inject
    Logger logger;

    @Inject
    TenantRepository tenantRepository;

    @Inject
    RentalAgreementRepository rentalAgreementRepository;

    @Inject
    TenantUserLinkController tenantUserLinker;

    @Inject
    AddressController addressController;

    public List<TenantModel> getTenants(final UUID projectId) {
        logger.infov("Retrieving tenants for project {0}", projectId);
        List<TenantEntity> entities = tenantRepository.findTenantsByProjectId(projectId);
        return entities.stream()
                .map(TenantModel.class::cast)
                .collect(Collectors.toList());
    }

    public TenantModel getTenant(final UUID projectId, final UUID tenantId) {
        logger.infov("Retrieving a tenant (projectId={0}, tenantId={1})",
            projectId, tenantId);
        return tenantRepository.findTenantByProjectId(projectId, tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));
    }

    /**
     * Resolves a tenant for a rental agreement: reuses an existing tenant in the project when the
     * input's email already belongs to a same-named tenant, signals a conflict when the email
     * belongs to a differently-named tenant, or creates a brand-new tenant otherwise. Without an
     * email, no deduplication is possible and a new tenant is always created.
     *
     * @param projectId the project ID
     * @param tenantInput the tenant input model
     * @return the resolved (reused or newly created) tenant entity
     * @throws AlreadyExistsException if the email already belongs to a differently-named tenant
     *         in the project
     */
    @Transactional
    public TenantEntity resolveOrCreateTenant(final UUID projectId, final TenantModel tenantInput) {
        if (tenantInput.getEmail() != null && !tenantInput.getEmail().isBlank()) {
            final String normalizedEmail = tenantInput.getEmail().trim().toLowerCase();
            final Optional<TenantEntity> existing = tenantRepository
                .findByEmailAndProjectId(normalizedEmail, projectId).stream().findFirst();

            if (existing.isPresent()) {
                final TenantEntity candidate = existing.get();
                if (namesMatch(tenantInput.getFirstName(), tenantInput.getLastName(), candidate)) {
                    logger.infov("Reusing existing tenant {0} in project {1}", candidate.getId(), projectId);
                    return candidate;
                }
                throw new AlreadyExistsException(
                    "Email " + normalizedEmail + " is already used by another tenant in this project");
            }

            final TenantEntity tenant = mergeTenantFields(tenantInput, new TenantEntity());
            tenant.generateId();
            tenant.setProjectId(projectId);
            tenant.setEmail(normalizedEmail);
            tenantUserLinker.relinkByEmail(tenant, normalizedEmail);
            logger.infov("Creating new tenant {0} {1} in project {2}",
                tenantInput.getFirstName(), tenantInput.getLastName(), projectId);
            return tenant;
        }

        final TenantEntity tenant = mergeTenantFields(tenantInput, new TenantEntity());
        tenant.generateId();
        tenant.setProjectId(projectId);
        logger.infov("Creating new tenant {0} {1} in project {2}",
            tenantInput.getFirstName(), tenantInput.getLastName(), projectId);
        return tenant;
    }

    /**
     * Resolves a list of tenants for a rental agreement.
     *
     * @param projectId the project ID
     * @param tenantsInput the tenant input models
     * @return list of resolved tenant entities
     * @see #resolveOrCreateTenant(UUID, TenantModel)
     */
    public List<TenantEntity> resolveOrCreateTenants(final UUID projectId,
            final List<? extends TenantModel> tenantsInput) {
        return tenantsInput.stream()
            .map(tenantInput -> resolveOrCreateTenant(projectId, tenantInput))
            .collect(Collectors.toList());
    }

    @Transactional
    public TenantModel updateTenant(final UUID projectId, final UUID tenantId, final TenantJson tenantJson) {
        logger.infov("Updating tenant (projectId={0}, tenantId={1})", projectId, tenantId);

        final TenantEntity entity = tenantRepository.findTenantByProjectId(projectId, tenantId)
            .orElseThrow(() -> new NotFoundException("Tenant not found"));

        if (tenantJson.getEmail() != null) {
            final String normalizedEmail = tenantJson.getEmail().trim().toLowerCase();
            if (!normalizedEmail.equals(entity.getEmail())) {
                final String postUpdateFirstName = tenantJson.getFirstName() != null
                    ? tenantJson.getFirstName() : entity.getFirstName();
                final String postUpdateLastName = tenantJson.getLastName() != null
                    ? tenantJson.getLastName() : entity.getLastName();

                tenantRepository.findByEmailAndProjectId(normalizedEmail, entity.getProjectId()).stream()
                    .filter(other -> !other.getId().equals(entity.getId()))
                    .findAny()
                    .ifPresent(other -> {
                        if (namesMatch(postUpdateFirstName, postUpdateLastName, other)) {
                            mergeTenants(entity, other);
                        } else {
                            throw new AlreadyExistsException(
                                "Email " + normalizedEmail + " is already used by another tenant in this project");
                        }
                    });

                entity.setEmail(normalizedEmail);
                tenantUserLinker.relinkByEmail(entity, normalizedEmail);
            }
        }

        mergeTenantFields(tenantJson, entity);

        tenantRepository.persistAndFlush(entity);
        tenantRepository.getEntityManager().refresh(entity);

        return getTenant(projectId, tenantId);
    }

    /**
     * Checks whether the given first/last name match another tenant's name, case-insensitively.
     *
     * @param firstName the first name to compare
     * @param lastName the last name to compare
     * @param other the tenant entity to compare against
     * @return true if both names match
     */
    private boolean namesMatch(final String firstName, final String lastName, final TenantEntity other) {
        return firstName != null && lastName != null
            && firstName.equalsIgnoreCase(other.getFirstName())
            && lastName.equalsIgnoreCase(other.getLastName());
    }

    /**
     * Merges {@code loser} into {@code survivor}: every rental agreement linked to {@code loser} is
     * relinked to {@code survivor}, then {@code loser} is deleted. Must be called before
     * {@link TenantUserLinkController#relinkByEmail}, since that method would otherwise see
     * {@code loser} still holding a conflicting user link.
     *
     * @param survivor the tenant entity to keep
     * @param loser the tenant entity to merge away and delete
     */
    private void mergeTenants(final TenantEntity survivor, final TenantEntity loser) {
        logger.infov("Merging tenant {0} into {1} (email conflict, same name)", loser.getId(), survivor.getId());
        final List<RentalAgreementEntity> agreements =
            rentalAgreementRepository.findRentalAgreementsByTenantId(loser.getId());
        for (RentalAgreementEntity agreement : agreements) {
            agreement.removeTenant(loser.getId());
            agreement.addTenant(survivor);
            rentalAgreementRepository.merge(agreement);
        }
        tenantRepository.delete(loser);
        // Flush the deletion now: Hibernate's default flush ordering runs updates before deletes,
        // so without an explicit flush here, the survivor's email update (applied right after this
        // call returns) would race the loser's deletion and trip the per-project unique email index.
        tenantRepository.getEntityManager().flush();
    }

    /**
     * Pure field-merge: copies every non-null field from {@code model} onto {@code entity} and
     * returns it. No repository access, no exceptions — reused both to populate a brand-new tenant
     * entity and to apply a PATCH onto an existing one. {@code email} is intentionally excluded,
     * since setting it requires dedup/merge/relink side effects that don't belong in a pure
     * function; callers handle it separately.
     *
     * @param model the tenant input model
     * @param entity the entity to merge fields onto (may be new or already persisted)
     * @return the mutated entity
     */
    private TenantEntity mergeTenantFields(final TenantModel model, final TenantEntity entity) {
        if (model.getFirstName() != null) {
            entity.setFirstName(model.getFirstName());
        }
        if (model.getLastName() != null) {
            entity.setLastName(model.getLastName());
        }
        if (model.getMobilePhoneNumber() != null) {
            entity.setMobilePhoneNumber(model.getMobilePhoneNumber());
        }
        if (model.getBusinessPhoneNumber() != null) {
            entity.setBusinessPhoneNumber(model.getBusinessPhoneNumber());
        }
        if (model.getPrivatePhoneNumber() != null) {
            entity.setPrivatePhoneNumber(model.getPrivatePhoneNumber());
        }
        if (model.getPlaceOfBirth() != null) {
            entity.setPlaceOfBirth(model.getPlaceOfBirth());
        }
        if (model.getDateOfBirth() != null) {
            entity.setDateOfBirth(model.getDateOfBirth());
        }
        if (model.getAddress() != null) {
            entity.setAddress(addressController.updateAddress(model.getAddress(), entity.getAddress()));
        }
        return entity;
    }
}
