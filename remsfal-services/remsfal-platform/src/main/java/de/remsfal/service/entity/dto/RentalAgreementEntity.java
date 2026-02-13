package de.remsfal.service.entity.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import de.remsfal.core.model.project.RentalAgreementModel;
import de.remsfal.service.entity.dto.superclass.AbstractEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "rental_agreements")
public class RentalAgreementEntity extends AbstractEntity implements RentalAgreementModel {

    @Column(name = "project_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID projectId;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(
        name = "rental_agreement_tenants",
        joinColumns = @JoinColumn(name = "rental_agreement_id"),
        inverseJoinColumns = @JoinColumn(name = "tenant_id")
    )
    @OrderBy("lastName, firstName")
    private List<TenantEntity> tenants = new ArrayList<>();

    @Column(name = "start_of_rental", columnDefinition = "date")
    private LocalDate startOfRental;

    @Column(name = "end_of_rental", columnDefinition = "date")
    private LocalDate endOfRental;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "agreement_id", nullable = false, columnDefinition = "uuid")
    @OrderBy("firstPaymentDate")
    private List<PropertyRentEntity> propertyRent;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "agreement_id", nullable = false, columnDefinition = "uuid")
    @OrderBy("firstPaymentDate")
    private List<SiteRentEntity> siteRent;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "agreement_id", nullable = false, columnDefinition = "uuid")
    @OrderBy("firstPaymentDate")
    private List<BuildingRentEntity> buildingRent;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "agreement_id", nullable = false, columnDefinition = "uuid")
    @OrderBy("firstPaymentDate")
    private List<ApartmentRentEntity> apartmentRent;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "agreement_id", nullable = false, columnDefinition = "uuid")
    @OrderBy("firstPaymentDate")
    private List<StorageRentEntity> storageRent;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "agreement_id", nullable = false, columnDefinition = "uuid")
    @OrderBy("firstPaymentDate")
    private List<CommercialRentEntity> commercialRent;

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(final UUID projectId) {
        this.projectId = projectId;
    }

    @Override
    public List<TenantEntity> getTenants() {
        return tenants;
    }

    public void setTenants(final List<TenantEntity> tenants) {
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

    @Override
    public List<PropertyRentEntity> getPropertyRents() {
        return propertyRent;
    }

    public void setPropertyRents(final List<PropertyRentEntity> propertyRent) {
        this.propertyRent = propertyRent;
    }

    @Override
    public List<SiteRentEntity> getSiteRents() {
        return siteRent;
    }

    public void setSiteRents(final List<SiteRentEntity> siteRent) {
        this.siteRent = siteRent;
    }

    @Override
    public List<BuildingRentEntity> getBuildingRents() {
        return buildingRent;
    }

    public void setBuildingRents(final List<BuildingRentEntity> buildingRent) {
        this.buildingRent = buildingRent;
    }

    @Override
    public List<ApartmentRentEntity> getApartmentRents() {
        return apartmentRent;
    }

    public void setApartmentRents(final List<ApartmentRentEntity> apartmentRent) {
        this.apartmentRent = apartmentRent;
    }

    @Override
    public List<StorageRentEntity> getStorageRents() {
        return storageRent;
    }

    public void setStorageRents(final List<StorageRentEntity> storageRent) {
        this.storageRent = storageRent;
    }

    @Override
    public List<CommercialRentEntity> getCommercialRents() {
        return commercialRent;
    }

    public void setCommercialRents(final List<CommercialRentEntity> commercialRent) {
        this.commercialRent = commercialRent;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof RentalAgreementEntity e) {
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
