package de.remsfal.service.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;
import de.remsfal.service.entity.dto.CommercialEntity;

import java.util.Map;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class CommercialRepository extends AbstractRepository<CommercialEntity> {

    public List<CommercialEntity> findAllCommercials(final UUID projectId, final UUID buildingId) {
        return list("projectId = :projectId and buildingId = :buildingId",
            Map.of(PARAM_PROJECT_ID, projectId, PARAM_BUILDING_ID, buildingId));
    }

    public Optional<CommercialEntity> findCommercialById(final UUID projectId, final UUID commercialId) {
        return find("id = :id and projectId = :projectId",
            Map.of(PARAM_ID, commercialId, PARAM_PROJECT_ID, projectId))
                .singleResultOptional();
    }

    public long deleteCommercialById(final UUID projectId, final UUID commercialId) {
        return delete("id = :id and projectId = :projectId",
            Map.of(PARAM_ID, commercialId, PARAM_PROJECT_ID, projectId));
    }

}
