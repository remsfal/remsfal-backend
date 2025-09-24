package de.remsfal.service.entity.dto;

import de.remsfal.core.model.project.BuildingModel;
import de.remsfal.service.entity.dto.superclass.RentalUnitEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "BUILDING")
public class BuildingEntity extends RentalUnitEntity implements BuildingModel {

    @Column(name = "PROPERTY_ID", nullable = false, updatable = false, columnDefinition = "uuid")
    private String propertyId;

    @OneToOne(fetch = FetchType.EAGER, cascade={CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "ADDRESS_ID")
    private AddressEntity address;

    @Column(name = "GROSS_FLOOR_AREA", columnDefinition = "numeric(10,2)")
    private Float grossFloorArea;

    @Column(name = "NET_FLOOR_AREA", columnDefinition = "numeric(10,2)")
    private Float netFloorArea;

    @Column(name = "CONSTRUCTION_FLOOR_AREA", columnDefinition = "numeric(10,2)")
    private Float constructionFloorArea;

    @Column(name = "LIVING_SPACE", columnDefinition = "numeric(10,2)")
    private Float livingSpace;

    @Column(name = "USABLE_SPACE", columnDefinition = "numeric(10,2)")
    private Float usableSpace;

    @Column(name = "HEATING_SPACE", columnDefinition = "numeric(10,2)")
    private Float heatingSpace;

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    @Override
    public AddressEntity getAddress() {
        return address;
    }

    public void setAddress(final AddressEntity address) {
        this.address = address;
    }

    @Override
    public Float getGrossFloorArea() {
        return grossFloorArea;
    }

    public void setGrossFloorArea(Float grossFloorArea) {
        this.grossFloorArea = grossFloorArea;
    }

    @Override
    public Float getNetFloorArea() {
        return netFloorArea;
    }

    public void setNetFloorArea(Float netFloorArea) {
        this.netFloorArea = netFloorArea;
    }

    @Override
    public Float getConstructionFloorArea() {
        return constructionFloorArea;
    }

    public void setConstructionFloorArea(Float constructionFloorArea) {
        this.constructionFloorArea = constructionFloorArea;
    }

    @Override
    public Float getLivingSpace() {
        return livingSpace;
    }

    public void setLivingSpace(final Float livingSpace) {
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

    public void setHeatingSpace(final Float heatingSpace) {
        this.heatingSpace = heatingSpace;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof BuildingEntity e) {
            return super.equals(e)
                && Objects.equals(propertyId, e.propertyId)
                && Objects.equals(address, e.address)
                && Objects.equals(grossFloorArea, e.grossFloorArea)
                && Objects.equals(netFloorArea, e.netFloorArea)
                && Objects.equals(constructionFloorArea, e.constructionFloorArea)
                && Objects.equals(livingSpace, e.livingSpace)
                && Objects.equals(usableSpace, e.usableSpace)
                && Objects.equals(heatingSpace, e.heatingSpace);
        }
        return false;
    }

}
