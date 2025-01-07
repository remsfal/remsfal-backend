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

    public List<CommercialEntity> findCommercialByBuildingId(String buildingId) {
        return getEntityManager()
                .createQuery(
                        "SELECT c FROM CommercialEntity c WHERE c.buildingId = :buildingId",
                        CommercialEntity.class
                )
                .setParameter("buildingId", buildingId)
                .getResultList();
    }
    public Optional<CommercialEntity> findCommercialById(final String projectId,
                                                         final String buildingId,
                                                         final String commercialId) {
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
