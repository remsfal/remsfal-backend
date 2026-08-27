package de.remsfal.core.json.ticketing;

import java.util.List;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.ChatMessageModel;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@Immutable
@ImmutableStyle
@Schema(description = "A list of internal issue chat messages")
@JsonDeserialize(as = ImmutableChatMessageListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ChatMessageListJson {
    // Validation is not required, because it is read-only.

    @Schema(description = "Chat messages", readOnly = true)
    public abstract List<ChatMessageJson> getMessages();

    public static ChatMessageListJson valueOf(final List<? extends ChatMessageModel> messages) {
        return ImmutableChatMessageListJson.builder()
            .messages(messages.stream()
                .map(ChatMessageJson::valueOf)
                .toList())
            .build();
    }

}
