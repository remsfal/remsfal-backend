package de.remsfal.core.json.eventing;

import java.util.UUID;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.project.ProjectJson;
import de.remsfal.core.json.ticketing.IssueJson;
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
        ISSUE_MENTIONED,
        TIMELINE_ENTRY_CREATED,
        CHAT_MESSAGE_CREATED,
        QUOTATION_REQUEST_CREATED,
        QUOTATION_REQUEST_STATUS_CHANGED,
        QUOTATION_CREATED,
        ORDER_PLACED,
        ORDER_PLACEMENT_STATUS_CHANGED
    }

    IssueEventType getIssueEventType();

    UUID getIssueId();

    /**
     * The full issue data as stored in the issue table. Attachments are always {@code null}
     * here, consistent with {@link IssueJson#valueOf} which never populates them.
     */
    @Nullable
    IssueJson getIssue();

    /**
     * Optional enriched project details. When present, provides the project title and metadata
     * to avoid additional database queries.
     */
    @Nullable
    ProjectJson getProject();

    /**
     * Frontend link to the issue detail/edit view.
     * Should be populated by the enricher service to enable direct access from email notifications.
     * If null, a fallback link should be constructed using the issue's projectId and issueId.
     */
    @Nullable
    String getLink();

    /**
     * The descriptive text to display for this event. For the core issue lifecycle events
     * (ISSUE_CREATED, ISSUE_UPDATED, ISSUE_ASSIGNED, ISSUE_MENTIONED) this mirrors
     * {@code getIssue().getDescription()}. For activity events (timeline entries, chat
     * messages, quotation/order status changes) it carries that activity's own text instead,
     * which is why it is a distinct field rather than being read off {@link #getIssue()}.
     */
    @Nullable
    String getActivityText();

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

    /**
     * Contractor organization involved in a quotation/order-placement activity event.
     */
    @Nullable
    UUID getOrganizationId();

    /**
     * Contractor involved in a quotation/order-placement activity event.
     */
    @Nullable
    UUID getContractorId();
}
