package de.remsfal.service.entity.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import de.remsfal.core.model.ApartmentModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "APARTMENT")
public class ApartmentEntity extends AbstractEntity implements ApartmentModel {

    @Id
    @Column(name = "ID", columnDefinition = "char", nullable = false, length = 36)
    private String id;

    @Id
    @Column(name = "PROJECT_ID", columnDefinition = "char", nullable = false, updatable = false, length = 36)
    private String projectId;

    @Id
    @Column(name = "BUILDING_ID", columnDefinition = "char", nullable = false, updatable = false, length = 36)
    private String buildingId;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "LOCATION")
    private String location;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "LIVING_SPACE", columnDefinition = "decimal")
    private Float livingSpace;

    @Column(name = "USABLE_SPACE", columnDefinition = "decimal")
    private Float usableSpace;

    @Column(name = "HEATING_SPACE", columnDefinition = "decimal")
    private Float heatingSpace;

    @Column(name = "RENT", columnDefinition = "decimal")
    private Float rent;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Override
    public String getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Float getLivingSpace() {
        return livingSpace;
    }

    public void setLivingSpace(Float livingSpace) {
        this.livingSpace = livingSpace;
    }

    @Override
    public Float getUsableSpace() {
        return usableSpace;
    }

    public void setUsableSpace(Float usableSpace) {
        this.usableSpace = usableSpace;
    }

    @Override
    public Float getHeatingSpace() {
        return heatingSpace;
    }

    public void setHeatingSpace(Float heatingSpace) {
        this.heatingSpace = heatingSpace;
    }

    @Override
    public Float getRent() {
        return rent;
    }

    public void setRent(Float rent) {
        this.rent = rent;
    }

}
