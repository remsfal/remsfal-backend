package de.remsfal.service.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;
import de.remsfal.service.entity.dto.StorageEntity;
import io.quarkus.panache.common.Parameters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class StorageRepository extends AbstractRepository<StorageEntity> {

    public List<StorageEntity> findAllStorages(final UUID projectId, final UUID buildingId) {
        return list("projectId = :projectId and buildingId = :buildingId",
            Parameters.with(PARAM_PROJECT_ID, projectId).and(PARAM_BUILDING_ID, buildingId));
    }

    public Optional<StorageEntity> findByIds(final UUID projectId, final UUID storageId) {
        return find("id = :id and projectId = :projectId",
            Parameters.with(PARAM_ID, storageId).and(PARAM_PROJECT_ID, projectId))
                .singleResultOptional();
    }

    public long removeStorageByIds(final UUID projectId, final UUID storageId) {
        return delete("id = :id and projectId = :projectId",
            Parameters.with(PARAM_ID, storageId).and(PARAM_PROJECT_ID, projectId));
    }

}