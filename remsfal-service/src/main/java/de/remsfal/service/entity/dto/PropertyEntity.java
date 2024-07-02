package de.remsfal.service.entity.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.util.Objects;

import de.remsfal.core.model.project.PropertyModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@NamedQuery(name = "PropertyEntity.findByProjectId",
        query = "SELECT p FROM PropertyEntity p WHERE p.projectId = :projectId")
@Table(name = "PROPERTY")
public class PropertyEntity extends AbstractEntity implements PropertyModel {

    @Id
    @Column(name = "ID", columnDefinition = "char", nullable = false, length = 36)
    private String id;
    
    @Column(name = "PROJECT_ID", columnDefinition = "char", nullable = false, updatable = false, length = 36)
    private String projectId;

    @Column(name = "TITLE")
    private String title;
    
    @Column(name = "LAND_REGISTER_ENTRY")
    private String landRegisterEntry;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "PLOT_AREA")
    private Integer plotArea;

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
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getLandRegisterEntry() {
        return landRegisterEntry;
    }

    public void setLandRegisterEntry(String landRegisterEntry) {
        this.landRegisterEntry = landRegisterEntry;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
                && Objects.equals(id, e.id)
                && Objects.equals(projectId, e.projectId)
                && Objects.equals(title, e.title)
                && Objects.equals(landRegisterEntry, e.landRegisterEntry)
                && Objects.equals(description, e.description)
                && Objects.equals(plotArea, e.plotArea);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static PropertyEntity fromModel(PropertyModel property) {
        final PropertyEntity entity = new PropertyEntity();
        entity.setId(property.getId());
        entity.setTitle(property.getTitle());
        entity.setLandRegisterEntry(property.getLandRegisterEntry());
        entity.setDescription(property.getDescription());
        entity.setPlotArea(property.getPlotArea());
        return entity;
    }

}
