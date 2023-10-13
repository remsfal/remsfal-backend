package de.remsfal.core.model;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface SiteModel {

    String getId();

    String getTitle();

    AddressModel getAddress();

    String getDescription();

    Float getUsableSpace();

    Float getRent();

}
