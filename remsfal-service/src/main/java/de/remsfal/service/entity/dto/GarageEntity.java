package de.remsfal.service.entity.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

import de.remsfal.core.model.GarageModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "GARAGE")
public class GarageEntity extends RentalUnitEntity implements GarageModel {

    @Id
    @Column(name = "ID", columnDefinition = "char", nullable = false, length = 36)
    private String id;
    
    @Column(name = "BUILDING_ID", columnDefinition = "char", nullable = false, updatable = false, length = 36)
    private String buildingId;

    @Column(name = "LOCATION")
    private String location;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

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
                && Objects.equals(id, e.id)
                && Objects.equals(buildingId, e.buildingId)
                && Objects.equals(location, e.location);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static GarageEntity fromModel(GarageModel garage) {
        final GarageEntity entity = new GarageEntity();
        entity.setId(garage.getId());
        entity.setTitle(garage.getTitle());
        entity.setLocation(garage.getLocation());
        entity.setDescription(garage.getDescription());
        entity.setUsableSpace(garage.getUsableSpace());
        entity.setRent(garage.getRent());
        return entity;
    }

}
