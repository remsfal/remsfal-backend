package de.remsfal.core.model;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface BuildingModel {

    String getId();

    String getPropertyId();

    String getTitle();

    AddressModel getAddress();

    String getDescription();

    Float getLivingSpace();

    Float getCommercialSpace();

    Float getUsableSpace();

    Float getHeatingSpace();

    Float getRent();

}
