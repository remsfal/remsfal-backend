package de.remsfal.service.entity.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Objects;

import de.remsfal.core.model.project.PropertyModel;
import de.remsfal.service.entity.dto.superclass.RentalUnitEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "properties")
public class PropertyEntity extends RentalUnitEntity implements PropertyModel {

    @Column(name = "LAND_REGISTRY")
    private String landRegistry;

    @Column(name = "CADASTRAL_DISTRICT")
    private String cadastralDistrict;

    @Column(name = "SHEET_NUMBER")
    private String sheetNumber;

    @Column(name = "PLOT_NUMBER")
    private Integer plotNumber;

    @Column(name = "CADASTRAL_SECTION")
    private String cadastralSection;

    @Column(name = "PLOT")
    private String plot;

    @Column(name = "ECONOMY_TYPE")
    private String economyType;

    @Column(name = "PLOT_AREA")
    private Integer plotArea;

    @Override
    public String getLandRegistry() {
        return landRegistry;
    }

    public void setLandRegistry(String landRegistry) {
        this.landRegistry = landRegistry;
    }

    @Override
    public String getCadastralDistrict() {
        return cadastralDistrict;
    }

    public void setCadastralDistrict(String cadastralDistrict) {
        this.cadastralDistrict = cadastralDistrict;
    }

    @Override
    public String getSheetNumber() {
        return sheetNumber;
    }

    public void setSheetNumber(String sheetNumber) {
        this.sheetNumber = sheetNumber;
    }

    @Override
    public Integer getPlotNumber() {
        return plotNumber;
    }

    public void setPlotNumber(Integer plotNumber) {
        this.plotNumber = plotNumber;
    }

    @Override
    public String getCadastralSection() {
        return cadastralSection;
    }

    public void setCadastralSection(String cadastralSection) {
        this.cadastralSection = cadastralSection;
    }

    @Override
    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    @Override
    public String getEconomyType() {
        return economyType;
    }

    public void setEconomyType(String economyType) {
        this.economyType = economyType;
    }

    @Override
    public Integer getPlotArea() {
        return plotArea;
    }

    public void setPlotArea(Integer plotArea) {
        this.plotArea = plotArea;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof PropertyEntity e) {
            return super.equals(e)
                && Objects.equals(landRegistry, e.landRegistry)
                && Objects.equals(cadastralDistrict, e.cadastralDistrict)
                && Objects.equals(sheetNumber, e.sheetNumber)
                && Objects.equals(plotNumber, e.plotNumber)
                && Objects.equals(cadastralSection, e.cadastralSection)
                && Objects.equals(plot, e.plot)
                && Objects.equals(economyType, e.economyType)
                && Objects.equals(plotArea, e.plotArea);
        }
        return false;
    }

}
