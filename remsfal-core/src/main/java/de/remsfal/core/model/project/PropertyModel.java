package de.remsfal.core.model.project;

import org.immutables.value.Value;

import de.remsfal.core.model.RentalUnitModel;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

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

    @Positive
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

    @PositiveOrZero
    @Nullable
    Integer getPlotArea(); // Größe

    @Value.Derived
    @Nullable
    @Override
    default Float getSpace() {
        if(getPlotArea() == null) {
            return null;
        }
        return getPlotArea().floatValue();
    }

}
