package de.remsfal.core.model.project;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface RentalAgreementModel {

    UUID getId();

    List<? extends TenantModel> getTenants();

    LocalDate getStartOfRental();

    LocalDate getEndOfRental();

    List<? extends RentModel> getPropertyRents();

    List<? extends RentModel> getSiteRents();

    List<? extends RentModel> getBuildingRents();

    List<? extends RentModel> getApartmentRents();

    List<? extends RentModel> getStorageRents();

    List<? extends RentModel> getCommercialRents();

    default List<? extends RentModel> getAllRents() {
        return Stream.of(
            getPropertyRents(), getSiteRents(), getBuildingRents(),
            getApartmentRents(), getStorageRents(), getCommercialRents()
        ).filter(Objects::nonNull).flatMap(List::stream).toList();
    }

    default Float getBasicRent() {
        return calculateSum(getActiveRents(), RentModel::getBasicRent);
    }

    default Float getOperatingCostsPrepayment() {
        return calculateSum(getActiveRents(), RentModel::getOperatingCostsPrepayment);
    }

    default Float getHeatingCostsPrepayment() {
        return calculateSum(getActiveRents(), RentModel::getHeatingCostsPrepayment);
    }

    private List<? extends RentModel> getActiveRents() {
        return getAllRents().stream().filter(this::isActiveRent).toList();
    }

    private boolean isActiveRent(final RentModel rent) {
        return rent.getLastPaymentDate() == null
            || rent.getLastPaymentDate().isAfter(LocalDate.now());
    }

    private Float calculateSum(final List<? extends RentModel> rents,
            final Function<RentModel, Float> extractor) {
        Float sum = rents.stream().map(extractor)
            .filter(v -> v != null).reduce(0.0f, Float::sum);
        return sum > 0 ? sum : null;
    }

    public default Boolean isActive() {
        if (this.getEndOfRental() == null) {
            return true;
        } else {
            return this.getEndOfRental().isAfter(LocalDate.now());
        }
    }

}
