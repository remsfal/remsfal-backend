package de.remsfal.core.model;

import jakarta.annotation.Nullable;
import java.util.UUID;

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
        COMMERCIAL;
    }

    @Nullable
    UUID getId();

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
