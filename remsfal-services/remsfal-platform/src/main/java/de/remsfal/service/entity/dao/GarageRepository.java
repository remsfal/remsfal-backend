package de.remsfal.service.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;
import de.remsfal.service.entity.dto.StorageEntity;
import io.quarkus.panache.common.Parameters;

import java.util.List;
import java.util.Optional;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class GarageRepository extends AbstractRepository<StorageEntity> {

    public List<StorageEntity> findAllGarages(final String projectId, final String buildingId) {
        return list("projectId = :projectId and buildingId = :buildingId",
            Parameters.with(PARAM_PROJECT_ID, projectId).and(PARAM_BUILDING_ID, buildingId));
    }

    public Optional<StorageEntity> findByIds(final String projectId, final String garageId) {
        return find("id = :id and projectId = :projectId",
            Parameters.with(PARAM_ID, garageId).and(PARAM_PROJECT_ID, projectId))
                .singleResultOptional();
    }

    public long removeGarageByIds(final String projectId, final String garageId) {
        return delete("id = :id and projectId = :projectId",
            Parameters.with(PARAM_ID, garageId).and(PARAM_PROJECT_ID, projectId));
    }

}