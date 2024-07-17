package de.remsfal.core.model.project;

import de.remsfal.core.model.AddressModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface BuildingModel {

    String getId();

    String getTitle();

    AddressModel getAddress();

    String getDescription();

    Float getLivingSpace();

    Float getCommercialSpace();

    Float getUsableSpace();

    Float getHeatingSpace();

    Boolean isDifferentHeatingSpace();

}
