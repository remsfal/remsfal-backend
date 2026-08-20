package de.remsfal.core.model.project;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface RentalAgreementModel {

    UUID getId();

    UUID getProjectId();

    List<? extends TenantModel> getTenants();

    LocalDate getStartOfRental();

    LocalDate getEndOfRental();

    List<? extends RentalAgreementKeysModel> getKeys();

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
        return calculateSum(getCurrentRents(), RentModel::getBasicRent);
    }

    default Float getOperatingCostsPrepayment() {
        return calculateSum(getCurrentRents(), RentModel::getOperatingCostsPrepayment);
    }

    default Float getHeatingCostsPrepayment() {
        return calculateSum(getCurrentRents(), RentModel::getHeatingCostsPrepayment);
    }

    default List<? extends RentModel> getCurrentRents() {
        return getAllRents().stream()
            .collect(Collectors.toMap(
                RentModel::getRentalUnitId,
                Function.identity(),
                (a, b) -> a.getFirstPaymentDate().isAfter(b.getFirstPaymentDate()) ? a : b,
                LinkedHashMap::new))
            .values().stream()
            .toList();
    }

    private Float calculateSum(final List<? extends RentModel> rents,
            final Function<RentModel, Float> extractor) {
        Float sum = rents.stream().map(extractor)
            .filter(v -> v != null).reduce(0.0f, Float::sum);
        return sum > 0 ? sum : null;
    }

}
