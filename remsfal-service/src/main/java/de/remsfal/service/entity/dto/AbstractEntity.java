package de.remsfal.service.entity.dto;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@MappedSuperclass
public abstract class AbstractEntity {

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
        if (!(o instanceof AbstractEntity)) {
            return false;
        }
        final AbstractEntity that = (AbstractEntity) o;
        return Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdAt);
    }

}
