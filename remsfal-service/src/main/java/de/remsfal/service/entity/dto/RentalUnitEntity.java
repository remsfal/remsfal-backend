package de.remsfal.service.entity.dto;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@MappedSuperclass
public abstract class RentalUnitEntity extends AbstractEntity {

    @Column(name = "PROJECT_ID", columnDefinition = "char", nullable = false, updatable = false, length = 36)
    private String projectId;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "USABLE_SPACE", columnDefinition = "decimal")
    private Float usableSpace;

    @Column(name = "RENT", columnDefinition = "decimal")
    private Float rent;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Float getUsableSpace() {
        return usableSpace;
    }

    public void setUsableSpace(Float usableSpace) {
        this.usableSpace = usableSpace;
    }

    public Float getRent() {
        return rent;
    }

    public void setRent(Float rent) {
        this.rent = rent;
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
                && Objects.equals(description, e.description)
                && Objects.equals(usableSpace, e.usableSpace)
                && Objects.equals(rent, e.rent);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), projectId, title, description, usableSpace, rent);
    }

}
