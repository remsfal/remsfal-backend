package de.remsfal.service.control;

import de.remsfal.core.model.AddressModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.RentalAgreementKeysModel;
import de.remsfal.core.model.project.RentalAgreementModel;
import de.remsfal.core.model.project.RentModel;
import de.remsfal.core.model.project.RentModel.BillingCycle;
import de.remsfal.core.model.project.TenantModel;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dao.RentalAgreementRepository;
import de.remsfal.service.entity.dao.TenantRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.AddressEntity;
import de.remsfal.service.entity.dto.ApartmentRentEntity;
import de.remsfal.service.entity.dto.BuildingRentEntity;
import de.remsfal.service.entity.dto.CommercialRentEntity;
import de.remsfal.service.entity.dto.PropertyRentEntity;
import de.remsfal.service.entity.dto.RentalAgreementEntity;
import de.remsfal.service.entity.dto.SiteRentEntity;
import de.remsfal.service.entity.dto.StorageRentEntity;
import de.remsfal.service.entity.dto.TenantEntity;
import de.remsfal.service.entity.dto.embeddable.RentalAgreementKeysEntity;
import de.remsfal.service.entity.dto.UserEntity;
import de.remsfal.service.entity.dto.superclass.RentEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.jboss.logging.Logger;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class RentalAgreementController {

    @Inject
    Logger logger;

    @Inject
    RentalAgreementRepository rentalAgreementRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    TenantRepository tenantRepository;

    public List<RentalAgreementEntity> getRentalAgreements(final UserModel tenant) {
        logger.infov("Retrieving all rental agreements (tenantId = {0})", tenant.getId());
        return rentalAgreementRepository.findRentalAgreementsByTenant(tenant.getId());
    }

    public RentalAgreementEntity getRentalAgreement(final UserModel tenant, final UUID agreementId) {
        logger.infov("Retrieving a rental agreement (tenantId = {0}, agreementId = {1})",
            tenant.getId(), agreementId);
        return rentalAgreementRepository.findRentalAgreementByTenant(tenant.getId(), agreementId)
            .orElseThrow(() -> new NotFoundException("Rental agreement not exist"));
    }

    public List<RentalAgreementEntity> getRentalAgreementsByProject(final UUID projectId) {
        logger.infov("Retrieving all rental agreements (projectId = {0})", projectId);
        return rentalAgreementRepository.findRentalAgreementByProject(projectId);
    }
    
    @Transactional
    public boolean deleteRentalAgreement(final UUID projectId, final UUID agreementId) {
        logger.infov("Deleting a Rental Agreement (projectId={0}, agreementId={1})",
            projectId, agreementId);
        return rentalAgreementRepository.removeRentalAgreementByIds(projectId, agreementId) > 0;
    }

    /**
     * Retrieves all rental agreements for a project grouped by tenant ID.
     * Each tenant ID maps to a list of rental agreements they are part of.
     *
     * @param projectId the project ID
     * @return map of tenant ID to their rental agreements
     */
    public Map<UUID, List<RentalAgreementEntity>> getRentalAgreementsByTenant(final UUID projectId) {
        logger.infov("Retrieving all rental agreements grouped by tenant (projectId = {0})", projectId);
        List<RentalAgreementEntity> agreements = rentalAgreementRepository.findRentalAgreementByProject(projectId);

        Map<UUID, List<RentalAgreementEntity>> agreementsByTenant = new HashMap<>();
        for (RentalAgreementEntity agreement : agreements) {
            if (agreement.getTenants() != null) {
                for (TenantModel tenant : agreement.getTenants()) {
                    agreementsByTenant
                        .computeIfAbsent(tenant.getId(), k -> new ArrayList<>())
                        .add(agreement);
                }
            }
        }

        return agreementsByTenant;
    }

    public RentalAgreementEntity getRentalAgreementByProject(final UUID projectId, final UUID agreementId) {
        logger.infov("Retrieving a rental agreement (projectId = {0}, agreementId = {1})", projectId, agreementId);
        return rentalAgreementRepository.findRentalAgreementByProject(projectId, agreementId)
            .orElseThrow(() -> new NotFoundException("Rental agreement not exist"));
    }

    @Transactional
    public RentalAgreementEntity createRentalAgreement(final UUID projectId, final RentalAgreementModel agreement) {
        logger.infov("Creating a rental agreement (project={0})", projectId);

        if (projectRepository.findById(projectId) == null) {
            throw new NotFoundException("Project not exist");
        }

        RentalAgreementEntity entity = new RentalAgreementEntity();
        entity.generateId();
        entity.setProjectId(projectId);
        entity.setStartOfRental(agreement.getStartOfRental());
        entity.setEndOfRental(agreement.getEndOfRental());

        // Process tenants
        entity.setTenants(processTenants(projectId, agreement.getTenants()));

        // Process keys
        entity.setKeys(processKeys(agreement.getKeys()));

        // Process rents
        processRents(entity, agreement);

        rentalAgreementRepository.persistAndFlush(entity);
        return entity;
    }

    @Transactional
    public RentalAgreementEntity updateRentalAgreement(final UUID projectId, final UUID agreementId,
        final RentalAgreementModel agreement) {
        logger.infov("Updating a rental agreement (projectId={0}, agreementId={1})", projectId, agreementId);
        final RentalAgreementEntity entity = rentalAgreementRepository
            .findRentalAgreementByProject(projectId, agreementId)
            .orElseThrow(() -> new NotFoundException("Rental agreement not exist"));

        if (agreement.getStartOfRental() != null) {
            entity.setStartOfRental(agreement.getStartOfRental());
        }
        if (agreement.getEndOfRental() != null) {
            entity.setEndOfRental(agreement.getEndOfRental());
        }

        // Update tenants (replace entire list)
        final List<? extends TenantModel> tenants = agreement.getTenants();
        if (tenants != null && !tenants.isEmpty()) {
            entity.getTenants().clear();
            List<TenantEntity> tenantEntities = processTenants(projectId, tenants);
            entity.getTenants().addAll(tenantEntities);
        }

        // Update keys (replace entire list, only if provided)
        if (agreement.getKeys() != null) {
            entity.setKeys(processKeys(agreement.getKeys()));
        }

        // Update rents (only replace if provided)
        processRents(entity, agreement);

        return rentalAgreementRepository.merge(entity);
    }

    @Transactional
    public TenantEntity addTenant(final UUID projectId, final UUID agreementId, final TenantModel tenantInput) {
        logger.infov("Adding a tenant to a rental agreement (projectId={0}, agreementId={1})",
            projectId, agreementId);
        final RentalAgreementEntity entity = rentalAgreementRepository
            .findRentalAgreementByProject(projectId, agreementId)
            .orElseThrow(() -> new NotFoundException("Rental agreement not exist"));

        final TenantEntity tenant = processTenants(projectId, List.of(tenantInput)).get(0);
        entity.addTenant(tenant);
        rentalAgreementRepository.merge(entity);
        return tenant;
    }

    @Transactional
    public void removeTenant(final UUID projectId, final UUID agreementId, final UUID tenantId) {
        logger.infov("Removing a tenant from a rental agreement (projectId={0}, agreementId={1}, tenantId={2})",
            projectId, agreementId, tenantId);
        final RentalAgreementEntity entity = rentalAgreementRepository
            .findRentalAgreementByProject(projectId, agreementId)
            .orElseThrow(() -> new NotFoundException("Rental agreement not exist"));

        entity.removeTenant(tenantId);
        rentalAgreementRepository.merge(entity);
    }

    /**
     * Process tenant models and create or reuse tenant entities.
     * If a tenant matches an existing tenant in the project (based on business equality),
     * the existing tenant is reused. If a tenant has an email, attempts to link to an existing user account.
     *
     * @param projectId the project ID
     * @param tenantsInput the tenant models from the request
     * @return list of tenant entities
     */
    private List<TenantEntity> processTenants(final UUID projectId, final List<? extends TenantModel> tenantsInput) {
        List<TenantEntity> tenantEntities = new ArrayList<>();

        for (TenantModel tenantInput : tenantsInput) {
            // Look up existing tenants in the project by first and last name
            List<TenantEntity> candidates = tenantRepository.findByNameInProject(
                projectId, tenantInput.getFirstName(), tenantInput.getLastName());

            // Try to find a matching tenant using business equality (ignoring ID when null)
            TenantEntity existingTenant = findMatchingTenantEntity(tenantInput, candidates);

            if (existingTenant != null) {
                // Reuse existing tenant from the project
                logger.infov("Reusing existing tenant {0} {1} (id={2}) from project",
                    existingTenant.getFirstName(), existingTenant.getLastName(), existingTenant.getId());
                tenantEntities.add(existingTenant);
            } else {
                // Create new tenant
                TenantEntity tenant = createNewTenant(projectId, tenantInput);
                tenantEntities.add(tenant);
            }
        }

        return tenantEntities;
    }

    /**
     * Creates a new tenant entity from the input model.
     *
     * @param projectId the project ID
     * @param tenantInput the tenant input model
     * @return new tenant entity
     */
    private TenantEntity createNewTenant(final UUID projectId, final TenantModel tenantInput) {
        TenantEntity tenant = new TenantEntity();
        tenant.generateId();
        tenant.setProjectId(projectId);

        // Set required fields
        tenant.setFirstName(tenantInput.getFirstName());
        tenant.setLastName(tenantInput.getLastName());

        // Optional: link to existing user by email
        if (tenantInput.getEmail() != null && !tenantInput.getEmail().isBlank()) {
            tenant.setEmail(tenantInput.getEmail());

            UserEntity user = userRepository.findByEmail(tenantInput.getEmail()).orElse(null);
            if (user != null) {
                tenant.setUser(user);
                logger.infov("Linking tenant {0} {1} to user {2}",
                    tenantInput.getFirstName(), tenantInput.getLastName(), user.getId());
            }
        }

        // Set optional phone numbers
        if (tenantInput.getMobilePhoneNumber() != null) {
            tenant.setMobilePhoneNumber(tenantInput.getMobilePhoneNumber());
        }
        if (tenantInput.getBusinessPhoneNumber() != null) {
            tenant.setBusinessPhoneNumber(tenantInput.getBusinessPhoneNumber());
        }
        if (tenantInput.getPrivatePhoneNumber() != null) {
            tenant.setPrivatePhoneNumber(tenantInput.getPrivatePhoneNumber());
        }

        // Set optional birth information
        if (tenantInput.getPlaceOfBirth() != null) {
            tenant.setPlaceOfBirth(tenantInput.getPlaceOfBirth());
        }
        if (tenantInput.getDateOfBirth() != null) {
            tenant.setDateOfBirth(tenantInput.getDateOfBirth());
        }

        // Handle address if provided
        if (tenantInput.getAddress() != null) {
            AddressModel addressModel = tenantInput.getAddress();
            AddressEntity address = new AddressEntity();
            address.generateId();
            address.setStreet(addressModel.getStreet());
            address.setCity(addressModel.getCity());
            address.setProvince(addressModel.getProvince());
            address.setZip(addressModel.getZip());
            address.setCountry(addressModel.getCountry());
            tenant.setAddress(address);
        }

        logger.infov("Creating new tenant {0} {1} in project",
            tenantInput.getFirstName(), tenantInput.getLastName());

        return tenant;
    }

    /**
     * Checks if two tenants match based on business equality (not ID).
     * Two tenants match if they have the same first name, last name, and:
     * - Same email (if at least one has an email)
     * - Same date of birth (if at least one has a date of birth)
     *
     * If either email or date of birth differs, the tenants are considered different.
     * First name and last name are required fields and cannot be null.
     *
     * @param input the tenant input from the POST request
     * @param existing the existing tenant entity
     * @return true if tenants match, false otherwise
     */
    private boolean tenantsMatch(final TenantModel input, final TenantEntity existing) {
        // First name and last name are required fields (validated at API level)
        // They must match (case-insensitive)
        if (input.getFirstName() == null || existing.getFirstName() == null) {
            return false;
        }
        if (!input.getFirstName().equalsIgnoreCase(existing.getFirstName())) {
            return false;
        }
        
        if (input.getLastName() == null || existing.getLastName() == null) {
            return false;
        }
        if (!input.getLastName().equalsIgnoreCase(existing.getLastName())) {
            return false;
        }

        // If at least one has an email, they must match (or both must be null/empty)
        String inputEmail = input.getEmail();
        String existingEmail = existing.getEmail();
        if (inputEmail != null || existingEmail != null) {
            // At least one has an email - they must match
            if (inputEmail == null || existingEmail == null) {
                // One has email, the other doesn't - not a match
                return false;
            }
            if (!inputEmail.equalsIgnoreCase(existingEmail)) {
                // Different emails - not a match
                return false;
            }
        }

        // If at least one has a date of birth, they must match
        if (input.getDateOfBirth() != null || existing.getDateOfBirth() != null) {
            // At least one has a date of birth - they must match
            if (input.getDateOfBirth() == null || existing.getDateOfBirth() == null) {
                // One has date of birth, the other doesn't - not a match
                return false;
            }
            if (!input.getDateOfBirth().equals(existing.getDateOfBirth())) {
                // Different dates of birth - not a match
                return false;
            }
        }

        return true;
    }

    /**
     * Finds a matching tenant from a list of candidate tenant entities using business equality.
     * This is the key method for tenant deduplication across the project.
     *
     * @param input the tenant input from the POST request
     * @param candidates list of candidate tenant entities from the database
     * @return matching tenant entity or null if no match found
     */
    private TenantEntity findMatchingTenantEntity(final TenantModel input, final List<TenantEntity> candidates) {
        for (TenantEntity candidate : candidates) {
            // Use business equality check (ignoring IDs)
            if (tenantsMatch(input, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Process key handover models into key handover entities.
     *
     * @param keysInput the key handover models from the request, may be null
     * @return list of key handover entities
     */
    private List<RentalAgreementKeysEntity> processKeys(final List<? extends RentalAgreementKeysModel> keysInput) {
        List<RentalAgreementKeysEntity> keyEntities = new ArrayList<>();
        if (keysInput == null) {
            return keyEntities;
        }
        for (RentalAgreementKeysModel keyInput : keysInput) {
            RentalAgreementKeysEntity key = new RentalAgreementKeysEntity();
            key.setAmountOfKeys(keyInput.getAmountOfKeys());
            key.setIssuedAt(keyInput.getIssuedAt());
            key.setReturnedAt(keyInput.getReturnedAt());
            key.setKeyDescription(keyInput.getKeyDescription());
            keyEntities.add(key);
        }
        return keyEntities;
    }

    /**
     * Process all rent types from the agreement model and update them on the entity.
     * PATCH-style behavior: only updates rent lists that are provided in the agreement.
     * If a rent list is provided, it replaces the existing list, but entries that match an
     * existing one (by unit and first payment date) are updated in place rather than recreated
     * (see {@link #reconcileRents}).
     *
     * @param entity the rental agreement entity
     * @param agreement the rental agreement model
     */
    private void processRents(final RentalAgreementEntity entity, final RentalAgreementModel agreement) {
        if (agreement.getPropertyRents() != null) {
            if (entity.getPropertyRents() == null) {
                entity.setPropertyRents(new ArrayList<>());
            }
            reconcileRents(entity.getPropertyRents(), agreement.getPropertyRents(),
                entity.getStartOfRental(), PropertyRentEntity::new, PropertyRentEntity::setPropertyId);
        }

        if (agreement.getSiteRents() != null) {
            if (entity.getSiteRents() == null) {
                entity.setSiteRents(new ArrayList<>());
            }
            reconcileRents(entity.getSiteRents(), agreement.getSiteRents(),
                entity.getStartOfRental(), SiteRentEntity::new, SiteRentEntity::setSiteId);
        }

        if (agreement.getBuildingRents() != null) {
            if (entity.getBuildingRents() == null) {
                entity.setBuildingRents(new ArrayList<>());
            }
            reconcileRents(entity.getBuildingRents(), agreement.getBuildingRents(),
                entity.getStartOfRental(), BuildingRentEntity::new, BuildingRentEntity::setBuildingId);
        }

        if (agreement.getApartmentRents() != null) {
            if (entity.getApartmentRents() == null) {
                entity.setApartmentRents(new ArrayList<>());
            }
            reconcileRents(entity.getApartmentRents(), agreement.getApartmentRents(),
                entity.getStartOfRental(), ApartmentRentEntity::new, ApartmentRentEntity::setApartmentId);
        }

        if (agreement.getStorageRents() != null) {
            if (entity.getStorageRents() == null) {
                entity.setStorageRents(new ArrayList<>());
            }
            reconcileRents(entity.getStorageRents(), agreement.getStorageRents(),
                entity.getStartOfRental(), StorageRentEntity::new, StorageRentEntity::setStorageId);
        }

        if (agreement.getCommercialRents() != null) {
            if (entity.getCommercialRents() == null) {
                entity.setCommercialRents(new ArrayList<>());
            }
            reconcileRents(entity.getCommercialRents(), agreement.getCommercialRents(),
                entity.getStartOfRental(), CommercialRentEntity::new, CommercialRentEntity::setCommercialId);
        }
    }

    /**
     * Reconciles the currently persisted rent entities of one type against the rents provided in
     * the request, instead of blindly deleting and recreating all of them on every update.
     *
     * <p>Rent entities carry an optimistic-locking {@code @Version} (inherited from
     * {@code MetaDataEntity}) and a natural, assigned composite id (unit id + first payment date) —
     * not a generated surrogate key. Recreating an entry that already exists in the database as a
     * brand-new Java object (with a {@code null} version) and merging it causes Hibernate to issue an
     * update against the existing row using that {@code null} version, which never matches the row's
     * real version and fails with {@link jakarta.persistence.OptimisticLockException}. This is why
     * adding a second unit used to fail: the first, already-persisted unit was rebuilt from scratch
     * alongside the new one.
     *
     * <p>Existing entries that still appear in {@code rentsInput} (matched by unit id + first payment
     * date) keep their original, managed instance — only their fields are refreshed — so Hibernate
     * treats them as an update of the same row. Entries no longer present are dropped so
     * {@code orphanRemoval} deletes them; entries with no match are created new.
     *
     * <p><b>Mutates {@code existingRents} in place</b> — it must never be replaced with a new List
     * instance via the entity's setter. The field holds a Hibernate-managed, {@code orphanRemoval}
     * collection once the owning entity is loaded/attached; swapping in a different List reference
     * makes Hibernate lose track of the dereferenced original and it fails the transaction with
     * {@code HibernateException: A collection with orphan deletion was no longer referenced by the
     * owning entity instance} at flush time.
     *
     * @param <T> the rent entity type
     * @param existingRents the currently persisted rent entities, mutated in place (must not be null)
     * @param rentsInput the rent models from the request
     * @param agreementStartOfRental fallback first payment date when a rent model doesn't specify one
     * @param factory creates a new, empty entity instance of type {@code T}
     * @param unitIdSetter assigns the unit id on a newly created entity
     */
    private <T extends RentEntity> void reconcileRents(final List<T> existingRents,
        final List<? extends RentModel> rentsInput, final LocalDate agreementStartOfRental,
        final Supplier<T> factory, final BiConsumer<T, UUID> unitIdSetter) {

        final Map<RentKey, T> existingByKey = new HashMap<>();
        for (T existing : existingRents) {
            existingByKey.put(new RentKey(existing.getUnitId(), existing.getFirstPaymentDate()), existing);
        }

        final List<T> reconciled = new ArrayList<>();
        for (RentModel rentInput : rentsInput) {
            final LocalDate firstPaymentDate = rentInput.getFirstPaymentDate() != null
                ? rentInput.getFirstPaymentDate() : agreementStartOfRental;
            final T existing = existingByKey.remove(new RentKey(rentInput.getUnitId(), firstPaymentDate));

            final T rent;
            if (existing != null) {
                rent = existing;
            } else {
                rent = factory.get();
                unitIdSetter.accept(rent, rentInput.getUnitId());
            }
            mapRentFields(rentInput, rent, agreementStartOfRental);
            reconciled.add(rent);
        }

        existingRents.clear();
        existingRents.addAll(reconciled);
    }

    /**
     * Identifies a rent entity by its natural key (unit id + first payment date), used to match
     * incoming rent models against already-persisted rent entities in {@link #reconcileRents}.
     */
    private record RentKey(UUID unitId, LocalDate firstPaymentDate) {
    }

    /**
     * Maps common rent fields from a rent model to a rent entity.
     *
     * @param <T> the rent entity type
     * @param rentInput the rent model
     * @param rentEntity the rent entity
     */
    private <T extends RentEntity> void mapRentFields(final RentModel rentInput,
        T rentEntity, LocalDate agreementStartOfRental) {
        if (rentInput.getFirstPaymentDate() == null) {
            rentEntity.setFirstPaymentDate(agreementStartOfRental);
        } else {
            rentEntity.setFirstPaymentDate(rentInput.getFirstPaymentDate());
        }
        if (rentInput.getLastPaymentDate() != null) {
            rentEntity.setLastPaymentDate(rentInput.getLastPaymentDate());
        }
        if (rentInput.getBillingCycle() == null) {
            rentEntity.setBillingCycle(BillingCycle.MONTHLY);
        } else {
            rentEntity.setBillingCycle(rentInput.getBillingCycle());
        }
        if (rentInput.getBasicRent() != null) {
            rentEntity.setBasicRent(rentInput.getBasicRent());
        }
        if (rentInput.getOperatingCostsPrepayment() != null) {
            rentEntity.setOperatingCostsPrepayment(rentInput.getOperatingCostsPrepayment());
        }
        if (rentInput.getHeatingCostsPrepayment() != null) {
            rentEntity.setHeatingCostsPrepayment(rentInput.getHeatingCostsPrepayment());
        }
    }

}
