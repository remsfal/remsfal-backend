package de.remsfal.service.entity.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import de.remsfal.core.model.ApartmentModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "APARTMENT")
public class ApartmentEntity extends RentalUnitEntity implements ApartmentModel {

    @Id
    @Column(name = "ID", columnDefinition = "char", nullable = false, length = 36)
    private String id;

    @Column(name = "BUILDING_ID", columnDefinition = "char", nullable = false, updatable = false, length = 36)
    private String buildingId;

    @Column(name = "LOCATION")
    private String location;

    @Column(name = "LIVING_SPACE", columnDefinition = "decimal")
    private Float livingSpace;

    @Column(name = "HEATING_SPACE", columnDefinition = "decimal")
    private Float heatingSpace;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
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
    public Float getLivingSpace() {
        return livingSpace;
    }

    public void setLivingSpace(Float livingSpace) {
        this.livingSpace = livingSpace;
    }

    @Override
    public Float getHeatingSpace() {
        return heatingSpace;
    }

    public void setHeatingSpace(Float heatingSpace) {
        this.heatingSpace = heatingSpace;
    }

}
