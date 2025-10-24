package de.remsfal.core.json.ticketing;

import java.util.List;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.ChatSessionModel;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * @author Parham Rahmani [parham.rahmani@student.htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A list of chat sessions")
@JsonDeserialize(as = ImmutableChatSessionListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ChatSessionListJson {
    // Validation is not required, because it is read-only.

    @Schema(description = "Number of chat sessions in the list", required = true)
    public abstract Integer getSize();

    public abstract List<ChatSessionJson> getChatSessions();

    public static ChatSessionListJson valueOf(final List<? extends ChatSessionModel> chatSessions) {
        return ImmutableChatSessionListJson.builder()
            .size(chatSessions.size())
            .chatSessions(chatSessions.stream()
                .map(ChatSessionJson::valueOf)
                .toList())
            .build();
    }

}