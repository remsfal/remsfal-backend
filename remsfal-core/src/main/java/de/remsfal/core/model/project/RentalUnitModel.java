package de.remsfal.core.model.project;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface RentalUnitModel {

    String getId();

    String getTitle();

    String getDescription();

    TenancyModel getTenancy();

    Float getUsableSpace();

}
