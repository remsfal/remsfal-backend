package de.remsfal.core.json.ticketing;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.core.model.ticketing.TimelineModel;

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
import java.util.List;
import java.util.UUID;

@Immutable
@ImmutableStyle
@Schema(description = "An issue timeline entry")
@JsonDeserialize(as = ImmutableTimelineJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class TimelineJson implements TimelineModel {

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract UUID getIssueId();

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract UUID getTenancyId();

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract UUID getTimelineId();

    @Null
    @Nullable
    @JsonIgnore
    @Schema(readOnly = true, hidden = true)
    @Override
    public abstract UUID getProjectId();

    @Null
    @Nullable
    @JsonIgnore
    @Schema(readOnly = true, hidden = true)
    @Override
    public abstract List<UUID> getAttachmentIds();

    @Null
    @Nullable
    @Schema(readOnly = true)
    public abstract List<IssueAttachmentJson> getAttachments();

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
    public abstract MessagePurpose getPurpose();

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

    public static TimelineJson valueOf(final TimelineModel model) {
        final ImmutableTimelineJson.Builder builder = ImmutableTimelineJson.builder()
            .issueId(model.getIssueId())
            .tenancyId(model.getTenancyId())
            .timelineId(model.getTimelineId())
            .senderId(model.getSenderId())
            .senderName(model.getSenderName())
            .purpose(model.getPurpose())
            .message(model.getMessage())
            .createdAt(model.getCreatedAt())
            .modifiedAt(model.getModifiedAt());
        // projectId and attachmentIds are omitted

        if (model instanceof TimelineJson timelineJson && timelineJson.getAttachments() != null) {
            builder.attachments(timelineJson.getAttachments());
        }

        return builder.build();
    }

    public abstract TimelineJson withAttachments(final Iterable<? extends IssueAttachmentJson> attachments);

}
