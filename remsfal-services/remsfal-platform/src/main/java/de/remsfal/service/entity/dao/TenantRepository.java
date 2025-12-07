package de.remsfal.service.entity.dao;

import de.remsfal.service.entity.dto.TenancyEntity;
import de.remsfal.service.entity.dto.UserEntity;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TenantRepository extends AbstractRepository<TenancyEntity> {

    public List<UserEntity> findTenantsByProjectId(final UUID projectId) {
        TenancyEntity tenancy = find("projectId", projectId).firstResult();
        if (tenancy != null) {
            return tenancy.getTenants();
        }
        return List.of();
    }

    public Optional<UserEntity> findTenantByProjectId(final UUID projectId, final UUID tenantId) {
        TenancyEntity tenancy = find("projectId", projectId).firstResult();
        if (tenancy != null) {
            return tenancy.getTenants().stream()
                    .filter(u -> u.getId().equals(tenantId))
                    .findFirst();
        }
        return Optional.empty();
    }

    @Transactional
    public long removeTenantFromProject(final UUID projectId, final UUID tenantId) {
        return delete("id = :id and projectId = :projectId",
                Parameters.with(PARAM_ID, tenantId)
                        .and(PARAM_PROJECT_ID, projectId));
    }
}
