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
@Table(name = "apartment_rents")
public class ApartmentRentEntity extends RentEntity {

    @Column(name = "apartment_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID apartmentId;

    public UUID getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(final UUID apartmentId) {
        this.apartmentId = apartmentId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(apartmentId, getTenancyId());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ApartmentRentEntity e) {
            return super.equals(e)
                && Objects.equals(apartmentId, e.apartmentId);
        }
        return false;
    }

}
