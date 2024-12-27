package de.remsfal.service.entity.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

import de.remsfal.core.model.project.CommercialModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "COMMERCIAL")
public class CommercialEntity extends RentalUnitEntity implements CommercialModel {

    @Id
    @Column(name = "ID", columnDefinition = "char", nullable = false, length = 36)
    private String id;

    @Column(name = "BUILDING_ID", columnDefinition = "char", nullable = false, updatable = false, length = 36)
    private String buildingId;

    @Column(name = "LOCATION")
    private String location;

    @Column(name = "COMMERCIAL_SPACE", columnDefinition = "decimal")
    private Float commercialSpace;

    @Column(name = "HEATING_SPACE", columnDefinition = "decimal")
    private Float heatingSpace;

    @Column(name="RENT", columnDefinition = "decimal")
    private Float rent;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }

    @Override
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public Float getCommercialSpace() {
        return commercialSpace;
    }

    public void setCommercialSpace(Float commercialSpace) {
        this.commercialSpace = commercialSpace;
    }

    @Override
    public Float getHeatingSpace() {
        return heatingSpace;
    }

    public void setHeatingSpace(Float heatingSpace) {
        this.heatingSpace = heatingSpace;
    }

    @Override
    public Float getRent() {return rent;}

    public void setRent(Float rent) {this.rent = rent;}
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof CommercialEntity e) {
            return super.equals(e)
                && Objects.equals(id, e.id)
                && Objects.equals(buildingId, e.buildingId)
                && Objects.equals(location, e.location)
                && Objects.equals(commercialSpace, e.commercialSpace)
                && Objects.equals(heatingSpace, e.heatingSpace)
                    && Objects.equals(rent, e.rent);

        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static CommercialEntity fromModel(CommercialModel commercial) {
        if(commercial == null) {
            return null;
        }
        final CommercialEntity entity = new CommercialEntity();
        entity.setId(commercial.getId());
        entity.setTitle(commercial.getTitle());
        entity.setLocation(commercial.getLocation());
        entity.setDescription(commercial.getDescription());
        entity.setCommercialSpace(commercial.getCommercialSpace());
        entity.setUsableSpace(commercial.getUsableSpace());
        entity.setHeatingSpace(commercial.getHeatingSpace());
        entity.setRent(commercial.getRent());
        return entity;
    }

}
