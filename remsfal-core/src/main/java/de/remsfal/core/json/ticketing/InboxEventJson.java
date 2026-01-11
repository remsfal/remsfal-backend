package de.remsfal.core.json.ticketing;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.remsfal.core.json.ticketing.ImmutableInboxEventJson;
import de.remsfal.core.json.ticketing.ProjectInfoJson;
import de.remsfal.core.json.ticketing.UserInfoJson;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableInboxEventJson.class)
@Schema(name = "InboxEvent", description = "Enriched issue event stored in the inbox")
public interface InboxEventJson {

    @Schema(description = "Type of event, e.g. ISSUE_CREATED")
    String type();

    @Schema(description = "Related issue ID")
    String issueId();

    @Schema(description = "Project info")
    ProjectInfoJson project();

    @Schema(description = "Issue title")
    String title();

    @Schema(description = "Frontend link to the issue")
    String link();

    @Schema(description = "Type of issue")
    String issueType();

    @Schema(description = "Current issue status")
    String status();

    @Schema(description = "ID of assigned owner")
    String ownerId();

    @Schema(description = "Issue description")
    String description();

    @Schema(description = "User who triggered the event")
    UserInfoJson user();

    @Schema(description = "Assigned owner details")
    UserInfoJson owner();
}
