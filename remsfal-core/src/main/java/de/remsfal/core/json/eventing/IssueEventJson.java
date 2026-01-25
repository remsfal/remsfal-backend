package de.remsfal.core.json.eventing;

import java.util.Set;
import java.util.UUID;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import jakarta.annotation.Nullable;

/**
 * Enriched issue event schema for Kafka messaging between microservices.
 *
 * <h3>Schema Version: 1.0</h3>
 *
 * This interface defines the public contract for issue events exchanged between:
 * <ul>
 *   <li>ticketing-service: Producer of basic issue events (ISSUE_CREATED, ISSUE_UPDATED,
 *   ISSUE_ASSIGNED, ISSUE_MENTIONED)</li>
 *   <li>platform-service: Enricher of events (adds project and user details)</li>
 *   <li>notification-service: Consumer of enriched events (sends email notifications)</li>
 * </ul>
 *
 * <h3>Versioning Guidelines</h3>
 * When modifying this schema:
 * <ul>
 *   <li>MINOR changes (new optional fields): Increment patch version (e.g., 1.0 →
 *   1.0.1)</li>
 *   <li>MAJOR changes (remove/rename fields, change types): Increment minor version (e.g.,
 *   1.0 → 1.1)</li>
 *   <li>Breaking changes: Increment major version (e.g., 1.0 → 2.0) and coordinate across
 *   all services</li>
 * </ul>
 *
 * @see <a href="https://github.com/remsfal/remsfal-backend/issues/593">Issue #593: Ticket
 *      notification Kafka consumer</a>
 */
@Immutable
@ImmutableStyle
@JsonDeserialize(as = ImmutableIssueEventJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public interface IssueEventJson {

    String TOPIC_BASIC = "issue-events-basic";
    String TOPIC_ENRICHED = "issue-events-enriched";

    enum IssueEventType {
        ISSUE_CREATED,
        ISSUE_UPDATED,
        ISSUE_ASSIGNED,
        ISSUE_MENTIONED
    }

    IssueEventType getIssueEventType();

    UUID getIssueId();

    /**
     * Project identifier for direct lookups.
     */
    UUID getProjectId();

    /**
     * Optional enriched project details. When present, provides the project title and metadata
     * to avoid additional database queries. Both projectId and project may be present;
     * use projectId for database operations and project for display purposes.
     */
    @Nullable
    ProjectEventJson getProject();

    @Nullable
    String getTitle();

    /**
     * Frontend link to the issue detail/edit view.
     * Should be populated by the enricher service to enable direct access from email notifications.
     * If null, a fallback link should be constructed using projectId and issueId.
     */
    @Nullable
    String getLink();

    @Nullable
    IssueType getIssueType();

    @Nullable
    IssueStatus getStatus();

    @Nullable
    UUID getReporterId();

    @Nullable
    UUID getTenancyId();

    @Nullable
    UUID getAssigneeId();

    @Nullable
    String getDescription();

    @Nullable
    UUID getParentIssue();

    @Nullable
    Set<UUID> getChildrenIssues();

    /**
     * Identifier of a ticket that blocks this issue from progressing.
     * Reserved for future notification types (e.g., dependency updates).
     */
    @Nullable
    Set<UUID> getBlockedBy();

    /**
     * Identifier of a ticket that is related to this issue (non-blocking relation).
     * Intended for future email content linking related work items.
     */
    @Nullable
    Set<UUID> getRelatedTo();

    @Nullable
    Set<UUID> getBlocks();

    /**
     * Identifier of the original ticket when this issue is marked as a duplicate.
     * May be used by future templates to guide users to the canonical ticket.
     */
    @Nullable
    Set<UUID> getDuplicateOf();

    @Nullable
    UserJson getUser();

    /**
     * Target user for owner assignment events.
     */
    @Nullable
    UserJson getAssignee();

    /**
     * Target user for mention events.
     */
    @Nullable
    UserJson getMentionedUser();
}
