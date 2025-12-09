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

        TenancyEntity tenancy = tenancyRepository.find("projectId", projectId).firstResult();
        if (tenancy == null) {
            tenancy = new TenancyEntity();
            tenancy.generateId();
            tenancy.setProjectId(projectId);
            tenancy.setTenants(new java.util.ArrayList<>());
            tenancyRepository.persist(tenancy);
        } else if (tenancy.getTenants() == null) {
            tenancy.setTenants(new java.util.ArrayList<>());
        }

        List<UserEntity> existingTenants = tenantRepository.findTenantsByProjectId(projectId);
        boolean duplicate = existingTenants.stream()
            .anyMatch(t -> t.getEmail().equalsIgnoreCase(tenantJson.getEmail()));
        if (duplicate) {
            throw new BadRequestException("A tenant with this email already exists in the project");
        }

        UserEntity entity = updateTenant(tenantJson, new UserEntity());
        entity.generateId();

        tenancy.getTenants().add(entity);

        tenancyRepository.mergeAndFlush(tenancy);

        UserEntity managedUser = tenantRepository.findById(entity.getId());

        return getTenant(projectId, managedUser.getId());
    }

    public List<CustomerModel> getTenants(final UUID projectId) {
        logger.infov("Retrieving tenants for project {0}", projectId);
        List<UserEntity> entities = tenantRepository.findTenantsByProjectId(projectId);
        return entities.stream()
                .map(entity -> (CustomerModel) entity)
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

        UserEntity entity = tenantRepository.findTenantByProjectId(projectId, tenantId)
            .orElseThrow(() -> new NotFoundException("Tenant not found"));

        updateTenant(tenantJson, entity);

        tenantRepository.persistAndFlush(entity);
        tenantRepository.getEntityManager().refresh(entity);

        return getTenant(projectId, tenantId);
    }

    private UserEntity updateTenant(final UserJson json, final UserEntity entity) {
        entity.setEmail(json.getEmail());
        entity.setFirstName(json.getFirstName());
        entity.setLastName(json.getLastName());

        return entity;
    }

    @Transactional
    public void deleteTenant(final UUID projectId, final UUID tenantIdToRemove) {
        TenancyEntity tenancy = tenancyRepository.findTenancyByProjectId(projectId)
            .orElseThrow(() -> new NotFoundException("Tenancy not found for project"));

        boolean removed = tenancy.getTenants()
            .removeIf(tenant -> tenant.getId().equals(tenantIdToRemove));

        if (!removed) {
            throw new NotFoundException("Tenant not found in this tenancy.");
        }

        tenancyRepository.mergeAndFlush(tenancy);
    }
}
