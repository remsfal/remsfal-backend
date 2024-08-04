package de.remsfal.service.entity.dto;

import jakarta.persistence.*;

import java.util.Objects;

import de.remsfal.core.model.project.BuildingModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@NamedQuery(name = "BuildingEntity.findByProjectIdAndByPropertyIdAndByBuildingId",
        query = "SELECT m FROM BuildingEntity m WHERE m.id = :buildingId and m.propertyId = :propertyId and m.projectId = :projectId")
@Table(name = "BUILDING")
public class BuildingEntity extends RentalUnitEntity implements BuildingModel {

    @Id
    @Column(name = "ID", columnDefinition = "char", nullable = false, length = 36)
    private String id;

    @Column(name = "PROPERTY_ID", columnDefinition = "char", nullable = false, updatable = false, length = 36)
    private String propertyId;

    @OneToOne(fetch = FetchType.EAGER, cascade={CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "ADDRESS_ID")
    private AddressEntity address;

    @Column(name = "LIVING_SPACE", columnDefinition = "decimal")
    private Float livingSpace;

    @Column(name = "COMMERCIAL_SPACE", columnDefinition = "decimal")
    private Float commercialSpace;

    @Column(name = "HEATING_SPACE", columnDefinition = "decimal")
    private Float heatingSpace;

    @Column(name = "DIFFERENT_HEATING_SPACE", columnDefinition = "TINYINT")
    private Boolean differentHeatingSpace;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(final String id) {
        this.id = id;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(final String propertyId) {
        this.propertyId = propertyId;
    }

    @Override
    public AddressEntity getAddress() {
        return address;
    }

    public void setAddress(final AddressEntity address) {
        this.address = address;
    }

    @Override
    public Float getLivingSpace() {
        return livingSpace;
    }

    public void setLivingSpace(final Float livingSpace) {
        this.livingSpace = livingSpace;
    }

    @Override
    public Float getCommercialSpace() {
        return commercialSpace;
    }

    public void setCommercialSpace(final Float commercialSpace) {
        this.commercialSpace = commercialSpace;
    }

    @Override
    public Float getHeatingSpace() {
        return heatingSpace;
    }

    public void setHeatingSpace(final Float heatingSpace) {
        this.heatingSpace = heatingSpace;
    }

    @Override
    public Boolean isDifferentHeatingSpace() {
        return differentHeatingSpace;
    }

    public void setDifferentHeatingSpace(final Boolean differentHeatingSpace) {
        this.differentHeatingSpace = differentHeatingSpace;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof BuildingEntity e) {
            return super.equals(e)
                && Objects.equals(id, e.id)
                && Objects.equals(propertyId, e.propertyId)
                && Objects.equals(address, e.address)
                && Objects.equals(livingSpace, e.livingSpace)
                && Objects.equals(commercialSpace, e.commercialSpace)
                && Objects.equals(heatingSpace, e.heatingSpace)
                && Objects.equals(differentHeatingSpace, e.differentHeatingSpace);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static BuildingEntity fromModel(BuildingModel building) {
        if(building == null) {
            return null;
        }
        final BuildingEntity entity = new BuildingEntity();
        entity.setId(building.getId());
        entity.setTitle(building.getTitle());
        entity.setAddress(AddressEntity.fromModel(building.getAddress()));
        entity.setDescription(building.getDescription());
        entity.setLivingSpace(building.getLivingSpace());
        entity.setCommercialSpace(building.getCommercialSpace());
        entity.setUsableSpace(building.getUsableSpace());
        entity.setHeatingSpace(building.getHeatingSpace());
        entity.setDifferentHeatingSpace(building.isDifferentHeatingSpace());
        return entity;
    }

}
