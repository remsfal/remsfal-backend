package de.remsfal.service.control;

import de.remsfal.core.model.AddressModel;
import de.remsfal.core.model.RentalUnitModel.UnitType;
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
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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

    @Inject
    PropertyController propertyController;

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

    public List<RentalAgreementEntity> getRentalAgreementsByProject(final UUID projectId,
            final UnitType rentalUnitType, final UUID rentalUnitId) {
        logger.infov("Retrieving all rental agreements (projectId = {0}, rentalUnitType = {1}, rentalUnitId = {2})",
            projectId, rentalUnitType, rentalUnitId);
        return rentalAgreementRepository.findRentalAgreementByProject(projectId, rentalUnitType, rentalUnitId);
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

        validateLeafRentalUnits(projectId, agreement);

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

    /**
     * Validates that every rent in the agreement references a leaf rental unit (i.e. a unit
     * without sub-units) — rental agreements may only be concluded for leaf units.
     */
    private void validateLeafRentalUnits(final UUID projectId, final RentalAgreementModel agreement) {
        validateLeafRentalUnits(projectId, UnitType.PROPERTY, agreement.getPropertyRents());
        validateLeafRentalUnits(projectId, UnitType.SITE, agreement.getSiteRents());
        validateLeafRentalUnits(projectId, UnitType.BUILDING, agreement.getBuildingRents());
        validateLeafRentalUnits(projectId, UnitType.APARTMENT, agreement.getApartmentRents());
        validateLeafRentalUnits(projectId, UnitType.STORAGE, agreement.getStorageRents());
        validateLeafRentalUnits(projectId, UnitType.COMMERCIAL, agreement.getCommercialRents());
    }

    private void validateLeafRentalUnits(final UUID projectId, final UnitType type,
            final List<? extends RentModel> rents) {
        if (rents == null) {
            return;
        }
        for (RentModel rent : rents) {
            if (!propertyController.isLeafRentalUnit(projectId, type, rent.getRentalUnitId())) {
                throw new BadRequestException("Rents may only be created for leaf rental units; "
                    + type + " " + rent.getRentalUnitId() + " has child units");
            }
        }
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
     * Adds a new rent for a rental unit of a rental agreement. If a rent is already active for that
     * unit (i.e. its {@code lastPaymentDate} is not set), it is ended by setting its
     * {@code lastPaymentDate} to one day before the new rent's {@code firstPaymentDate}. If
     * {@code billingCycle} is not set on the input, it is inherited from that previous rent, or
     * defaults to {@link BillingCycle#MONTHLY} if there is no previous rent.
     *
     * @param projectId the project ID
     * @param agreementId the rental agreement ID
     * @param rentalUnitType the type of the rental unit
     * @param rentalUnitId the ID of the rental unit
     * @param rentInput the rent information; {@code firstPaymentDate} is required
     * @return the updated rental agreement entity
     */
    @Transactional
    public RentalAgreementEntity addRent(final UUID projectId, final UUID agreementId,
            final UnitType rentalUnitType, final UUID rentalUnitId, final RentModel rentInput) {
        logger.infov("Adding a rent to a rental agreement (projectId={0}, agreementId={1}, "
            + "rentalUnitType={2}, rentalUnitId={3})", projectId, agreementId, rentalUnitType, rentalUnitId);
        final RentalAgreementEntity entity = rentalAgreementRepository
            .findRentalAgreementByProject(projectId, agreementId)
            .orElseThrow(() -> new NotFoundException("Rental agreement not exist"));

        if (!propertyController.isLeafRentalUnit(projectId, rentalUnitType, rentalUnitId)) {
            throw new BadRequestException("Rents may only be created for leaf rental units; "
                + rentalUnitType + " " + rentalUnitId + " has child units");
        }
        if (rentInput.getFirstPaymentDate() == null) {
            throw new BadRequestException("First payment date is required");
        }
        if (entity.getStartOfRental() != null
                && rentInput.getFirstPaymentDate().isBefore(entity.getStartOfRental())) {
            throw new BadRequestException("First payment date must not be before start of rental");
        }
        if (entity.getEndOfRental() != null) {
            if (rentInput.getFirstPaymentDate().isAfter(entity.getEndOfRental())) {
                throw new BadRequestException("First payment date must not be after end of rental");
            }
            if (rentInput.getLastPaymentDate() != null
                    && rentInput.getLastPaymentDate().isAfter(entity.getEndOfRental())) {
                throw new BadRequestException("Last payment date must not be after end of rental");
            }
        }

        switch (rentalUnitType) {
            case PROPERTY -> addRentToList(getOrInitRents(entity.getPropertyRents(), entity::setPropertyRents),
                rentalUnitId, rentInput, PropertyRentEntity::new, PropertyRentEntity::setPropertyId);
            case SITE -> addRentToList(getOrInitRents(entity.getSiteRents(), entity::setSiteRents),
                rentalUnitId, rentInput, SiteRentEntity::new, SiteRentEntity::setSiteId);
            case BUILDING -> addRentToList(getOrInitRents(entity.getBuildingRents(), entity::setBuildingRents),
                rentalUnitId, rentInput, BuildingRentEntity::new, BuildingRentEntity::setBuildingId);
            case APARTMENT -> addRentToList(getOrInitRents(entity.getApartmentRents(), entity::setApartmentRents),
                rentalUnitId, rentInput, ApartmentRentEntity::new, ApartmentRentEntity::setApartmentId);
            case STORAGE -> addRentToList(getOrInitRents(entity.getStorageRents(), entity::setStorageRents),
                rentalUnitId, rentInput, StorageRentEntity::new, StorageRentEntity::setStorageId);
            case COMMERCIAL -> addRentToList(getOrInitRents(entity.getCommercialRents(), entity::setCommercialRents),
                rentalUnitId, rentInput, CommercialRentEntity::new, CommercialRentEntity::setCommercialId);
        }

        return rentalAgreementRepository.merge(entity);
    }

    /**
     * Returns the given rent list, or initializes and sets a new empty list on the entity if it is
     * currently {@code null}.
     */
    private <T extends RentEntity> List<T> getOrInitRents(final List<T> current, final Consumer<List<T>> setter) {
        if (current != null) {
            return current;
        }
        final List<T> list = new ArrayList<>();
        setter.accept(list);
        return list;
    }

    /**
     * Ends the currently active rent of the given unit (if any) and appends a new rent to
     * {@code rents}. Mutates {@code rents} in place, following the same rationale as
     * {@link #reconcileRents}: the list must stay the same, Hibernate-managed instance.
     */
    private <T extends RentEntity> T addRentToList(final List<T> rents, final UUID rentalUnitId,
            final RentModel rentInput, final Supplier<T> factory, final BiConsumer<T, UUID> unitIdSetter) {

        final T previousRent = rents.stream()
            .filter(r -> Objects.equals(r.getRentalUnitId(), rentalUnitId))
            .filter(r -> r.getLastPaymentDate() == null)
            .findFirst()
            .orElse(null);

        if (previousRent != null) {
            previousRent.setLastPaymentDate(rentInput.getFirstPaymentDate().minusDays(1));
        }

        final BillingCycle billingCycle;
        if (rentInput.getBillingCycle() != null) {
            billingCycle = rentInput.getBillingCycle();
        } else if (previousRent != null) {
            billingCycle = previousRent.getBillingCycle();
        } else {
            billingCycle = BillingCycle.MONTHLY;
        }

        final T rent = factory.get();
        unitIdSetter.accept(rent, rentalUnitId);
        rent.setFirstPaymentDate(rentInput.getFirstPaymentDate());
        rent.setLastPaymentDate(rentInput.getLastPaymentDate());
        rent.setBillingCycle(billingCycle);
        if (rentInput.getBasicRent() != null) {
            rent.setBasicRent(rentInput.getBasicRent());
        }
        if (rentInput.getOperatingCostsPrepayment() != null) {
            rent.setOperatingCostsPrepayment(rentInput.getOperatingCostsPrepayment());
        }
        if (rentInput.getHeatingCostsPrepayment() != null) {
            rent.setHeatingCostsPrepayment(rentInput.getHeatingCostsPrepayment());
        }
        rents.add(rent);
        return rent;
    }

    /**
     * Deletes all rents (active and historic) of a rental unit from a rental agreement.
     *
     * @param projectId the project ID
     * @param agreementId the rental agreement ID
     * @param rentalUnitType the type of the rental unit
     * @param rentalUnitId the ID of the rental unit
     */
    @Transactional
    public void deleteRents(final UUID projectId, final UUID agreementId, final UnitType rentalUnitType,
            final UUID rentalUnitId) {
        logger.infov("Deleting rents of a rental unit from a rental agreement (projectId={0}, agreementId={1}, "
            + "rentalUnitType={2}, rentalUnitId={3})", projectId, agreementId, rentalUnitType, rentalUnitId);
        final RentalAgreementEntity entity = rentalAgreementRepository
            .findRentalAgreementByProject(projectId, agreementId)
            .orElseThrow(() -> new NotFoundException("Rental agreement not exist"));

        switch (rentalUnitType) {
            case PROPERTY -> removeRentsForUnit(entity.getPropertyRents(), rentalUnitId);
            case SITE -> removeRentsForUnit(entity.getSiteRents(), rentalUnitId);
            case BUILDING -> removeRentsForUnit(entity.getBuildingRents(), rentalUnitId);
            case APARTMENT -> removeRentsForUnit(entity.getApartmentRents(), rentalUnitId);
            case STORAGE -> removeRentsForUnit(entity.getStorageRents(), rentalUnitId);
            case COMMERCIAL -> removeRentsForUnit(entity.getCommercialRents(), rentalUnitId);
        }

        rentalAgreementRepository.merge(entity);
    }

    private void removeRentsForUnit(final List<? extends RentEntity> rents, final UUID rentalUnitId) {
        if (rents != null) {
            rents.removeIf(r -> Objects.equals(r.getRentalUnitId(), rentalUnitId));
        }
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
            existingByKey.put(new RentKey(existing.getRentalUnitId(), existing.getFirstPaymentDate()), existing);
        }

        final List<T> reconciled = new ArrayList<>();
        for (RentModel rentInput : rentsInput) {
            final LocalDate firstPaymentDate = rentInput.getFirstPaymentDate() != null
                ? rentInput.getFirstPaymentDate() : agreementStartOfRental;
            final T existing = existingByKey.remove(new RentKey(rentInput.getRentalUnitId(), firstPaymentDate));

            final T rent;
            if (existing != null) {
                rent = existing;
            } else {
                rent = factory.get();
                unitIdSetter.accept(rent, rentInput.getRentalUnitId());
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
