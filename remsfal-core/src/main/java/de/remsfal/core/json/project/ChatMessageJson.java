package de.remsfal.core.json.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.ChatMessageModel;
import de.remsfal.core.model.project.ChatSessionModel;
import de.remsfal.core.validation.UUID;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Null;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import java.util.Date;

/**
 * @author: Parham Rahmani [parham.rahmani@student.htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A single chat message")
@JsonDeserialize(as = ImmutableChatMessageJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class ChatMessageJson implements ChatMessageModel {

    @Null
    @Nullable
    @Override
    @UUID
    public abstract String getId();

    @Nullable
    @Override
    @UUID
    public abstract String getChatSessionId();

    @Nullable
    @Override
    @UUID
    public abstract String getSenderId();

    @Nullable
    @Override
    public abstract ContentType getContentType();

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
    public abstract Date getTimestamp();

    @Null
    @Nullable
    @Override
    @JsonIgnore
    public abstract ChatSessionModel getChatSession();

    @Null
    @Nullable
    @Override
    @JsonIgnore
    public abstract UserModel getSender();

    public static ChatMessageJson valueOf(final ChatMessageModel model)
    {
        return ImmutableChatMessageJson.builder()
                .id(model.getId())
                .chatSessionId(model.getChatSessionId())
                .senderId(model.getSenderId())
                .contentType(model.getContentType())
                .content(model.getContent())
                .url(model.getUrl())
                .timestamp(model.getTimestamp())
                .build();
    }

}
