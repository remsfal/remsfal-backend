package de.remsfal.core.model.project;

import org.immutables.value.Value;

import de.remsfal.core.model.AddressModel;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface BuildingModel extends RentalUnitModel {

    @Value.Default
    @Override
    default UnitType getType() {
        return UnitType.BUILDING;
    }

    @Nullable
    AddressModel getAddress();

    @PositiveOrZero
    @Nullable
    Float getGrossFloorArea(); // Brutto-Grundfläche (BGF) nach DIN 277

    @PositiveOrZero
    @Nullable
    Float getNetFloorArea(); // Netto-Raumfläche (NRF) nach DIN 277

    @PositiveOrZero
    @Nullable
    Float getConstructionFloorArea(); // Konstruktions-Grundfläche (KGF) nach DIN 277

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
        if(getGrossFloorArea() != null) {
            return getGrossFloorArea();
        } else if(getLivingSpace() != null && getUsableSpace() != null) {
            return getLivingSpace() + getUsableSpace();
        } else if(getLivingSpace() != null) {
            return getLivingSpace();
        }
        return getUsableSpace();
    }

}
