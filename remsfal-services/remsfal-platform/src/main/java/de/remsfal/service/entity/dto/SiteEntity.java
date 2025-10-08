package de.remsfal.service.entity.dto;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

import de.remsfal.core.model.project.SiteModel;
import de.remsfal.service.entity.dto.superclass.RentalUnitEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "sites")
public class SiteEntity extends RentalUnitEntity implements SiteModel {

    @Column(name = "property_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID propertyId;

    @OneToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "address_id", columnDefinition = "uuid")
    private AddressEntity address;

    @Column(name = "outdoor_area", columnDefinition = "numeric(10,2)")
    private Float outdoorArea;

    public UUID getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(final UUID propertyId) {
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
    public Float getOutdoorArea() {
        return outdoorArea;
    }

    public void setOutdoorArea(final Float outdoorArea) {
        this.outdoorArea = outdoorArea;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof SiteEntity e) {
            return super.equals(e)
                && Objects.equals(propertyId, e.propertyId)
                && Objects.equals(address, e.address)
                && Objects.equals(outdoorArea, e.outdoorArea);
        }
        return false;
    }

}
