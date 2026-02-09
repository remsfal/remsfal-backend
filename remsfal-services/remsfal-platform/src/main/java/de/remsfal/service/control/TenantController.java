package de.remsfal.service.control;

import de.remsfal.core.json.project.TenantJson;
import de.remsfal.core.model.project.TenantModel;
import de.remsfal.service.entity.dao.RentalAgreementRepository;
import de.remsfal.service.entity.dao.TenantRepository;
import de.remsfal.service.entity.dto.RentalAgreementEntity;
import de.remsfal.service.entity.dto.TenantEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import org.jboss.logging.Logger;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequestScoped
public class TenantController {
    @Inject
    Logger logger;

    @Inject
    RentalAgreementRepository rentalAgreementRepository;

    @Inject
    TenantRepository tenantRepository;

    @Transactional
    public TenantModel createTenant(final UUID projectId, final TenantJson tenantJson) {
        logger.infov("Creating a new tenant for project {0}", projectId);

        RentalAgreementEntity agreement = rentalAgreementRepository.find("projectId", projectId).firstResult();
        if (agreement == null) {
            agreement = new RentalAgreementEntity();
            agreement.generateId();
            agreement.setProjectId(projectId);
            agreement.setStartOfRental(java.time.LocalDate.now());
            agreement.setTenants(new java.util.ArrayList<>());
            rentalAgreementRepository.persist(agreement);
        } else if (agreement.getTenants() == null) {
            agreement.setTenants(new java.util.ArrayList<>());
        }

        List<TenantEntity> existingTenants = tenantRepository.findTenantsByProjectId(projectId);
        boolean duplicate = existingTenants.stream()
            .anyMatch(t -> t.getEmail() != null && t.getEmail().equalsIgnoreCase(tenantJson.getEmail()));
        if (duplicate) {
            throw new BadRequestException("A tenant with this email already exists in the project");
        }

        TenantEntity entity = applyJsonToEntity(tenantJson, new TenantEntity());
        entity.generateId();
        entity.setAgreement(agreement);

        agreement.getTenants().add(entity);

        rentalAgreementRepository.mergeAndFlush(agreement);

        TenantEntity managedTenant = tenantRepository.findById(entity.getId());

        return getTenant(projectId, managedTenant.getId());
    }

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

    @Transactional
    public TenantModel updateTenant(final UUID projectId, final UUID tenantId, final TenantJson tenantJson) {
        logger.infov("Updating tenant (projectId={0}, tenantId={1})", projectId, tenantId);

        TenantEntity entity = tenantRepository.findTenantByProjectId(projectId, tenantId)
            .orElseThrow(() -> new NotFoundException("Tenant not found"));

        applyJsonToEntity(tenantJson, entity);

        tenantRepository.persistAndFlush(entity);
        tenantRepository.getEntityManager().refresh(entity);

        return getTenant(projectId, tenantId);
    }

    private TenantEntity applyJsonToEntity(final TenantJson json, final TenantEntity entity) {
        if (json.getEmail() != null) {
            entity.setEmail(json.getEmail());
        }
        entity.setFirstName(json.getFirstName());
        entity.setLastName(json.getLastName());

        if (json.getMobilePhoneNumber() != null) {
            entity.setMobilePhoneNumber(json.getMobilePhoneNumber());
        }
        if (json.getBusinessPhoneNumber() != null) {
            entity.setBusinessPhoneNumber(json.getBusinessPhoneNumber());
        }
        if (json.getPrivatePhoneNumber() != null) {
            entity.setPrivatePhoneNumber(json.getPrivatePhoneNumber());
        }
        return entity;
    }

    @Transactional
    public void deleteTenant(final UUID projectId, final UUID tenantIdToRemove) {
        RentalAgreementEntity agreement = rentalAgreementRepository.findRentalAgreementByProjectId(projectId)
            .orElseThrow(() -> new NotFoundException("Rental agreement not found for project"));

        boolean removed = agreement.getTenants()
            .removeIf(tenant -> tenant.getId().equals(tenantIdToRemove));

        if (!removed) {
            throw new NotFoundException("Tenant not found in this rental agreement.");
        }

        rentalAgreementRepository.mergeAndFlush(agreement);
    }
}
