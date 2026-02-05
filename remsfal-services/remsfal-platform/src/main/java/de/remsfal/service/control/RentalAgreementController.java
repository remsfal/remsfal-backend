package de.remsfal.service.control;

import de.remsfal.core.model.AddressModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.RentalAgreementModel;
import de.remsfal.core.model.project.TenantModel;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dao.RentalAgreementRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.AddressEntity;
import de.remsfal.service.entity.dto.RentalAgreementEntity;
import de.remsfal.service.entity.dto.TenantEntity;
import de.remsfal.service.entity.dto.UserEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

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

    /**
     * Process tenant models and create tenant entities.
     * If a tenant has an email, attempts to link to an existing user account.
     *
     * @param agreementId the agreement ID
     * @param tenantsInput the tenant models from the request
     * @return list of tenant entities
     */
    private List<TenantEntity> processTenants(UUID agreementId, List<? extends TenantModel> tenantsInput) {
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
        if (tenants != null) {
            entity.getTenants().clear(); // Triggers orphanRemoval
            List<TenantEntity> tenantEntities = processTenants(agreementId, tenants);
            // Set bidirectional relationship
            tenantEntities.forEach(tenant -> tenant.setAgreement(entity));
            entity.getTenants().addAll(tenantEntities);
        }

        return rentalAgreementRepository.merge(entity);
    }

}
