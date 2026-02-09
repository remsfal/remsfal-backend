package de.remsfal.service.entity.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import de.remsfal.service.entity.dto.TenantEntity;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository for {@link TenantEntity}.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class TenantRepository extends AbstractRepository<TenantEntity> {

    /**
     * Find all tenants for a specific rental agreement.
     *
     * @param agreementId the agreement ID
     * @return list of tenants
     */
    public List<TenantEntity> findByAgreementId(UUID agreementId) {
        return find("agreement.id", agreementId).list();
    }

    /**
     * Find all tenants for a specific project (across all agreements).
     *
     * @param projectId the project ID
     * @return list of tenants
     */
    public List<TenantEntity> findTenantsByProjectId(final UUID projectId) {
        return list("SELECT DISTINCT t FROM TenantEntity t JOIN t.agreement a " +
                        "WHERE a.projectId = :projectId",
                Parameters.with("projectId", projectId));
    }

    /**
     * Find a specific tenant by ID within a project.
     *
     * @param projectId the project ID
     * @param tenantId the tenant ID
     * @return optional tenant
     */
    public Optional<TenantEntity> findTenantByProjectId(final UUID projectId, final UUID tenantId) {
        return find("SELECT t FROM TenantEntity t JOIN t.agreement a " +
                        "WHERE a.projectId = :projectId and t.id = :tenantId",
                Parameters.with("projectId", projectId).and("tenantId", tenantId))
                .singleResultOptional();
    }

    /**
     * Find a specific tenant by ID within a rental agreement.
     *
     * @param tenantId the tenant ID
     * @param agreementId the agreement ID
     * @return optional tenant
     */
    public Optional<TenantEntity> findByIdAndAgreementId(UUID tenantId, UUID agreementId) {
        return find("id = ?1 and agreement.id = ?2", tenantId, agreementId)
                .singleResultOptional();
    }

    /**
     * Find all tenants linked to a specific user.
     *
     * @param userId the user ID
     * @return list of tenants
     */
    public List<TenantEntity> findByUserId(UUID userId) {
        return find("user.id", userId).list();
    }

    /**
     * Find a tenant by email within a specific rental agreement.
     *
     * @param email the email address
     * @param agreementId the agreement ID
     * @return optional tenant
     */
    public Optional<TenantEntity> findByEmailAndAgreementId(String email, UUID agreementId) {
        return find("email = ?1 and agreement.id = ?2", email, agreementId)
                .singleResultOptional();
    }
}
