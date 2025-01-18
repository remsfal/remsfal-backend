package de.remsfal.core.model.project;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface ApartmentModel extends RentalUnitModel {

    String getLocation();

    Float getLivingSpace();

    Float getUsableSpace();

    Float getHeatingSpace();



}
