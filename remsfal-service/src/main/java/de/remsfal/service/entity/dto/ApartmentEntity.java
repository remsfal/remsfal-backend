package de.remsfal.service.entity.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

import de.remsfal.core.model.ApartmentModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "APARTMENT")
public class ApartmentEntity extends RentalUnitEntity implements ApartmentModel {

    @Id
    @Column(name = "ID", columnDefinition = "char", nullable = false, length = 36)
    private String id;

    @Column(name = "BUILDING_ID", columnDefinition = "char", nullable = false, updatable = false, length = 36)
    private String buildingId;

    @Column(name = "LOCATION")
    private String location;

    @Column(name = "LIVING_SPACE", columnDefinition = "decimal")
    private Float livingSpace;

    @Column(name = "HEATING_SPACE", columnDefinition = "decimal")
    private Float heatingSpace;

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
    public Float getLivingSpace() {
        return livingSpace;
    }

    public void setLivingSpace(Float livingSpace) {
        this.livingSpace = livingSpace;
    }

    @Override
    public Float getHeatingSpace() {
        return heatingSpace;
    }

    public void setHeatingSpace(Float heatingSpace) {
        this.heatingSpace = heatingSpace;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ApartmentEntity e) {
            return super.equals(e)
                && Objects.equals(id, e.id)
                && Objects.equals(buildingId, e.buildingId)
                && Objects.equals(location, e.location)
                && Objects.equals(livingSpace, e.livingSpace)
                && Objects.equals(heatingSpace, e.heatingSpace);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static ApartmentEntity fromModel(ApartmentModel apartment) {
        final ApartmentEntity entity = new ApartmentEntity();
        entity.setId(apartment.getId());
        entity.setTitle(apartment.getTitle());
        entity.setLocation(apartment.getLocation());
        entity.setDescription(apartment.getDescription());
        entity.setLivingSpace(apartment.getLivingSpace());
        entity.setUsableSpace(apartment.getUsableSpace());
        entity.setHeatingSpace(apartment.getHeatingSpace());
        entity.setRent(apartment.getRent());
        return entity;
    }

}
