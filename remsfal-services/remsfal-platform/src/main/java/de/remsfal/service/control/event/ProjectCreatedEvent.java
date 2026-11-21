package de.remsfal.service.control.event;

import java.util.UUID;

/**
 * Synchronous CDI event fired by {@code ProjectController} after a project has been persisted, within the
 * transaction that creates the project.
 * <p>
 * Observers using {@code @Observes} run in this transaction: an exception thrown by such an observer rolls back the
 * creation of the project. Observers that must only react to committed projects (e.g. to publish messages) use
 * {@code @Observes(during = TransactionPhase.AFTER_SUCCESS)}.
 *
 * @param projectId              id of the created project
 * @param creatorUserId          id of the user who created the project
 * @param managingOrganizationId id of the managing organization, or {@code null} if the project was created for the
 *                               user personally
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public record ProjectCreatedEvent(UUID projectId, UUID creatorUserId, UUID managingOrganizationId) {
}
