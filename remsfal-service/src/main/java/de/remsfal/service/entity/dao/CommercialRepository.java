package de.remsfal.service.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;
import de.remsfal.service.entity.dto.CommercialEntity;
import io.quarkus.panache.common.Parameters;

import java.util.List;
import java.util.Optional;


/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class CommercialRepository extends AbstractRepository<CommercialEntity> {

    public List<CommercialEntity> findCommercialsByBuildingId(final String projectId, final String buildingId) {
        return find("projectId = :projectId and buildingId = :buildingId",
                Parameters.with("projectId", projectId).and("buildingId", buildingId)).list();
    }

    public Optional<CommercialEntity> findCommercialById(final String projectId, final String buildingId, final String commercialId) {
        return find("id = :id and projectId = :projectId and buildingId = :buildingId",
                Parameters.with("id", commercialId)
                        .and("projectId", projectId)
                        .and("buildingId", buildingId)).singleResultOptional();
    }

    public long deleteCommercialById(final String projectId, final String buildingId, final String commercialId) {
        return delete("id = :id and projectId = :projectId and buildingId = :buildingId",
                Parameters.with("id", commercialId)
                        .and("projectId", projectId)
                        .and("buildingId", buildingId));
    }
}
