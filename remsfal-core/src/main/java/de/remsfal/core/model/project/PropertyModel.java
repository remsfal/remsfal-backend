package de.remsfal.core.model.project;

import org.immutables.value.Value;

import jakarta.annotation.Nullable;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface PropertyModel extends RentalUnitModel {

    @Value.Default
    @Override
    default UnitType getType() {
        return UnitType.PROPERTY;
    }

    @Nullable
    String getLandRegistry(); // Grundbuchamt / Katasteramt

    @Nullable
    String getCadastralDistrict(); // Gemarkung / Bezirk

    @Nullable
    String getSheetNumber(); // Grundbuchblattnummer

    @Nullable
    Integer getPlotNumber(); // Laufende Nummer des Grundstücks

    @Nullable
    String getCadastralSection(); // Flur

    @Nullable
    String getPlot(); // Flurstück

    @Nullable
    String getEconomyType(); // Wirtschaftsart

    @Nullable
    @Override
    String getLocation(); // Lage des Grundstücks / Adresse

    @Nullable
    Integer getPlotArea(); // Größe

    @Value.Derived
    @Nullable
    @Override
    default Float getSpace() {
        return getPlotArea().floatValue();
    }

}
