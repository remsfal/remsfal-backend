package de.remsfal.core.json.project;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.remsfal.core.model.project.ChatMessageModel;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Null;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;
import java.time.Instant;
import java.util.UUID;

/**
 * @author: Parham Rahmani [parham.rahmani@student.htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A single chat message")
@JsonDeserialize(as = ImmutableChatMessageJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ChatMessageJson implements ChatMessageModel {

    @Null
    @Nullable
    @Override
    public abstract UUID getMessageId();

    @Nullable
    @Override
    public abstract UUID getChatSessionId();

    @Nullable
    @Override
    public abstract UUID getSenderId();

    @Nullable
    @Override
    public abstract String getContentType();

    @Nullable
    @Override
    public abstract String getContent();

    @Null
    @Nullable
    @Override
    public abstract String getUrl();

    @Null
    @Nullable
    @Override
    public abstract Instant getCreatedAt();

    public static ChatMessageJson valueOf(final ChatMessageModel model)
    {
        return ImmutableChatMessageJson.builder()
                .messageId(model.getMessageId())
                .chatSessionId(model.getChatSessionId())
                .senderId(model.getSenderId())
                .contentType(model.getContentType())
                .content(model.getContent())
                .url(model.getUrl())
                .createdAt(model.getCreatedAt())
                .build();
    }

}
