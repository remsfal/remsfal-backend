package de.remsfal.core.json.ticketing;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(name = "InboxMessage", description = "Represents a message or invoice in the user's inbox")
public class InboxMessageJson {

    @Schema(description = "Unique identifier of the message")
    public String id;

    @Schema(
            description = "Type of message (Nachricht | Rechnung)",
            enumeration = {"Nachricht", "Rechnung"}
    )
    public String type;

    @Schema(description = "Sender or contractor associated with the message")
    public String contractor;

    @Schema(description = "Subject or title of the message")
    public String subject;

    @Schema(description = "Property or building related to the message")
    public String property;

    @Schema(description = "Tenant or person related to the message")
    public String tenant;

    @Schema(description = "Date and time when the message was received")
    public OffsetDateTime receivedAt;
}
