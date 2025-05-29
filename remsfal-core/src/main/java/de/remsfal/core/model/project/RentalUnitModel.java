package de.remsfal.core.model.project;

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

    String getId();

    String getTitle();

    String getDescription();

    Float getUsableSpace();

}
