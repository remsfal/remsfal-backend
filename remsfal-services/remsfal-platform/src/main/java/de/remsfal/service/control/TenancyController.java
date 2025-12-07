package de.remsfal.service.control;

import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.TenancyModel;
import de.remsfal.service.entity.dao.TenancyRepository;
import de.remsfal.service.entity.dto.TenancyEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;

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
        TenancyEntity entity = new TenancyEntity();
        entity.generateId();
        entity.setProjectId(projectId);
        entity.setStartOfRental(tenancy.getStartOfRental());
        entity.setEndOfRental(tenancy.getEndOfRental());
        tenancyRepository.persistAndFlush(entity);
        return entity;
    }

    @Transactional(TxType.MANDATORY)
    public TenancyEntity updateTenancy(final UUID projectId, TenancyEntity entity, final TenancyModel tenancy) {
        if(entity == null) {
            entity = new TenancyEntity();
            entity.generateId();
            entity.setProjectId(projectId);
        }
        if(tenancy.getStartOfRental() != null) {
            entity.setStartOfRental(tenancy.getStartOfRental());
        }
        if(tenancy.getEndOfRental() != null) {
            entity.setEndOfRental(tenancy.getEndOfRental());
        }
        return entity;
    }

}
