package de.remsfal.core.model;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface CommercialModel {

    String getId();

    String getBuildingId();

    String getTitle();

    String getLocation();

    String getDescription();

    Float getCommercialSpace();

    Float getUsableSpace();

    Float getHeatingSpace();

    Float getRent();

}
