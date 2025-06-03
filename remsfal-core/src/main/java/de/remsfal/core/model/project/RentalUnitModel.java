package de.remsfal.core.model.project;

import jakarta.annotation.Nullable;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface RentalUnitModel {

    public enum UnitType {
        PROPERTY,
        SITE,
        BUILDING,
        APARTMENT,
        STORAGE,
        COMMERCIAL
    }

    @Nullable
    String getId();

    UnitType getType();

    @Nullable
    String getTitle();

    @Nullable
    String getLocation();

    @Nullable
    String getDescription();

    @Nullable
    Float getSpace();

}
