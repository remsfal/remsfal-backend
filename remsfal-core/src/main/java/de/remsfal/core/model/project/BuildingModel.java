package de.remsfal.core.model.project;

import de.remsfal.core.model.AddressModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface BuildingModel extends RentalUnitModel {

    AddressModel getAddress();

    Float getLivingSpace();

    Float getCommercialSpace();

    Float getUsableSpace();

    Float getHeatingSpace();

    Boolean isDifferentHeatingSpace();

}
