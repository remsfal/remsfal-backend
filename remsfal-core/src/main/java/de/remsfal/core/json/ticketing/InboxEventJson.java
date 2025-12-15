package de.remsfal.core.json.ticketing;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.OffsetDateTime;

@Value.Immutable
@JsonDeserialize(as = ImmutableInboxEventJson.class)
@Schema(name = "InboxEvent", description = "Event emitted for inbox notifications")
public interface InboxEventJson {

    @Schema(description = "Unique event ID")
    String id();

    @Schema(description = "ID of the related issue")
    String issueId();

    @Schema(description = "Event type, e.g. ISSUE_ASSIGNED")
    String eventType();

    String contractor();
    String subject();
    String property();
    String tenant();

    @Schema(description = "ID of the user who receives the notification")
    String userId();

    @Schema(description = "When the event occurred")
    OffsetDateTime receivedAt();

    @Schema(description = "Frontend link to the Issue")
    String link();
}