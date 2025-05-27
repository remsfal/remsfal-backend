package de.remsfal.service.entity.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Objects;

import de.remsfal.core.model.project.GarageModel;
import de.remsfal.service.entity.dto.superclass.RentalUnitEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "GARAGE")
public class GarageEntity extends RentalUnitEntity implements GarageModel {

    @Column(name = "BUILDING_ID", columnDefinition = "char", nullable = false, updatable = false, length = 36)
    private String buildingId;

    @Column(name = "LOCATION")
    private String location;

    public String getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }

    @Override
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof GarageEntity e) {
            return super.equals(e)
                && Objects.equals(buildingId, e.buildingId)
                && Objects.equals(location, e.location);
        }
        return false;
    }

    public static GarageEntity fromModel(GarageModel garage) {
        if(garage == null) {
            return null;
        }
        final GarageEntity entity = new GarageEntity();
        entity.setId(garage.getId());
        entity.setTitle(garage.getTitle());
        entity.setLocation(garage.getLocation());
        entity.setDescription(garage.getDescription());
        entity.setUsableSpace(garage.getUsableSpace());
        return entity;
    }

}
