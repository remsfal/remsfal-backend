package de.remsfal.service.entity.dto;

import java.util.Objects;

import de.remsfal.service.entity.dto.superclass.RentEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "BUILDING_RENT")
public class BuildingRentEntity extends RentEntity {

    @Column(name = "BUILDING_ID", columnDefinition = "char", nullable = false, updatable = false, length = 36)
    private String buildingId;

    public String getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(final String buildingId) {
        this.buildingId = buildingId;
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
