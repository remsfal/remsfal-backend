package de.remsfal.service.control;

import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.TenancyModel;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dao.TenancyRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.core.model.CustomerModel;
import de.remsfal.service.entity.dto.TenancyEntity;
import de.remsfal.service.entity.dto.UserEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class TenancyController {

    @Inject
    Logger logger;

    @Inject
    TenancyRepository tenancyRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    ProjectRepository projectRepository;

    public List<TenancyEntity> getTenancies(final UserModel tenant) {
        logger.infov("Retrieving all tenancies (tenantId = {0})", tenant.getId());
        return tenancyRepository.findTenanciesByTenant(tenant.getId());
    }

    public TenancyEntity getTenancy(final UserModel tenant, final UUID tenancyId) {
        logger.infov("Retrieving a tenancy (tenantId = {0}, tenancyId = {1})",
            tenant.getId(), tenancyId);
        return tenancyRepository.findTenancyByTenant(tenant.getId(), tenancyId)
            .orElseThrow(() -> new NotFoundException("Tenancy not exist"));
    }

    public List<TenancyEntity> getTenanciesByProject(final UUID projectId) {
        logger.infov("Retrieving all tenancies (projectId = {0})", projectId);
        return tenancyRepository.findTenancyByProject(projectId);
    }

    public TenancyEntity getTenancyByProject(final UUID projectId, final UUID tenancyId) {
        logger.infov("Retrieving a tenancy (projectId = {0}, tenancyId = {1})", projectId, tenancyId);
        return tenancyRepository.findTenancyByProject(projectId, tenancyId)
            .orElseThrow(() -> new NotFoundException("Tenancy not exist"));
    }

    @Transactional
    public TenancyEntity createTenancy(final UUID projectId, final TenancyModel tenancy) {
        logger.infov("Creating a tenancy (project={0}", projectId);

        if (projectRepository.findById(projectId) == null) {
            throw new NotFoundException("Project not exist");
        }

        TenancyEntity entity = new TenancyEntity();
        entity.generateId();
        entity.setProjectId(projectId);
        entity.setStartOfRental(tenancy.getStartOfRental());
        entity.setEndOfRental(tenancy.getEndOfRental());

        final List<? extends CustomerModel> tenants = tenancy.getTenants();
        if (tenants != null && !tenants.isEmpty()) {
            List<UserEntity> userEntities = tenants.stream()
                .map(user -> userRepository.findByIdWithAdditionalEmails(user.getId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            if (userEntities.size() != tenants.size()) {
                throw new BadRequestException("One or more users not found");
            }
            entity.setTenants(userEntities);
        }

        tenancyRepository.persistAndFlush(entity);
        return entity;
    }

    @Transactional
    public TenancyEntity updateTenancy(final UUID projectId, final UUID tenancyId, final TenancyModel tenancy) {
        logger.infov("Updating a tenancy (projectId={0}, tenancyId={1})", projectId, tenancyId);
        final TenancyEntity entity = tenancyRepository.findTenancyByProject(projectId, tenancyId)
            .orElseThrow(() -> new NotFoundException("Tenancy not exist"));

        if (tenancy.getStartOfRental() != null) {
            entity.setStartOfRental(tenancy.getStartOfRental());
        }
        if (tenancy.getEndOfRental() != null) {
            entity.setEndOfRental(tenancy.getEndOfRental());
        }

        final List<? extends CustomerModel> tenants = tenancy.getTenants();
        if (tenants != null) {
            List<UserEntity> userEntities = tenants.stream()
                .map(user -> userRepository.findByIdWithAdditionalEmails(user.getId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            if (userEntities.size() != tenants.size()) {
                throw new BadRequestException("One or more users not found");
            }
            entity.setTenants(userEntities);
        }

        return tenancyRepository.merge(entity);
    }

}
