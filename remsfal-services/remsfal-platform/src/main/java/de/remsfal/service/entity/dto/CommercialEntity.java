package de.remsfal.service.entity.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

import de.remsfal.core.model.project.CommercialModel;
import de.remsfal.service.entity.dto.superclass.RentalUnitEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "commercials")
public class CommercialEntity extends RentalUnitEntity implements CommercialModel {

    @Column(name = "BUILDING_ID", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID buildingId;

    @Column(name = "NET_FLOOR_AREA", columnDefinition = "numeric(10,2)")
    private Float netFloorArea;

    @Column(name = "USABLE_FLOOR_AREA", columnDefinition = "numeric(10,2)")
    private Float usableFloorArea;

    @Column(name = "TECHNICAL_SERVICE_AREA", columnDefinition = "numeric(10,2)")
    private Float technicalServicesArea;

    @Column(name = "TRAFFIC_AREA", columnDefinition = "numeric(10,2)")
    private Float trafficArea;

    @Column(name = "HEATING_SPACE", columnDefinition = "numeric(10,2)")
    private Float heatingSpace;

    public UUID getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(final UUID buildingId) {
        this.buildingId = buildingId;
    }

    @Override
    public Float getNetFloorArea() {
        return netFloorArea;
    }

    public void setNetFloorArea(final Float netFloorArea) {
        this.netFloorArea = netFloorArea;
    }

    @Override
    public Float getUsableFloorArea() {
        return usableFloorArea;
    }

    public void setUsableFloorArea(final Float usableFloorArea) {
        this.usableFloorArea = usableFloorArea;
    }

    @Override
    public Float getTechnicalServicesArea() {
        return technicalServicesArea;
    }

    public void setTechnicalServicesArea(final Float technicalServicesArea) {
        this.technicalServicesArea = technicalServicesArea;
    }

    @Override
    public Float getTrafficArea() {
        return trafficArea;
    }

    public void setTrafficArea(final Float trafficArea) {
        this.trafficArea = trafficArea;
    }

    @Override
    public Float getHeatingSpace() {
        return heatingSpace;
    }

    public void setHeatingSpace(final Float heatingSpace) {
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
