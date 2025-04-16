package de.remsfal.service.entity.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import de.remsfal.core.model.project.TenancyModel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "TENANCY_ID", referencedColumnName="ID", nullable = false)
    @OrderBy("firstPaymentDate")
    private List<RentEntity> rent;

    @OneToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "USER_ID")
    private UserEntity tenant;

    @Column(name = "START_OF_RMENTAL", columnDefinition = "date")
    private LocalDate startOfRental;

    @Column(name = "END_OF_RENTAL", columnDefinition = "date")
    private LocalDate endOfRental;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Override
    public List<RentEntity> getRent() {
        return rent;
    }

    public void setRent(List<RentEntity> rent) {
        this.rent = rent;
    }

    @Override
    public UserEntity getTenant() {
        return tenant;
    }

    public void setTenant(UserEntity tenant) {
        this.tenant = tenant;
    }

    @Override
    public LocalDate getStartOfRental() {
        return startOfRental;
    }

    public void setStartOfRental(LocalDate startOfRental) {
        this.startOfRental = startOfRental;
    }

    @Override
    public LocalDate getEndOfRental() {
        return endOfRental;
    }

    public void setEndOfRental(LocalDate endOfRental) {
        this.endOfRental = endOfRental;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof TenancyEntity e) {
            return super.equals(e)
                && Objects.equals(id, e.id)
                && Objects.equals(rent, e.rent)
                && Objects.equals(tenant, e.tenant)
                && Objects.equals(startOfRental, e.startOfRental)
                && Objects.equals(endOfRental, e.endOfRental);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
