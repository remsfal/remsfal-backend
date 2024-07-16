package de.remsfal.core.model.project;

import de.remsfal.core.model.AddressModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface SiteModel extends RentalUnitModel {

    AddressModel getAddress();

    Float getUsableSpace();

}
