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
@Table(name = "APARTMENT_RENT")
public class ApartmentRentEntity extends RentEntity {

    @Column(name = "APARTMENT_ID", columnDefinition = "char", nullable = false, updatable = false, length = 36)
    private String apartmentId;

    public String getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(final String apartmentId) {
        this.apartmentId = apartmentId;
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
