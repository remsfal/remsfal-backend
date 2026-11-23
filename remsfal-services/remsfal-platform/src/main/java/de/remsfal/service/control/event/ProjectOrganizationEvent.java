package de.remsfal.service.control.event;

import java.util.UUID;

import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.project.OrganizationMemberJson;

/**
 * Extension point for changes of the organization memberships of a project, see {@link ProjectEvent} for the
 * semantics.
 */
public sealed interface ProjectOrganizationEvent {

    UUID projectId();

    /** Snapshot of the organization membership, taken within the transaction (for a removal the last state). */
    OrganizationMemberJson organization();

    UserJson actingUser();

    record Added(UUID projectId, OrganizationMemberJson organization, UserJson actingUser)
        implements ProjectOrganizationEvent {
    }

    record RoleChanged(UUID projectId, OrganizationMemberJson organization, UserJson actingUser)
        implements ProjectOrganizationEvent {
    }

    record Removed(UUID projectId, OrganizationMemberJson organization, UserJson actingUser)
        implements ProjectOrganizationEvent {
    }

}
