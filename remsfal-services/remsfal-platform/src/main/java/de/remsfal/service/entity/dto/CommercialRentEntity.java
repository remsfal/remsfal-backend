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
@Table(name = "commercial_rents")
public class CommercialRentEntity extends RentEntity {

    @Column(name = "COMMERCIAL_ID", nullable = false, updatable = false, columnDefinition = "uuid")
    private String commercialId;

    public String getCommercialId() {
        return commercialId;
    }

    public void setCommercialId(final String commercialId) {
        this.commercialId = commercialId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(commercialId, getTenancyId());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof CommercialRentEntity e) {
            return super.equals(e)
                && Objects.equals(commercialId, e.commercialId);
        }
        return false;
    }

}
