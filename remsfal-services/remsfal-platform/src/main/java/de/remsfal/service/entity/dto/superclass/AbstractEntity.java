package de.remsfal.service.entity.dto.superclass;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@MappedSuperclass
public abstract class AbstractEntity extends MetaDataEntity {

    @Id
    @Column(name = "ID", nullable = false, columnDefinition = "uuid")
    protected UUID id;

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void generateId() {
        setId(UUID.randomUUID());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof AbstractEntity e) {
            return super.equals(e)
                && Objects.equals(id, e.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
