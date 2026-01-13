package de.remsfal.service.entity.dao;

import de.remsfal.service.entity.dto.UserEntity;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TenantRepository extends AbstractRepository<UserEntity> {

    public List<UserEntity> findTenantsByProjectId(final UUID projectId) {
        return list("SELECT DISTINCT u FROM TenancyEntity t JOIN t.tenants u " +
                        "WHERE t.projectId = :projectId",
                Parameters.with("projectId", projectId));
    }

    public Optional<UserEntity> findTenantByProjectId(final UUID projectId, final UUID tenantId) {
        return find("SELECT u FROM TenancyEntity t JOIN t.tenants u " +
                        "WHERE t.projectId = :projectId and u.id = :tenantId",
                Parameters.with("projectId", projectId).and("tenantId", tenantId))
                .singleResultOptional();
    }
}
