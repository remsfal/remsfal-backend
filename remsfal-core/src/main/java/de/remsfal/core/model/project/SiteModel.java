package de.remsfal.core.model.project;

import org.immutables.value.Value;

import de.remsfal.core.model.AddressModel;
import jakarta.annotation.Nullable;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface SiteModel extends RentalUnitModel {

    @Value.Default
    @Override
    default UnitType getType() {
        return UnitType.SITE;
    }

    @Nullable
    AddressModel getAddress();

    @Nullable
    Float getOutdoorArea(); // Außenanlagenfläche (AF) nach DIN 277

    @Value.Derived
    @Nullable
    @Override
    default Float getSpace() {
        return getOutdoorArea();
    }

}
