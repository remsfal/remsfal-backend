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
@Table(name = "commercial_rents")
public class CommercialRentEntity extends RentEntity {

    @Column(name = "commercial_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID commercialId;

    public UUID getCommercialId() {
        return commercialId;
    }

    public void setCommercialId(final UUID commercialId) {
        this.commercialId = commercialId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(commercialId, getAgreementId());
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
