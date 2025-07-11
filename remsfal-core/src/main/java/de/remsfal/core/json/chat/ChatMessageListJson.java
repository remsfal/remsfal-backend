package de.remsfal.core.json.chat;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import java.util.List;

/**
 * @author: Parham Rahmani [parham.rahmani@student.htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A list of chat messages")
@JsonDeserialize(as = ImmutableChatMessageListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ChatMessageListJson {

    public abstract List<ChatMessageJson> getMessages();

    public static ChatMessageListJson valueOf(final List<? extends ChatMessageJson> messages) {
        final ImmutableChatMessageListJson.Builder builder = ImmutableChatMessageListJson.builder();
        for(ChatMessageJson message : messages) {
            builder.addMessages(message);
        }
        return builder.build();
    }
}
