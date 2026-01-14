package de.remsfal.core.json.ticketing;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(name = "InboxMessage", description = "Represents an enriched issue event stored in a user's inbox")
public class InboxMessageJson {

    @Schema(description = "Unique identifier of this inbox message")
    public String id;

    @Schema(description = "User who received this notification")
    public String userId;

    @Schema(description = "Event type, e.g. ISSUE_CREATED, ISSUE_UPDATED, ISSUE_ASSIGNED")
    public String eventType;

    @Schema(description = "Related issue ID")
    public String issueId;

    @Schema(description = "Issue title")
    public String title;

    @Schema(description = "Issue description")
    public String description;

    @Schema(description = "Issue type: DEFECT, TASK, APPLICATION, ...")
    public String issueType;

    @Schema(description = "Current status of the issue")
    public String status;

    @Schema(description = "Link to the frontend issue page")
    public String link;

    @Schema(description = "Whether the message has been read")
    public boolean read;

    @Schema(description = "Timestamp when the notification was created")
    public OffsetDateTime createdAt;

    @Schema(description = "Email of the actor who triggered the event")
    public String actorEmail;

    @Schema(description = "Email of the owner assigned to the issue")
    public String ownerEmail;
}
