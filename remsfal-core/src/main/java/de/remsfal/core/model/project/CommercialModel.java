package de.remsfal.core.model.project;

import org.immutables.value.Value;

import jakarta.annotation.Nullable;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface CommercialModel extends RentalUnitModel {

    @Value.Default
    @Override
    default UnitType getType() {
        return UnitType.COMMERCIAL;
    }

    @Nullable
    Float getNetFloorArea(); // Netto-Raumfläche (NRF) nach DIN 277

    @Nullable
    Float getUsableFloorArea(); // Nutzungsfläche (NUF) nach DIN 277

    @Nullable
    Float getTechnicalServicesArea(); // Technikfläche (TF) nach DIN 277

    @Nullable
    Float getTrafficArea(); // Verkehrsfläche (VF) nach DIN 277

    @Nullable
    Float getHeatingSpace();

    @Value.Derived
    @Nullable
    @Override
    default Float getSpace() {
        return getNetFloorArea();
    }

}
