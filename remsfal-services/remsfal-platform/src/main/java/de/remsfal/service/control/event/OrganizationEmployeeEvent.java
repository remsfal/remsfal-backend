package de.remsfal.service.control.event;

import java.util.UUID;

import de.remsfal.core.json.organization.OrganizationEmployeeJson;

/**
 * Extension point for changes of the employees of an organization, see {@link ProjectEvent} for the semantics.
 */
public sealed interface OrganizationEmployeeEvent {

    UUID organizationId();

    /** Snapshot of the employment, taken within the transaction (for a removal the last state). */
    OrganizationEmployeeJson employee();

    UUID actingUserId();

    record Added(UUID organizationId, OrganizationEmployeeJson employee, UUID actingUserId)
        implements OrganizationEmployeeEvent {
    }

    record RoleChanged(UUID organizationId, OrganizationEmployeeJson employee, UUID actingUserId)
        implements OrganizationEmployeeEvent {
    }

    record Removed(UUID organizationId, OrganizationEmployeeJson employee, UUID actingUserId)
        implements OrganizationEmployeeEvent {
    }

}
