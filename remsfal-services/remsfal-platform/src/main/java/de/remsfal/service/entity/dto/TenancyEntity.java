package de.remsfal.service.entity.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import de.remsfal.core.model.project.TenancyModel;
import de.remsfal.service.entity.dto.superclass.AbstractEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "tenancies")
public class TenancyEntity extends AbstractEntity implements TenancyModel {

    @Column(name = "project_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID projectId;

    @ManyToOne
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private ProjectEntity project;

    @OneToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "tenants",
        joinColumns = @JoinColumn(name = "tenancy_id", columnDefinition = "uuid"),
        inverseJoinColumns = @JoinColumn(name = "user_id", columnDefinition = "uuid")
    )
    private List<UserEntity> tenants = new ArrayList<>();

    @Column(name = "start_of_rental", columnDefinition = "date")
    private LocalDate startOfRental;

    @Column(name = "end_of_rental", columnDefinition = "date")
    private LocalDate endOfRental;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tenancy_id", nullable = false, columnDefinition = "uuid")
    @OrderBy("firstPaymentDate")
    private List<PropertyRentEntity> propertyRent;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tenancy_id", nullable = false, columnDefinition = "uuid")
    @OrderBy("firstPaymentDate")
    private List<SiteRentEntity> siteRent;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tenancy_id", nullable = false, columnDefinition = "uuid")
    @OrderBy("firstPaymentDate")
    private List<BuildingRentEntity> buildingRent;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tenancy_id", nullable = false, columnDefinition = "uuid")
    @OrderBy("firstPaymentDate")
    private List<ApartmentRentEntity> apartmentRent;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tenancy_id", nullable = false, columnDefinition = "uuid")
    @OrderBy("firstPaymentDate")
    private List<StorageRentEntity> storageRent;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tenancy_id", nullable = false, columnDefinition = "uuid")
    @OrderBy("firstPaymentDate")
    private List<CommercialRentEntity> commercialRent;

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(final UUID projectId) {
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
