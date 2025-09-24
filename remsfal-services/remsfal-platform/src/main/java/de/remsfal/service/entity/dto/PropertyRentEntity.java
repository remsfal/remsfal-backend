package de.remsfal.service.entity.dto;

import java.util.Objects;

import de.remsfal.service.entity.dto.superclass.RentEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "PROPERTY_RENT")
public class PropertyRentEntity extends RentEntity {

    @Column(name = "PROPERTY_ID", nullable = false, updatable = false, columnDefinition = "uuid")
    private String propertyId;

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(final String propertyId) {
        this.propertyId = propertyId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyId, getTenancyId());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof PropertyRentEntity e) {
            return super.equals(e)
                && Objects.equals(propertyId, e.propertyId);
        }
        return false;
    }

}
