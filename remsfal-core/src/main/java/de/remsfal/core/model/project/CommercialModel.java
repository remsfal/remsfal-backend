package de.remsfal.core.model.project;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface CommercialModel {

    String getId();

    String getTitle();

    String getLocation();

    String getDescription();

    Float getCommercialSpace();

    Float getUsableSpace();

    Float getHeatingSpace();

}
