package de.remsfal.service.entity.dto;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.Objects;

import de.remsfal.core.model.project.SiteModel;

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

    @OneToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE})
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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof SiteEntity e) {
            return super.equals(e)
                && Objects.equals(id, e.id)
                && Objects.equals(propertyId, e.propertyId)
                && Objects.equals(address, e.address);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static SiteEntity fromModel(SiteModel site) {
        if(site == null) {
            return null;
        }
        final SiteEntity entity = new SiteEntity();
        entity.setId(site.getId());
        entity.setTitle(site.getTitle());
        entity.setAddress(AddressEntity.fromModel(site.getAddress()));
        entity.setDescription(site.getDescription());
        entity.setUsableSpace(site.getUsableSpace());
        return entity;
    }

}
