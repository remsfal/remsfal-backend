package de.remsfal.service.entity.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Objects;

import de.remsfal.core.model.project.CommercialModel;
import de.remsfal.service.entity.dto.superclass.RentalUnitEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "COMMERCIAL")
public class CommercialEntity extends RentalUnitEntity implements CommercialModel {

    @Column(name = "BUILDING_ID", columnDefinition = "char", nullable = false, updatable = false, length = 36)
    private String buildingId;

    @Column(name = "NET_FLOOR_AREA", columnDefinition = "decimal")
    private Float netFloorArea;

    @Column(name = "USABLE_FLOOR_AREA", columnDefinition = "decimal")
    private Float usableFloorArea;

    @Column(name = "TECHNICAL_SERVICE_AREA", columnDefinition = "decimal")
    private Float technicalServicesArea;

    @Column(name = "TRAFFIC_AREA", columnDefinition = "decimal")
    private Float trafficArea;

    @Column(name = "HEATING_SPACE", columnDefinition = "decimal")
    private Float heatingSpace;

    public String getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }

    @Override
    public Float getNetFloorArea() {
        return netFloorArea;
    }

    public void setNetFloorArea(Float netFloorArea) {
        this.netFloorArea = netFloorArea;
    }

    @Override
    public Float getUsableFloorArea() {
        return usableFloorArea;
    }

    public void setUsableFloorArea(Float usableFloorArea) {
        this.usableFloorArea = usableFloorArea;
    }

    @Override
    public Float getTechnicalServicesArea() {
        return technicalServicesArea;
    }

    public void setTechnicalServicesArea(Float technicalServicesArea) {
        this.technicalServicesArea = technicalServicesArea;
    }

    @Override
    public Float getTrafficArea() {
        return trafficArea;
    }

    public void setTrafficArea(Float trafficArea) {
        this.trafficArea = trafficArea;
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
        if (o instanceof CommercialEntity e) {
            return super.equals(e)
                && Objects.equals(buildingId, e.buildingId)
                && Objects.equals(netFloorArea, e.netFloorArea)
                && Objects.equals(usableFloorArea, e.usableFloorArea)
                && Objects.equals(technicalServicesArea, e.technicalServicesArea)
                && Objects.equals(trafficArea, e.trafficArea)
                && Objects.equals(heatingSpace, e.heatingSpace);
        }
        return false;
    }

}
