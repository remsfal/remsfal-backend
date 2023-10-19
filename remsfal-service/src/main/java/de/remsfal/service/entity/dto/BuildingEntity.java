package de.remsfal.service.entity.dto;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.Objects;

import de.remsfal.core.model.BuildingModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "BUILDING")
public class BuildingEntity extends RentalUnitEntity implements BuildingModel {

    @Id
    @Column(name = "ID", columnDefinition = "char", nullable = false, length = 36)
    private String id;

    @Column(name = "PROPERTY_ID", columnDefinition = "char", nullable = false, updatable = false, length = 36)
    private String propertyId;

    @OneToOne(fetch = FetchType.EAGER, cascade={CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "ADDRESS_ID")
    private AddressEntity address;

    @Column(name = "LIVING_SPACE", columnDefinition = "decimal")
    private Float livingSpace;

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

    public void setAddress(AddressEntity address) {
        this.address = address;
    }

    @Override
    public Float getLivingSpace() {
        return livingSpace;
    }

    public void setLivingSpace(Float livingSpace) {
        this.livingSpace = livingSpace;
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof BuildingEntity e) {
            return super.equals(e)
                && Objects.equals(id, e.id)
                && Objects.equals(propertyId, e.propertyId)
                && Objects.equals(address, e.address)
                && Objects.equals(livingSpace, e.livingSpace)
                && Objects.equals(commercialSpace, e.commercialSpace)
                && Objects.equals(heatingSpace, e.heatingSpace);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static BuildingEntity fromModel(BuildingModel building) {
        if(building == null) {
            return null;
        }
        final BuildingEntity entity = new BuildingEntity();
        entity.setId(building.getId());
        entity.setTitle(building.getTitle());
        entity.setAddress(AddressEntity.fromModel(building.getAddress()));
        entity.setDescription(building.getDescription());
        entity.setLivingSpace(building.getLivingSpace());
        entity.setCommercialSpace(building.getCommercialSpace());
        entity.setUsableSpace(building.getUsableSpace());
        entity.setHeatingSpace(building.getHeatingSpace());
        entity.setRent(building.getRent());
        return entity;
    }

}
