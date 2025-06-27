package de.remsfal.core.model.project;

import org.immutables.value.Value;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface StorageModel extends RentalUnitModel {

    @Value.Default
    @Override
    default UnitType getType() {
        return UnitType.STORAGE;
    }

    @PositiveOrZero
    @Nullable
    Float getUsableSpace(); // Nutzfläche nach Wohnflächenverordnung - WoFlV

    @PositiveOrZero
    @Nullable
    Float getHeatingSpace();

    @Value.Derived
    @Nullable
    @Override
    default Float getSpace() {
        return getUsableSpace();
    }

}
