package de.remsfal.core.json.project;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.remsfal.core.model.project.ChatMessageModel;
import de.remsfal.core.validation.UUID;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Null;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import java.util.Date;


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
    public abstract String getImageUrl();

    @Null
    @Nullable
    @Override
    public abstract Date getTimestamp();

    public static ChatMessageJson valueOf(final ChatMessageModel model)
    {
        return ImmutableChatMessageJson.builder()
                .id(model.getId())
                .chatSessionId(model.getChatSessionId())
                .senderId(model.getSenderId())
                .contentType(model.getContentType())
                .content(model.getContent())
                .imageUrl(model.getImageUrl())
                .timestamp(model.getTimestamp())
                .build();
    }

}