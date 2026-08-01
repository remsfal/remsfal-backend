package de.remsfal.core.model.project;

import org.immutables.value.Value;

import de.remsfal.core.model.RentalUnitModel;
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

    @Nullable
    Boolean isHeated();

    @Value.Derived
    @Nullable
    @Override
    default Float getSpace() {
        return getUsableSpace();
    }

}
