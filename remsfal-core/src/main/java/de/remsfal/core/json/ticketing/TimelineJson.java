package de.remsfal.core.json.ticketing;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.TimelineModel;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Null;

import java.util.List;

@Immutable
@ImmutableStyle
@Schema(description = "An issue timeline entry")
@JsonDeserialize(as = ImmutableTimelineJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class TimelineJson extends AbstractTimelineJson {

    @Null
    @Nullable
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(readOnly = true)
    public abstract List<IssueAttachmentJson> getAttachments();

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
