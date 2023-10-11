package de.remsfal.service.entity.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import de.remsfal.core.model.CommercialModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "COMMERCIAL")
public class CommercialEntity extends RentalUnitEntity implements CommercialModel {

    @Id
    @Column(name = "ID", columnDefinition = "char", nullable = false, length = 36)
    private String id;

    @Column(name = "BUILDING_ID", columnDefinition = "char", nullable = false, updatable = false, length = 36)
    private String buildingId;

    @Column(name = "LOCATION")
    private String location;

    @Column(name = "COMMERCIAL_SPACE", columnDefinition = "decimal")
    private Float commercialSpace;

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
    public Float getCommercialSpace() {
        return commercialSpace;
    }

    public void setCommercialSpace(Float commercialSpace) {
        this.commercialSpace = commercialSpace;
    }

    @Override
    public Float getHeatingSpace() {
        return heatingSpace;
    }

    public void setHeatingSpace(Float heatingSpace) {
        this.heatingSpace = heatingSpace;
    }

}
