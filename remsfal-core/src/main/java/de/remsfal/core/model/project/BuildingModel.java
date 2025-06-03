package de.remsfal.core.model.project;

import org.immutables.value.Value;

import de.remsfal.core.model.AddressModel;
import jakarta.annotation.Nullable;

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

    @Nullable
    Float getGrossFloorArea(); // Brutto-Grundfläche (BGF) nach DIN 277

    @Nullable
    Float getNetFloorArea(); // Netto-Raumfläche (NRF) nach DIN 277

    @Nullable
    Float getConstructionFloorArea(); // Konstruktions-Grundfläche (KGF) nach DIN 277

    @Nullable
    Float getLivingSpace(); // Wohnfläche nach Wohnflächenverordnung - WoFlV

    @Nullable
    Float getUsableSpace(); // Nutzfläche nach Wohnflächenverordnung - WoFlV

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
