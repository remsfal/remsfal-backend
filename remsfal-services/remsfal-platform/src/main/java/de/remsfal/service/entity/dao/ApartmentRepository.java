package de.remsfal.service.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;

import de.remsfal.service.entity.dto.ApartmentEntity;

import java.util.Map;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class ApartmentRepository extends AbstractRepository<ApartmentEntity> {

    public List<ApartmentEntity> findAllApartments(final UUID projectId, final UUID buildingId) {
        return list("projectId = :projectId and buildingId = :buildingId",
            Map.of(PARAM_PROJECT_ID, projectId, PARAM_BUILDING_ID, buildingId));
    }

    public Optional<ApartmentEntity> findByIds(final UUID projectId, final UUID apartmentId) {
        return find("id = :id and projectId = :projectId",
            Map.of(PARAM_ID, apartmentId, PARAM_PROJECT_ID, projectId))
                .singleResultOptional();
    }

    public long removeApartmentByIds(final UUID projectId, final UUID apartmentId) {
        return delete("id = :id and projectId = :projectId",
            Map.of(PARAM_ID, apartmentId, PARAM_PROJECT_ID, projectId));
    }

}
