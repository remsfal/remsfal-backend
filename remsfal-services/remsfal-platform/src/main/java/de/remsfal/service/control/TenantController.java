package de.remsfal.service.control;

import de.remsfal.core.json.UserJson;
import de.remsfal.core.model.CustomerModel;
import de.remsfal.service.entity.dao.TenancyRepository;
import de.remsfal.service.entity.dao.TenantRepository;
import de.remsfal.service.entity.dto.TenancyEntity;
import de.remsfal.service.entity.dto.UserEntity;
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
    TenancyRepository tenancyRepository;

    @Inject
    TenantRepository tenantRepository;

    @Transactional
    public CustomerModel createTenant(final UUID projectId, final UserJson tenantJson) {
        logger.infov("Creating a new tenant for project {0}", projectId);

        // Find or create the TenancyEntity for the project
        TenancyEntity tenancy = tenancyRepository.find("projectId", projectId).firstResult();
        if (tenancy == null) {
            tenancy = new TenancyEntity();
            tenancy.generateId();
            tenancy.setProjectId(projectId);
            tenancyRepository.persist(tenancy); // Persist early to ensure ID
        }

        // Project-specific duplicate check
        List<UserEntity> existingTenants = tenantRepository.findTenantsByProjectId(projectId);
        boolean duplicate = existingTenants.stream()
                .anyMatch(t -> t.getEmail().equalsIgnoreCase(tenantJson.getEmail()));
        if (duplicate) {
            throw new BadRequestException("A tenant with this email already exists in the project");
        }

        // Create and update the new UserEntity (mirroring updateStorage)
        UserEntity entity = updateTenant(tenantJson, new UserEntity());
        entity.generateId();
        // No direct projectId or buildingId equivalent; association is via tenancy list

        // Add to tenancy (key adaptation for relationship)
        tenancy.getTenants().add(entity);

        // Persist, flush, and refresh (direct from pattern)
        tenantRepository.persistAndFlush(entity);
        tenantRepository.getEntityManager().refresh(entity);

        // Return via getter (mirroring getStorage)
        return getTenant(projectId, entity.getId());
    }

    public List<CustomerModel> getTenants(final UUID projectId) {
        logger.infov("Retrieving tenants for project {0}", projectId);
        List<UserEntity> entities = tenantRepository.findTenantsByProjectId(projectId);
        return entities.stream()
                .map(entity -> (CustomerModel) entity) // Cast is safe due to implements
                .collect(Collectors.toList());
    }

    public CustomerModel getTenant(final UUID projectId, final UUID tenantId) {
        logger.infov("Retrieving a tenant (projectId={0}, tenantId={1})",
                projectId, tenantId);
        return tenantRepository.findTenantByProjectId (projectId, tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));
    }
    @Transactional
    public CustomerModel updateTenant(final UUID projectId, final UUID tenantId, final UserJson tenantJson) {
        logger.infov("Updating tenant (projectId={0}, tenantId={1})", projectId, tenantId);

        // Find the existing tenant (UserEntity)
        UserEntity entity = tenantRepository.findTenantByProjectId(projectId, tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        // Update fields using the private helper
        updateTenant(tenantJson, entity);

        // Persist changes (flush and refresh for fresh data)
        tenantRepository.persistAndFlush(entity);
        tenantRepository.getEntityManager().refresh(entity);

        // Return updated model (via getter for consistency)
        return getTenant(projectId, tenantId);
    }


    // Helper method (new, mirroring updateStorage for mapping JSON to entity)
    private UserEntity updateTenant(final UserJson json, final UserEntity entity) {
        entity.setEmail(json.getEmail());
        entity.setFirstName(json.getFirstName());
        entity.setLastName(json.getLastName());

        return entity;
    }

    @Transactional
    public boolean deleteTenant (final UUID projectId, final UUID tenantId) {
        logger.infov("Deleting a site (projectId={0}, tenantId={1})", projectId, tenantId);
        return tenantRepository.removeTenantFromProject(projectId, tenantId) > 0;
    }
}
