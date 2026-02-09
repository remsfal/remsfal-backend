package de.remsfal.service.control;

import de.remsfal.core.model.AddressModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.RentalAgreementModel;
import de.remsfal.core.model.project.RentModel;
import de.remsfal.core.model.project.RentModel.BillingCycle;
import de.remsfal.core.model.project.TenantModel;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dao.RentalAgreementRepository;
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
import de.remsfal.service.entity.dto.UserEntity;
import de.remsfal.service.entity.dto.superclass.RentEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        final List<? extends TenantModel> tenants = agreement.getTenants();
        if (tenants != null && !tenants.isEmpty()) {
            List<TenantEntity> tenantEntities = processTenants(entity.getId(), tenants);
            // Set bidirectional relationship
            tenantEntities.forEach(tenant -> tenant.setAgreement(entity));
            entity.setTenants(tenantEntities);
        }

        // Process rents
        processRents(entity, agreement);

        rentalAgreementRepository.persistAndFlush(entity);
        return entity;
    }

    @Transactional
    public RentalAgreementEntity updateRentalAgreement(final UUID projectId, final UUID agreementId,
            final RentalAgreementModel agreement) {
        logger.infov("Updating a rental agreement (projectId={0}, agreementId={1})", projectId, agreementId);
        final RentalAgreementEntity entity =
            rentalAgreementRepository.findRentalAgreementByProject(projectId, agreementId)
                .orElseThrow(() -> new NotFoundException("Rental agreement not exist"));

        if (agreement.getStartOfRental() != null) {
            entity.setStartOfRental(agreement.getStartOfRental());
        }
        if (agreement.getEndOfRental() != null) {
            entity.setEndOfRental(agreement.getEndOfRental());
        }

        // Update tenants (replace entire list due to orphanRemoval)
        final List<? extends TenantModel> tenants = agreement.getTenants();
        if (tenants != null && !tenants.isEmpty()) {
            entity.getTenants().clear(); // Triggers orphanRemoval
            List<TenantEntity> tenantEntities = processTenants(agreementId, tenants);
            // Set bidirectional relationship
            tenantEntities.forEach(tenant -> tenant.setAgreement(entity));
            entity.getTenants().addAll(tenantEntities);
        }

        // Update rents (only replace if provided)
        processRents(entity, agreement);

        return rentalAgreementRepository.merge(entity);
    }

    /**
     * Process tenant models and create tenant entities.
     * If a tenant has an email, attempts to link to an existing user account.
     *
     * @param agreementId the agreement ID
     * @param tenantsInput the tenant models from the request
     * @return list of tenant entities
     */
    private List<TenantEntity> processTenants(final UUID agreementId, final List<? extends TenantModel> tenantsInput) {
        List<TenantEntity> tenantEntities = new ArrayList<>();

        for (TenantModel tenantInput : tenantsInput) {
            TenantEntity tenant = new TenantEntity();
            tenant.generateId();

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

            tenantEntities.add(tenant);
        }

        return tenantEntities;
    }

    /**
     * Process all rent types from the agreement model and update them on the entity.
     * PATCH-style behavior: only updates rent lists that are provided in the agreement.
     * If a rent list is provided, it replaces the existing list (orphanRemoval will delete old ones).
     *
     * @param entity the rental agreement entity
     * @param agreement the rental agreement model
     */
    private void processRents(final RentalAgreementEntity entity, final RentalAgreementModel agreement) {
        if (agreement.getPropertyRents() != null) {
            if (entity.getPropertyRents() == null) {
                entity.setPropertyRents(new ArrayList<>());
            } else {
                entity.getPropertyRents().clear();
            }
            if (!agreement.getPropertyRents().isEmpty()) {
                processPropertyRents(entity, agreement.getPropertyRents());
            }
        }

        if (agreement.getSiteRents() != null) {
            if (entity.getSiteRents() == null) {
                entity.setSiteRents(new ArrayList<>());
            } else {
                entity.getSiteRents().clear();
            }
            if (!agreement.getSiteRents().isEmpty()) {
                processSiteRents(entity, agreement.getSiteRents());
            }
        }

        if (agreement.getBuildingRents() != null) {
            if (entity.getBuildingRents() == null) {
                entity.setBuildingRents(new ArrayList<>());
            } else {
                entity.getBuildingRents().clear();
            }
            if (!agreement.getBuildingRents().isEmpty()) {
                processBuildingRents(entity, agreement.getBuildingRents());
            }
        }

        if (agreement.getApartmentRents() != null) {
            if (entity.getApartmentRents() == null) {
                entity.setApartmentRents(new ArrayList<>());
            } else {
                entity.getApartmentRents().clear();
            }
            if (!agreement.getApartmentRents().isEmpty()) {
                processApartmentRents(entity, agreement.getApartmentRents());
            }
        }

        if (agreement.getStorageRents() != null) {
            if (entity.getStorageRents() == null) {
                entity.setStorageRents(new ArrayList<>());
            } else {
                entity.getStorageRents().clear();
            }
            if (!agreement.getStorageRents().isEmpty()) {
                processStorageRents(entity, agreement.getStorageRents());
            }
        }

        if (agreement.getCommercialRents() != null) {
            if (entity.getCommercialRents() == null) {
                entity.setCommercialRents(new ArrayList<>());
            } else {
                entity.getCommercialRents().clear();
            }
            if (!agreement.getCommercialRents().isEmpty()) {
                processCommercialRents(entity, agreement.getCommercialRents());
            }
        }
    }

    /**
     * Process property rent models and create property rent entities.
     *
     * @param entity the rental agreement entity
     * @param rentsInput the rent models from the request
     */
    private void processPropertyRents(final RentalAgreementEntity entity, List<? extends RentModel> rentsInput) {
        for (RentModel rentInput : rentsInput) {
            PropertyRentEntity rent = new PropertyRentEntity();
            rent.setPropertyId(rentInput.getUnitId());
            mapRentFields(rentInput, rent, entity.getStartOfRental());
            entity.getPropertyRents().add(rent);
        }
    }

    /**
     * Process site rent models and create site rent entities.
     *
     * @param entity the rental agreement entity
     * @param rentsInput the rent models from the request
     */
    private void processSiteRents(final RentalAgreementEntity entity, List<? extends RentModel> rentsInput) {
        for (RentModel rentInput : rentsInput) {
            SiteRentEntity rent = new SiteRentEntity();
            rent.setSiteId(rentInput.getUnitId());
            mapRentFields(rentInput, rent, entity.getStartOfRental());
            entity.getSiteRents().add(rent);
        }
    }

    /**
     * Process building rent models and create building rent entities.
     *
     * @param entity the rental agreement entity
     * @param rentsInput the rent models from the request
     */
    private void processBuildingRents(final RentalAgreementEntity entity, List<? extends RentModel> rentsInput) {
        for (RentModel rentInput : rentsInput) {
            BuildingRentEntity rent = new BuildingRentEntity();
            rent.setBuildingId(rentInput.getUnitId());
            mapRentFields(rentInput, rent, entity.getStartOfRental());
            entity.getBuildingRents().add(rent);
        }
    }

    /**
     * Process apartment rent models and create apartment rent entities.
     *
     * @param entity the rental agreement entity
     * @param rentsInput the rent models from the request
     */
    private void processApartmentRents(final RentalAgreementEntity entity, List<? extends RentModel> rentsInput) {
        for (RentModel rentInput : rentsInput) {
            ApartmentRentEntity rent = new ApartmentRentEntity();
            rent.setApartmentId(rentInput.getUnitId());
            mapRentFields(rentInput, rent, entity.getStartOfRental());
            entity.getApartmentRents().add(rent);
        }
    }

    /**
     * Process storage rent models and create storage rent entities.
     *
     * @param entity the rental agreement entity
     * @param rentsInput the rent models from the request
     */
    private void processStorageRents(final RentalAgreementEntity entity, List<? extends RentModel> rentsInput) {
        for (RentModel rentInput : rentsInput) {
            StorageRentEntity rent = new StorageRentEntity();
            rent.setStorageId(rentInput.getUnitId());
            mapRentFields(rentInput, rent, entity.getStartOfRental());
            entity.getStorageRents().add(rent);
        }
    }

    /**
     * Process commercial rent models and create commercial rent entities.
     *
     * @param entity the rental agreement entity
     * @param rentsInput the rent models from the request
     */
    private void processCommercialRents(final RentalAgreementEntity entity, List<? extends RentModel> rentsInput) {
        for (RentModel rentInput : rentsInput) {
            CommercialRentEntity rent = new CommercialRentEntity();
            rent.setCommercialId(rentInput.getUnitId());
            mapRentFields(rentInput, rent, entity.getStartOfRental());
            entity.getCommercialRents().add(rent);
        }
    }

    /**
     * Maps common rent fields from a rent model to a rent entity.
     *
     * @param <T> the rent entity type
     * @param rentInput the rent model
     * @param rentEntity the rent entity
     */
    private <T extends RentEntity> void mapRentFields(
            RentModel rentInput, T rentEntity, LocalDate agreementStartOfRental) {
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
