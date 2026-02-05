package de.remsfal.service.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import de.remsfal.service.entity.dto.RentalAgreementEntity;
import io.quarkus.panache.common.Parameters;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class RentalAgreementRepository extends AbstractRepository<RentalAgreementEntity> {

    public List<RentalAgreementEntity> findRentalAgreementsByTenant(final UUID tenantId) {
        return find("SELECT a FROM RentalAgreementEntity a JOIN a.tenants tenant WHERE tenant.user.id = :userId",
            Parameters.with(PARAM_USER_ID, tenantId)).list();
    }

    public Optional<RentalAgreementEntity> findRentalAgreementByTenant(final UUID tenantId, final UUID agreementId) {
        return find("SELECT a FROM RentalAgreementEntity a JOIN a.tenants tenant "
                + "WHERE a.id = :id and tenant.user.id = :userId",
            Parameters.with(PARAM_ID, agreementId).and(PARAM_USER_ID, tenantId))
            .singleResultOptional();
    }

    public Optional<RentalAgreementEntity> findRentalAgreementByProjectId(final UUID projectId) {
        return find("projectId", projectId).firstResultOptional();
    }

    public List<RentalAgreementEntity> findRentalAgreementByProject(final UUID projectId) {
        return find("projectId", projectId).list();
    }

    public Optional<RentalAgreementEntity> findRentalAgreementByProject(final UUID projectId, final UUID agreementId) {
        return find("SELECT a FROM RentalAgreementEntity a LEFT JOIN FETCH a.tenants t " +
            "LEFT JOIN FETCH t.user u LEFT JOIN FETCH t.address WHERE a.id = :id and a.projectId = :projectId",
            Parameters.with(PARAM_ID, agreementId).and(PARAM_PROJECT_ID, projectId))
            .singleResultOptional();
    }
}
