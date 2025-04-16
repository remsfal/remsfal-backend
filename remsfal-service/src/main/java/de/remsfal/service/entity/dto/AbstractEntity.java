package de.remsfal.service.entity.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@MappedSuperclass
public abstract class AbstractEntity extends AbstractMetaDataEntity {

    @Id
    @Column(name = "ID", columnDefinition = "char", nullable = false, length = 36)
    protected String id;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void generateId() {
        setId(UUID.randomUUID().toString());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof AbstractEntity e) {
            return Objects.equals(id, e.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
