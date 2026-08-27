package de.remsfal.core.json.ticketing;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.ChatMessageModel;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.time.Instant;
import java.util.UUID;

@Immutable
@ImmutableStyle
@Schema(description = "An internal chat message between project members for an issue")
@JsonDeserialize(as = ImmutableChatMessageJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ChatMessageJson implements ChatMessageModel {

    @Null
    @Nullable
    @JsonIgnore
    @Schema(readOnly = true, hidden = true)
    @Override
    public abstract UUID getProjectId();

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract UUID getIssueId();

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract UUID getMessageId();

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract UUID getSenderId();

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract String getSenderName();

    @NotNull
    @Nullable
    @Override
    public abstract String getMessage();

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract Instant getCreatedAt();

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract Instant getModifiedAt();

    public static ChatMessageJson valueOf(final ChatMessageModel model) {
        return ImmutableChatMessageJson.builder()
            .issueId(model.getIssueId())
            .messageId(model.getMessageId())
            .senderId(model.getSenderId())
            .senderName(model.getSenderName())
            .message(model.getMessage())
            .createdAt(model.getCreatedAt())
            .modifiedAt(model.getModifiedAt())
            .build();
        // projectId is omitted
    }

}
