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
@Table(name = "site_rents")
public class SiteRentEntity extends RentEntity {

    @Column(name = "SITE_ID", nullable = false, updatable = false, columnDefinition = "uuid")
    private String siteId;

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(final String siteId) {
        this.siteId = siteId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(siteId, getTenancyId());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof SiteRentEntity e) {
            return super.equals(e)
                && Objects.equals(siteId, e.siteId);
        }
        return false;
    }

}
