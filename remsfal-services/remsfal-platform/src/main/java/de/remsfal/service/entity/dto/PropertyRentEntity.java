package de.remsfal.service.entity.dto;

import java.util.Objects;
import java.util.UUID;

import de.remsfal.service.entity.dto.superclass.RentEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "property_rents")
public class PropertyRentEntity extends RentEntity {

    @Column(name = "property_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID propertyId;

    public UUID getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(final UUID propertyId) {
        this.propertyId = propertyId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyId, getAgreementId());
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
