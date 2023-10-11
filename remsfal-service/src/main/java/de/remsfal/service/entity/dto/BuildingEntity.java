package de.remsfal.service.entity.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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

    @OneToOne(fetch = FetchType.EAGER)
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

    @Override
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

}
