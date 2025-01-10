package de.remsfal.service.entity.dao;

import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import de.remsfal.service.entity.dto.ApartmentEntity;

import java.util.List;
import java.util.Optional;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class ApartmentRepository extends AbstractRepository<ApartmentEntity> {

    public List<ApartmentEntity> findApartmentsByBuildingId(final String projectId, final String buildingId) {
        return find("projectId = :projectId and buildingId = :buildingId",
            Parameters.with(PARAM_PROJECT_ID, projectId).and(PARAM_BUILDING_ID, buildingId))
                .list();
    }

    public Optional<ApartmentEntity> findByIds(final String projectId, final String apartmentId) {
        return find("id = :id and projectId = :projectId",
            Parameters.with(PARAM_ID, apartmentId).and(PARAM_PROJECT_ID, projectId))
                .singleResultOptional();
    }

    public long removeApartmentByIds(final String projectId, final String apartmentId) {
        return delete("id = :id and projectId = :projectId",
            Parameters.with(PARAM_ID, apartmentId).and(PARAM_PROJECT_ID, projectId));
    }

}
