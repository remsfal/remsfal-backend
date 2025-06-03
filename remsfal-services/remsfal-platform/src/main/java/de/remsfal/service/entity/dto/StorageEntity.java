package de.remsfal.service.entity.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Objects;

import de.remsfal.core.model.project.StorageModel;
import de.remsfal.service.entity.dto.superclass.RentalUnitEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "STORAGE")
public class StorageEntity extends RentalUnitEntity implements StorageModel {

    @Column(name = "BUILDING_ID", columnDefinition = "char", nullable = false, updatable = false, length = 36)
    private String buildingId;

    @Column(name = "USABLE_SPACE", columnDefinition = "decimal")
    private Float usableSpace;

    @Column(name = "HEATING_SPACE", columnDefinition = "decimal")
    private Float heatingSpace;


    public String getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }

    @Override
    public Float getUsableSpace() {
        return usableSpace;
    }

    public void setUsableSpace(Float usableSpace) {
        this.usableSpace = usableSpace;
    }

    @Override
    public Float getHeatingSpace() {
        return heatingSpace;
    }

    public void setHeatingSpace(Float heatingSpace) {
        this.heatingSpace = heatingSpace;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof StorageEntity e) {
            return super.equals(e)
                && Objects.equals(buildingId, e.buildingId)
                && Objects.equals(usableSpace, e.usableSpace)
                && Objects.equals(heatingSpace, e.heatingSpace);
        }
        return false;
    }

    @Deprecated
    public static StorageEntity fromModel(StorageModel garage) {
        if(garage == null) {
            return null;
        }
        final StorageEntity entity = new StorageEntity();
        entity.setId(garage.getId());
        entity.setTitle(garage.getTitle());
        entity.setLocation(garage.getLocation());
        entity.setDescription(garage.getDescription());
        entity.setUsableSpace(garage.getUsableSpace());
        return entity;
    }

}
