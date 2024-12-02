package de.remsfal.service.entity.dao;

import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import de.remsfal.service.entity.dto.ApartmentEntity;

import java.util.Optional;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class ApartmentRepository extends AbstractRepository<ApartmentEntity> {

    public Optional<ApartmentEntity> findByIds(final String apartmentId, final String projectId, final String buildingId) {
        return find("id = :apartmentId and projectId = :projectId and buildingId = :buildingId",
                Parameters.with("id", apartmentId).and("projectId", projectId).and("buildingId", buildingId))
                .singleResultOptional();
    }

    public long removeApartmentByIds(final String apartmentId, final String projectId, final String buildingId) {
        return delete("id = :apartmentId and projectId = :projectId and buildingId = :buildingId",
                Parameters.with("id", apartmentId).and("projectId", projectId).and("buildingId", buildingId));
    }

}