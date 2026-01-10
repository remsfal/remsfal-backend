package de.remsfal.service.entity.dto.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Miroslaw Keil [miroslaw.keil@student.htw-berlin.de]
 */
@Embeddable
public class OrganizationEmployeeKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "organization_id", nullable = false, columnDefinition = "uuid")
    public UUID organizationId;

    @Column(name = "employee_id", nullable = false, columnDefinition = "uuid")
    public UUID userId;

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(final UUID organizationId) {
        this.organizationId = organizationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(final UUID userId) {
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(organizationId, userId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof OrganizationEmployeeKey e) {
            return Objects.equals(organizationId, e.organizationId)
                    && Objects.equals(userId, e.userId);
        }
        return false;
    }
}
