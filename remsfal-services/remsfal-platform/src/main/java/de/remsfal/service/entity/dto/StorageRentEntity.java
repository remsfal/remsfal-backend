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
@Table(name = "storage_rents")
public class StorageRentEntity extends RentEntity {

    @Column(name = "STORAGE_ID", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID storageId;

    public UUID getStorageId() {
        return storageId;
    }

    public void setStorageId(final UUID storageId) {
        this.storageId = storageId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(storageId, getTenancyId());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof StorageRentEntity e) {
            return super.equals(e)
                && Objects.equals(storageId, e.storageId);
        }
        return false;
    }

}
