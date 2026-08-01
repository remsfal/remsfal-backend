package de.remsfal.service.entity.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

import de.remsfal.core.model.project.StorageModel;
import de.remsfal.service.entity.dto.superclass.RentalUnitEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "storages")
public class StorageEntity extends RentalUnitEntity implements StorageModel {

    @Column(name = "building_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID buildingId;

    @Column(name = "usable_space", columnDefinition = "numeric(10,2)")
    private Float usableSpace;

    @Column(name = "heating_space", columnDefinition = "numeric(10,2)")
    private Float heatingSpace;

    @Column(name = "is_heated", nullable = false)
    private Boolean isHeated = false;


    public UUID getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(final UUID buildingId) {
        this.buildingId = buildingId;
    }

    @Override
    public Float getUsableSpace() {
        return usableSpace;
    }

    public void setUsableSpace(final Float usableSpace) {
        this.usableSpace = usableSpace;
    }

    @Override
    public Float getHeatingSpace() {
        return heatingSpace;
    }

    public void setHeatingSpace(final Float heatingSpace) {
        this.heatingSpace = heatingSpace;
    }

    @Override
    public Boolean isHeated() {
        return isHeated;
    }

    public void setHeated(final Boolean heated) {
        this.isHeated = heated;
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
                && Objects.equals(heatingSpace, e.heatingSpace)
                && Objects.equals(isHeated, e.isHeated);
        }
        return false;
    }

}
