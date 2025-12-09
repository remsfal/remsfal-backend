package de.remsfal.service.entity.dao;

import com.sun.tools.xjc.reader.xmlschema.bindinfo.OptionalPropertyMode;
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
}