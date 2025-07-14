package de.remsfal.service.entity.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import de.remsfal.core.model.project.TenancyModel;
import de.remsfal.service.entity.dto.superclass.AbstractEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "TENANCY")
public class TenancyEntity extends AbstractEntity implements TenancyModel {

    @Column(name = "PROJECT_ID", columnDefinition = "char", nullable = false, updatable = false, length = 36)
    private String projectId;

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "TENANT",
        joinColumns = @JoinColumn(name = "TENANCY_ID"),
        inverseJoinColumns = @JoinColumn(name = "USER_ID")
    )
    private List<UserEntity> tenants;

    @Column(name = "START_OF_RENTAL", columnDefinition = "date")
    private LocalDate startOfRental;

    @Column(name = "END_OF_RENTAL", columnDefinition = "date")
    private LocalDate endOfRental;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "TENANCY_ID", nullable = false)
    @OrderBy("firstPaymentDate")
    private List<PropertyRentEntity> propertyRent;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "TENANCY_ID", nullable = false)
    @OrderBy("firstPaymentDate")
    private List<SiteRentEntity> siteRent;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "TENANCY_ID", nullable = false)
    @OrderBy("firstPaymentDate")
    private List<BuildingRentEntity> buildingRent;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "TENANCY_ID", nullable = false)
    @OrderBy("firstPaymentDate")
    private List<ApartmentRentEntity> apartmentRent;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "TENANCY_ID", nullable = false)
    @OrderBy("firstPaymentDate")
    private List<StorageRentEntity> storageRent;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "TENANCY_ID", nullable = false)
    @OrderBy("firstPaymentDate")
    private List<CommercialRentEntity> commercialRent;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    @Override
    public List<UserEntity> getTenants() {
        return tenants;
    }

    public void setTenants(final List<UserEntity> tenants) {
        this.tenants = tenants;
    }

    @Override
    public LocalDate getStartOfRental() {
        return startOfRental;
    }

    public void setStartOfRental(final LocalDate startOfRental) {
        this.startOfRental = startOfRental;
    }

    @Override
    public LocalDate getEndOfRental() {
        return endOfRental;
    }

    public void setEndOfRental(final LocalDate endOfRental) {
        this.endOfRental = endOfRental;
    }

    public List<PropertyRentEntity> getPropertyRent() {
        return propertyRent;
    }

    public void setPropertyRent(final List<PropertyRentEntity> propertyRent) {
        this.propertyRent = propertyRent;
    }

    public List<SiteRentEntity> getSiteRent() {
        return siteRent;
    }

    public void setSiteRent(final List<SiteRentEntity> siteRent) {
        this.siteRent = siteRent;
    }

    public List<BuildingRentEntity> getBuildingRent() {
        return buildingRent;
    }

    public void setBuildingRent(final List<BuildingRentEntity> buildingRent) {
        this.buildingRent = buildingRent;
    }

    public List<ApartmentRentEntity> getApartmentRent() {
        return apartmentRent;
    }

    public void setApartmentRent(final List<ApartmentRentEntity> apartmentRent) {
        this.apartmentRent = apartmentRent;
    }

    public List<StorageRentEntity> getStorageRent() {
        return storageRent;
    }

    public void setStorageRent(final List<StorageRentEntity> storageRent) {
        this.storageRent = storageRent;
    }

    public List<CommercialRentEntity> getCommercialRent() {
        return commercialRent;
    }

    public void setCommercialRent(final List<CommercialRentEntity> commercialRent) {
        this.commercialRent = commercialRent;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof TenancyEntity e) {
            return super.equals(e)
                && Objects.equals(projectId, e.projectId)
                && Objects.equals(tenants, e.tenants)
                && Objects.equals(startOfRental, e.startOfRental)
                && Objects.equals(endOfRental, e.endOfRental)
                && Objects.equals(propertyRent, e.propertyRent)
                && Objects.equals(siteRent, e.siteRent)
                && Objects.equals(buildingRent, e.buildingRent)
                && Objects.equals(apartmentRent, e.apartmentRent)
                && Objects.equals(storageRent, e.storageRent)
                && Objects.equals(commercialRent, e.commercialRent);
        }
        return false;
    }

}
