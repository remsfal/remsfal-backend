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
     * Find all tenants for a specific project.
     *
     * @param projectId the project ID
     * @return list of tenants
     */
    public List<TenantEntity> findTenantsByProjectId(final UUID projectId) {
        return find("projectId", projectId).list();
    }

    /**
     * Find a specific tenant by ID within a project.
     *
     * @param projectId the project ID
     * @param tenantId the tenant ID
     * @return optional tenant
     */
    public Optional<TenantEntity> findTenantByProjectId(final UUID projectId, final UUID tenantId) {
        return find("projectId = :projectId and id = :tenantId",
            Parameters.with("projectId", projectId).and("tenantId", tenantId))
                .singleResultOptional();
    }

    /**
     * Find tenants by first name and last name within a specific project. Used for deduplication when creating rental
     * agreements.
     *
     * @param projectId the project ID
     * @param firstName the first name (case-insensitive)
     * @param lastName the last name (case-insensitive)
     * @return list of matching tenants
     */
    public List<TenantEntity> findByNameInProject(final UUID projectId, final String firstName, final String lastName) {
        return find(
            "LOWER(firstName) = LOWER(:firstName) AND LOWER(lastName) = LOWER(:lastName) AND projectId = :projectId",
            Parameters.with("firstName", firstName)
                .and("lastName", lastName)
                .and("projectId", projectId))
                    .list();
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
     * Find tenants by email within a specific project.
     *
     * @param email the email address
     * @param projectId the project ID
     * @return list of tenants
     */
    public List<TenantEntity> findByEmailAndProjectId(String email, UUID projectId) {
        return find("email = :email and projectId = :projectId",
            Parameters.with("email", email).and("projectId", projectId))
                .list();
    }
}
