package de.remsfal.service.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import de.remsfal.service.entity.dto.TenancyEntity;
import io.quarkus.panache.common.Parameters;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class TenancyRepository extends AbstractRepository<TenancyEntity> {

    public List<TenancyEntity> findTenanciesByTenant(final UUID tenantId) {
        return find("SELECT t FROM TenancyEntity t JOIN t.tenants tenant WHERE tenant.id = :userId",
            Parameters.with(PARAM_USER_ID, tenantId)).list();
    }

    public Optional<TenancyEntity> findTenancyByTenant(final UUID tenantId, final UUID tenancyId) {
        return find("SELECT t FROM TenancyEntity t JOIN t.tenants tenant WHERE t.id = :id and tenant.id = :userId",
            Parameters.with(PARAM_ID, tenancyId).and(PARAM_USER_ID, tenantId))
            .singleResultOptional();
    }

    public Optional<TenancyEntity> findTenancyByProjectId(final UUID projectId) {
        return find("projectId", projectId).firstResultOptional();
    }
  
    public List<TenancyEntity> findTenancyByProject(final UUID projectId) {
        return find("projectId", projectId).list();
    }

    public Optional<TenancyEntity> findTenancyByProject(final UUID projectId, final UUID tenancyId) {
        return find("SELECT t FROM TenancyEntity t LEFT JOIN FETCH t.tenants u " +
            "LEFT JOIN FETCH u.additionalEmails WHERE t.id = :id and t.projectId = :projectId",
            Parameters.with(PARAM_ID, tenancyId).and(PARAM_PROJECT_ID, projectId))
            .singleResultOptional();
    }
}