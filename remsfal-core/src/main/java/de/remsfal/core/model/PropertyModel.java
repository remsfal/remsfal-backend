package de.remsfal.core.model;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface PropertyModel {

    String getId();

    String getTitle();

    String getLandRegisterEntry();

    String getDescription();

    Integer getPlotArea();

    Float getEffectiveSpace(); // living space + usable space + commercial space

}
