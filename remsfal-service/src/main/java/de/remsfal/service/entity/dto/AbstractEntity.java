package de.remsfal.service.entity.dto;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@MappedSuperclass
public abstract class AbstractEntity {

    public abstract void setId(String id);
    
    public void generateId() {
        setId(UUID.randomUUID().toString());
    }

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Version
    @Column(name = "MODIFIED_AT", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedAt;

    @PrePersist
    public void created() {
        final Date now = new Date();
        createdAt = now;
        modifiedAt = now;
    }

    @PreUpdate
    public void modified() {
        modifiedAt = new Date();
    }

    public Date getCreatedAt() {
        return cloneDate(createdAt);
    }

    public Date getModifiedAt() {
        return cloneDate(modifiedAt);
    }

    private Date cloneDate(final Date date) {
        return (Date) Optional.ofNullable(date)
            .map(Date::clone)
            .orElse(null);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof AbstractEntity e) {
            return Objects.equals(createdAt, e.createdAt);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdAt);
    }

}
