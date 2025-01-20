package de.remsfal.core.json.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.immutable.ImmutableStyle;
import de.remsfal.core.model.project.ChatSessionModel;
import de.remsfal.core.validation.UUID;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Null;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: Parham Rahmani [parham.rahmani@student.htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A chat session")
@JsonDeserialize(as = ImmutableChatSessionJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ChatSessionJson implements ChatSessionModel {

    @Null
    @Override
    @UUID
    public abstract String getId();

    @Null
    @Nullable
    @Override
    @UUID
    public abstract String getProjectId();

    @Null
    @Nullable
    @Override
    @UUID
    public abstract String getTaskId();

    @Nullable
    @Override
    public abstract TaskType getTaskType();

    @Nullable
    @Override
    @JsonIgnore
    public abstract Map<String, ParticipantRole> getParticipants();

    @Nullable
    @Override
    public abstract Status getStatus();


    @Null
    @Nullable
    @Override
    @JsonIgnore
    public abstract List<ChatMessageJson> getMessages();

    @Null
    @Nullable
    @Override
    public abstract Date getCreatedAt();

    @Null
    @Nullable
    @Override
    public abstract Date getModifiedAt();

    public static ChatSessionJson valueOf(final ChatSessionModel model) {
        return ImmutableChatSessionJson.builder()
                .id(model.getId())
                .projectId(model.getProjectId())
                .taskId(model.getTaskId())
                .taskType(model.getTaskType())
                .status(model.getStatus())
                .messages(model.getMessages().stream()
                        .map(ChatMessageJson::valueOf)
                        .toList())
                .createdAt(model.getCreatedAt())
                .modifiedAt(model.getModifiedAt())
                .build();
    }


}
