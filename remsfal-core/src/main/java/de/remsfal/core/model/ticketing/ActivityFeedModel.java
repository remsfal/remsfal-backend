package de.remsfal.core.model.ticketing;

import java.time.Instant;
import java.util.UUID;

import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;

/**
 * Represents one entry of a user's activity feed: an issue lifecycle event, a tenant timeline
 * entry, a chat message, or a quotation/order-placement event, all recorded for the issue's
 * assignee.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface ActivityFeedModel {

    UUID getId();

    UUID getUserId();

    UUID getProjectId();

    UUID getIssueId();

    IssueEventType getActivityType();

    String getTitle();

    String getDescription();

    String getLink();

    UUID getActorId();

    String getActorName();

    IssueType getIssueType();

    IssueStatus getStatus();

    UUID getAgreementId();

    UUID getOrganizationId();

    UUID getContractorId();

    UUID getAssigneeId();

    boolean isRead();

    Instant getCreatedAt();

}
