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
        COMMERCIAL;

        public static final String asResourcePath(final UnitType type) {
            if(type.name().endsWith("Y")) {
                return "properties";
            }
            return type.name().toLowerCase().concat("s");
        }

        public final String asResourcePath() {
            if(this.name().endsWith("Y")) {
                return "properties";
            }
            return this.name().toLowerCase().concat("s");
        }

        public static final UnitType fromResourcePath(final String rentalType) {
            if(rentalType.equals("properties")) {
                return UnitType.PROPERTY;
            }
            return UnitType.valueOf(rentalType.substring(0, rentalType.length() - 1).toUpperCase());
        }
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
