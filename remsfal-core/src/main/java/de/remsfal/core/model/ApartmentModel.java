package de.remsfal.core.model;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface ApartmentModel {

    String getId();

    String getTitle();

    String getLocation();

    String getDescription();

    Float getLivingSpace();

    Float getUsableSpace();

    Float getHeatingSpace();

    Float getRent();

}
