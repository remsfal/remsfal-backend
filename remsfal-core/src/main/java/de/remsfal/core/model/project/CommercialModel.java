package de.remsfal.core.model.project;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface CommercialModel extends RentalUnitModel {

    String getId();

    String getTitle();

    String getLocation();

    String getDescription();

    Float getCommercialSpace();

    Float getUsableSpace();

    Float getHeatingSpace();

}
