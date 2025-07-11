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
@Table(name = "STORAGE_RENT")
public class StorageRentEntity extends RentEntity {

    @Column(name = "STORAGE_ID", columnDefinition = "char", nullable = false, updatable = false, length = 36)
    private String storageId;

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(final String storageId) {
        this.storageId = storageId;
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
