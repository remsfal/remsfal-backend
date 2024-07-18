package de.remsfal.service.entity.dto;

import de.remsfal.core.model.project.RentalUnitModel;

import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@MappedSuperclass
public abstract class RentalUnitEntity extends AbstractEntity implements RentalUnitModel {

    @Column(name = "PROJECT_ID", columnDefinition = "char", nullable = false, updatable = false, length = 36)
    private String projectId;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "DESCRIPTION")
    private String description;

    @OneToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "TENANCY_ID")
    private TenancyEntity tenancy;

    @Column(name = "USABLE_SPACE", columnDefinition = "decimal")
    private Float usableSpace;

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
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public TenancyEntity getTenancy() {
        return tenancy;
    }

    public void setTenancies(final TenancyEntity tenancy) {
        this.tenancy = tenancy;
    }

    @Override
    public Float getUsableSpace() {
        return usableSpace;
    }

    public void setUsableSpace(final Float usableSpace) {
        this.usableSpace = usableSpace;
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
                && Objects.equals(tenancy, e.tenancy)
                && Objects.equals(usableSpace, e.usableSpace);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), projectId, title, description, tenancy, usableSpace);
    }

}
