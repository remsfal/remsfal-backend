package de.remsfal.service.entity.dto;

import java.util.Objects;
import java.util.UUID;

import de.remsfal.service.entity.dto.superclass.RentEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "building_rents")
public class BuildingRentEntity extends RentEntity {

    @Id
    @Column(name = "building_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID buildingId;

    @Override
    public UUID getUnitId() {
        return buildingId;
    }

    public UUID getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(final UUID buildingId) {
        this.buildingId = buildingId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(buildingId, getFirstPaymentDate());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof BuildingRentEntity e) {
            return super.equals(e)
                && Objects.equals(buildingId, e.buildingId);
        }
        return false;
    }

}
