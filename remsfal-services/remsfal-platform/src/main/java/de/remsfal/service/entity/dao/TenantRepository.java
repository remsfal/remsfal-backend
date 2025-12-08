package de.remsfal.service.entity.dao;

import de.remsfal.service.entity.dto.UserEntity;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TenantRepository extends AbstractRepository<UserEntity> {

    public List<UserEntity> findTenantsByProjectId(final UUID projectId) {
        return find("SELECT u FROM UserEntity u JOIN TenancyEntity t " +
                        "WHERE u MEMBER OF t.tenants AND t.projectId = :projectId",
                Parameters.with("projectId", projectId))
                .list();
    }


    public Optional<UserEntity> findTenantByProjectId(final UUID projectId, final UUID tenantId) {
        return find("SELECT u FROM UserEntity u JOIN TenancyEntity t" +
                     " WHERE u MEMBER OF t.tenants AND t.projectId = :projectId AND u.id = :tenantId",
                Parameters.with("projectId", projectId).and("tenantId", tenantId))
                .firstResultOptional();
    }

    @Transactional
    public long removeTenantFromProject(final UUID projectId, final UUID tenantId) {
        return delete("id = :id and projectId = :projectId",
                Parameters.with(PARAM_ID, tenantId)
                        .and(PARAM_PROJECT_ID, projectId));
    }
}
