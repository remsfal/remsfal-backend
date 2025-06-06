package de.remsfal.service.entity.dto.superclass;

import de.remsfal.core.model.project.RentalUnitModel;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@MappedSuperclass
public abstract class RentalUnitEntity extends AbstractEntity implements RentalUnitModel {

    @Column(name = "PROJECT_ID", columnDefinition = "char", nullable = false, updatable = false, length = 36)
    private String projectId;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "LOCATION")
    private String location;

    @Column(name = "DESCRIPTION")
    private String description;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
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

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof RentalUnitEntity e) {
            return super.equals(e)
                && Objects.equals(projectId, e.projectId)
                && Objects.equals(title, e.title)
                && Objects.equals(location, e.location)
                && Objects.equals(description, e.description);
        }
        return false;
    }

}
