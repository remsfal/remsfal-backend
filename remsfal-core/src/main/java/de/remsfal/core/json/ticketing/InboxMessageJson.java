package de.remsfal.core.json.ticketing;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "InboxMessage", description = "Represents a message in the user's inbox")
public class InboxMessageJson {

    @Schema(description = "Unique identifier of the message")
    public String id;

    @Schema(description = "Type of message (Nachricht | Rechnung)")
    public String type;

    @Schema(description = "Sender or contractor")
    public String contractor;

//    @Schema(description = "Property or building related to the message")
//    public String property;
//
//    @Schema(description = "Tenant or person related to the message")
//    public String tenant;

}

