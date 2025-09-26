package de.remsfal.service.entity.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Objects;

import de.remsfal.core.model.project.ApartmentModel;
import de.remsfal.service.entity.dto.superclass.RentalUnitEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "apartments")
public class ApartmentEntity extends RentalUnitEntity implements ApartmentModel {

    @Column(name = "BUILDING_ID", nullable = false, updatable = false, columnDefinition = "uuid")
    private String buildingId;

    @Column(name = "LIVING_SPACE", columnDefinition = "numeric(10,2)")
    private Float livingSpace;

    @Column(name = "USABLE_SPACE", columnDefinition = "numeric(10,2)")
    private Float usableSpace;

    @Column(name = "HEATING_SPACE", columnDefinition = "numeric(10,2)")
    private Float heatingSpace;

    public String getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }

    @Override
    public Float getLivingSpace() {
        return livingSpace;
    }

    public void setLivingSpace(Float livingSpace) {
        this.livingSpace = livingSpace;
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
        if (o instanceof ApartmentEntity e) {
            return super.equals(e)
                && Objects.equals(buildingId, e.buildingId)
                && Objects.equals(livingSpace, e.livingSpace)
                && Objects.equals(usableSpace, e.usableSpace)
                && Objects.equals(heatingSpace, e.heatingSpace);
        }
        return false;
    }

}
