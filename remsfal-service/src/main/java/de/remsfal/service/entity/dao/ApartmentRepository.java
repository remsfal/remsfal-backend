package de.remsfal.service.entity.dao;

import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import de.remsfal.service.entity.dto.ApartmentEntity;

import java.util.Optional;
import java.util.List;


/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class ApartmentRepository extends AbstractRepository<ApartmentEntity> {

    private static final String BUILDING_ID = "buildingId";

    public Optional<ApartmentEntity> findByIds(final String apartmentId,
                                               final String projectId, final String buildingId) {
        return find("id = :id and projectId = :projectId and buildingId = :buildingId",
                Parameters.with("id", apartmentId).and("projectId", projectId)
                        .and(BUILDING_ID, buildingId))
                .singleResultOptional();
    }

    public long removeApartmentByIds(final String apartmentId,
                                     final String projectId, final String buildingId) {
        return delete("id = :id and projectId = :projectId and buildingId = :buildingId",
                Parameters.with("id", apartmentId).and("projectId", projectId)
                        .and(BUILDING_ID, buildingId));
    }

    public List<ApartmentEntity> findApartmentByBuildingId(final String buildingId) {
        return getEntityManager()
                .createQuery("SELECT a FROM ApartmentEntity a WHERE a.buildingId = :buildingId", ApartmentEntity.class)
                .setParameter(BUILDING_ID, buildingId)
                .getResultList();
    }
}

