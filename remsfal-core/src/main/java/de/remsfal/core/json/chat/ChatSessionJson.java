package de.remsfal.core.json.chat;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.chat.ChatSessionModel;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Null;

/**
 * @author: Parham Rahmani [parham.rahmani@student.htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A chat session")
@JsonDeserialize(as = ImmutableChatSessionJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ChatSessionJson implements ChatSessionModel {

    @Override
    public abstract UUID getSessionId();

    @Nullable
    @Override
    public abstract UUID getProjectId();

    @Nullable
    @Override
    public abstract UUID getTaskId();

    @Nullable
    @Override
    @JsonIgnore
    public abstract Map<java.util.UUID, String> getParticipants();

    @Null
    @Nullable
    @Override
    public abstract Instant getCreatedAt();

    @Null
    @Nullable
    @Override
    public abstract Instant getModifiedAt();

    public static ChatSessionJson valueOf(final ChatSessionModel model) {
        return ImmutableChatSessionJson.builder()
                .sessionId(model.getSessionId())
                .projectId(model.getProjectId())
                .taskId(model.getTaskId())
                .createdAt(model.getCreatedAt())
                .modifiedAt(model.getModifiedAt())
                .build();
    }


}
