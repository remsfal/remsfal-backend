package de.remsfal.service.entity.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import de.remsfal.core.model.SiteModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "SITE")
public class SiteEntity extends RentalUnitEntity implements SiteModel {

    @Id
    @Column(name = "ID", columnDefinition = "char", nullable = false, length = 36)
    private String id;

    @Column(name = "PROPERTY_ID", columnDefinition = "char", nullable = false, updatable = false, length = 36)
    private String propertyId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ADDRESS_ID")
    private AddressEntity address;

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

}
