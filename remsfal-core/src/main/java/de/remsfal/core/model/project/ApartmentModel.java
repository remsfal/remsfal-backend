package de.remsfal.core.model.project;

import org.immutables.value.Value;

import de.remsfal.core.model.RentalUnitModel;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface ApartmentModel extends RentalUnitModel {

    @Value.Default
    @Override
    default UnitType getType() {
        return UnitType.APARTMENT;
    }

    @PositiveOrZero
    @Nullable
    Float getLivingSpace(); // Wohnfläche nach Wohnflächenverordnung - WoFlV

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
        if(getLivingSpace() != null && getUsableSpace() != null) {
            return getLivingSpace() + getUsableSpace();
        } else if(getLivingSpace() != null) {
            return getLivingSpace();
        }
        return getUsableSpace();
    }

}
