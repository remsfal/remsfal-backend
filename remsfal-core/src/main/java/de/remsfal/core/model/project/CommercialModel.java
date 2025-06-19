package de.remsfal.core.model.project;

import org.immutables.value.Value;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface CommercialModel extends RentalUnitModel {

    @Value.Default
    @Override
    default UnitType getType() {
        return UnitType.COMMERCIAL;
    }

    @PositiveOrZero
    @Nullable
    Float getNetFloorArea(); // Netto-Raumfläche (NRF) nach DIN 277

    @PositiveOrZero
    @Nullable
    Float getUsableFloorArea(); // Nutzungsfläche (NUF) nach DIN 277

    @PositiveOrZero
    @Nullable
    Float getTechnicalServicesArea(); // Technikfläche (TF) nach DIN 277

    @PositiveOrZero
    @Nullable
    Float getTrafficArea(); // Verkehrsfläche (VF) nach DIN 277

    @PositiveOrZero
    @Nullable
    Float getHeatingSpace();

    @Value.Derived
    @Nullable
    @Override
    default Float getSpace() {
        return getNetFloorArea();
    }

}
