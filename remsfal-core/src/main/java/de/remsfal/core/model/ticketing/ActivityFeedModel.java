package de.remsfal.core.model.ticketing;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
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

    String getProjectTitle();

    UUID getIssueId();

    IssueEventType getActivityType();

    String getIssueTitle();

    UUID getActorId();

    String getActorName();

    IssueType getIssueType();

    IssueStatus getIssueStatus();

    IssuePriority getIssuePriority();

    UUID getAgreementId();

    List<String> getTenantNames();

    UUID getOrganizationId();

    UUID getContractorId();

    String getContractorName();

    boolean isRead();

    Instant getCreatedAt();

}
