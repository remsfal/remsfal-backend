package de.remsfal.service.control.event;

import java.util.UUID;

import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.project.ProjectMemberJson;

/**
 * Extension point for changes of the user memberships of a project, see {@link ProjectEvent} for the semantics.
 */
public sealed interface ProjectMemberEvent {

    UUID projectId();

    /** Snapshot of the membership, taken within the transaction (for a removal the last state). */
    ProjectMemberJson member();

    UserJson actingUser();

    record Added(UUID projectId, ProjectMemberJson member, UserJson actingUser) implements ProjectMemberEvent {
    }

    record RoleChanged(UUID projectId, ProjectMemberJson member, UserJson actingUser) implements ProjectMemberEvent {
    }

    record Removed(UUID projectId, ProjectMemberJson member, UserJson actingUser) implements ProjectMemberEvent {
    }

}
