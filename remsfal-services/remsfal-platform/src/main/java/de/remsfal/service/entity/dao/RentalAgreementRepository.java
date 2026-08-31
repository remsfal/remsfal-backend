package de.remsfal.service.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import java.util.Map;

import de.remsfal.core.model.RentalUnitModel.UnitType;
import de.remsfal.service.entity.dto.RentalAgreementEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class RentalAgreementRepository extends AbstractRepository<RentalAgreementEntity> {

    private static final String PARAM_RENTAL_UNIT_ID = "rentalUnitId";

    public List<RentalAgreementEntity> findRentalAgreementsByTenant(final UUID tenantId) {
        return find("SELECT a FROM RentalAgreementEntity a JOIN a.tenants tenant WHERE tenant.user.id = :userId",
            Map.of(PARAM_USER_ID, tenantId)).list();
    }

    /**
     * Find all rental agreements a given tenant entity is linked to (by tenant entity id, not user id).
     *
     * @param tenantId the {@code TenantEntity} id
     * @return list of rental agreements
     */
    public List<RentalAgreementEntity> findRentalAgreementsByTenantId(final UUID tenantId) {
        return find("SELECT a FROM RentalAgreementEntity a JOIN a.tenants tenant WHERE tenant.id = :id",
            Map.of(PARAM_ID, tenantId)).list();
    }

    public Optional<RentalAgreementEntity> findRentalAgreementByTenant(final UUID tenantId, final UUID agreementId) {
        return find("SELECT a FROM RentalAgreementEntity a JOIN a.tenants tenant "
                + "WHERE a.id = :id and tenant.user.id = :userId",
            Map.of(PARAM_ID, agreementId, PARAM_USER_ID, tenantId))
            .singleResultOptional();
    }

    public Optional<RentalAgreementEntity> findRentalAgreementByProjectId(final UUID projectId) {
        return find("projectId", projectId).firstResultOptional();
    }

    public List<RentalAgreementEntity> findRentalAgreementByProject(final UUID projectId) {
        return find("projectId", projectId).list();
    }

    public List<RentalAgreementEntity> findRentalAgreementByProject(final UUID projectId,
            final UnitType rentalUnitType, final UUID rentalUnitId) {
        if (rentalUnitType == null) {
            return findRentalAgreementByProject(projectId);
        }
        final Map<String, Object> params = new HashMap<>();
        params.put(PARAM_PROJECT_ID, projectId);
        params.put(PARAM_RENTAL_UNIT_ID, rentalUnitId);
        return switch (rentalUnitType) {
            case PROPERTY -> find("SELECT DISTINCT a FROM RentalAgreementEntity a JOIN a.propertyRent r "
                + "WHERE a.projectId = :projectId AND (:rentalUnitId IS NULL OR r.propertyId = :rentalUnitId)",
                params).list();
            case SITE -> find("SELECT DISTINCT a FROM RentalAgreementEntity a JOIN a.siteRent r "
                + "WHERE a.projectId = :projectId AND (:rentalUnitId IS NULL OR r.siteId = :rentalUnitId)",
                params).list();
            case BUILDING -> find("SELECT DISTINCT a FROM RentalAgreementEntity a JOIN a.buildingRent r "
                + "WHERE a.projectId = :projectId AND (:rentalUnitId IS NULL OR r.buildingId = :rentalUnitId)",
                params).list();
            case APARTMENT -> find("SELECT DISTINCT a FROM RentalAgreementEntity a JOIN a.apartmentRent r "
                + "WHERE a.projectId = :projectId AND (:rentalUnitId IS NULL OR r.apartmentId = :rentalUnitId)",
                params).list();
            case STORAGE -> find("SELECT DISTINCT a FROM RentalAgreementEntity a JOIN a.storageRent r "
                + "WHERE a.projectId = :projectId AND (:rentalUnitId IS NULL OR r.storageId = :rentalUnitId)",
                params).list();
            case COMMERCIAL -> find("SELECT DISTINCT a FROM RentalAgreementEntity a JOIN a.commercialRent r "
                + "WHERE a.projectId = :projectId AND (:rentalUnitId IS NULL OR r.commercialId = :rentalUnitId)",
                params).list();
        };
    }

    public Optional<RentalAgreementEntity> findRentalAgreementByProject(final UUID projectId, final UUID agreementId) {
        return find("SELECT a FROM RentalAgreementEntity a LEFT JOIN FETCH a.tenants t " +
            "LEFT JOIN FETCH t.user u LEFT JOIN FETCH t.address WHERE a.id = :id and a.projectId = :projectId",
            Map.of(PARAM_ID, agreementId, PARAM_PROJECT_ID, projectId))
            .singleResultOptional();
    }
    
    public long removeRentalAgreementByIds(final UUID projectId, final UUID agreementId) {
        return delete("id = :id and projectId = :projectId",
            Map.of(PARAM_ID, agreementId, PARAM_PROJECT_ID, projectId));
    }
}
