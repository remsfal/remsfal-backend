package de.remsfal.core.json.eventing;

import java.util.UUID;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.ContractorJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.project.ProjectJson;
import de.remsfal.core.json.project.RentalAgreementJson;
import de.remsfal.core.json.ticketing.ChatMessageJson;
import de.remsfal.core.json.ticketing.IssueJson;
import de.remsfal.core.json.ticketing.OrderPlacementJson;
import de.remsfal.core.json.ticketing.QuotationJson;
import de.remsfal.core.json.ticketing.QuotationRequestJson;
import de.remsfal.core.json.ticketing.TenantTimelineJson;
import jakarta.annotation.Nullable;

/**
 * Enriched issue event schema for Kafka messaging between microservices.
 *
 * <h3>Schema Version: 2.0.1</h3>
 *
 * This interface defines the public contract for issue events exchanged between:
 * <ul>
 *   <li>ticketing-service: Producer of basic issue events (ISSUE_CREATED, ISSUE_UPDATED,
 *   ISSUE_ASSIGNED, ISSUE_MENTIONED, and the activity events below)</li>
 *   <li>platform-service: Enricher of events (resolves project details and role stubs to full
 *   user profiles)</li>
 *   <li>notification-service / ticketing-service: Consumers of enriched events (send email
 *   notifications, record activity feed entries)</li>
 * </ul>
 *
 * <p>Instead of a single free-text {@code activityText} and a generic {@code user} actor, each
 * activity event carries the full domain object it relates to ({@link #getChatMessage()},
 * {@link #getQuotationRequest()}, ...) plus the specific role(s) that apply to it, so consumers
 * can build detailed, localizable messages instead of relying on a pre-baked string.
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
     * Optional enriched rental agreement details. When present (i.e. the issue is linked to a
     * tenancy via {@code issue.getAgreementId()}), provides the agreement's tenants and rent
     * data to avoid additional database queries.
     */
    @Nullable
    RentalAgreementJson getRentalAgreement();

    /**
     * Frontend link to the issue detail/edit view.
     * Should be populated by the enricher service to enable direct access from email notifications.
     * If null, a fallback link should be constructed using the issue's projectId and issueId.
     */
    @Nullable
    String getLink();

    /**
     * The user who directly triggered this event. Always populated by the producer, present on
     * every event type regardless of which role fields below also apply.
     */
    @Nullable
    UserJson getPrincipal();

    /**
     * Target user for owner assignment events. Also carried on every event as the issue's
     * current assignee ({@code issue.getAssigneeId()}).
     */
    @Nullable
    UserJson getAssignee();

    /**
     * The user who reported the issue ({@code issue.getReporterId()}). Carried on every event
     * since the issue is always embedded.
     */
    @Nullable
    UserJson getReporter();

    /**
     * The platform-side (manager) user who initiated a quotation request or order placement.
     */
    @Nullable
    UserJson getInitiator();

    /**
     * The contractor that is involved in this order management.
     */
    @Nullable
    ContractorJson getContractor();

    /**
     * The contractor-side user who confirmed or rejected an order placement.
     */
    @Nullable
    UserJson getConfirmor();

    /**
     * The contractor-side user who submitted a quotation.
     */
    @Nullable
    UserJson getOfferer();

    /**
     * The chat message that was created. Set only for {@link IssueEventType#CHAT_MESSAGE_CREATED}.
     */
    @Nullable
    ChatMessageJson getChatMessage();

    /**
     * The tenant timeline entry that was created. Set only for
     * {@link IssueEventType#TIMELINE_ENTRY_CREATED}.
     */
    @Nullable
    TenantTimelineJson getTimelineEntry();

    /**
     * The quotation request this event relates to. Set for
     * {@link IssueEventType#QUOTATION_REQUEST_CREATED} and
     * {@link IssueEventType#QUOTATION_REQUEST_STATUS_CHANGED}.
     */
    @Nullable
    QuotationRequestJson getQuotationRequest();

    /**
     * The quotation that was submitted. Set only for {@link IssueEventType#QUOTATION_CREATED}.
     */
    @Nullable
    QuotationJson getQuotation();

    /**
     * The order placement this event relates to. Set for {@link IssueEventType#ORDER_PLACED} and
     * {@link IssueEventType#ORDER_PLACEMENT_STATUS_CHANGED}.
     */
    @Nullable
    OrderPlacementJson getOrderPlacement();
}
