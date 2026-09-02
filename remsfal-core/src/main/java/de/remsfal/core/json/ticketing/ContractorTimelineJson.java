package de.remsfal.core.json.ticketing;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.ContractorTimelineModel;
import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.core.model.ticketing.ParticipantRole;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@Schema(description = "A contractor timeline entry")
@JsonDeserialize(as = ImmutableContractorTimelineJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ContractorTimelineJson implements ContractorTimelineModel {

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract UUID getRequestId();

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract UUID getIssueId();

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
    public abstract List<UUID> getAttachmentIds();

    @Null
    @Nullable
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(readOnly = true)
    public abstract List<OrderAttachmentJson> getAttachments();

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

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract ParticipantRole getSenderRole();

    @Nullable
    @Override
    public abstract ParticipantRole getRecipient();

    @Nullable
    @Override
    public abstract String getTitle();

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

    public static ContractorTimelineJson valueOf(final ContractorTimelineModel model) {
        final ImmutableContractorTimelineJson.Builder builder = ImmutableContractorTimelineJson.builder()
            .requestId(model.getRequestId())
            .issueId(model.getIssueId())
            .timelineId(model.getTimelineId())
            .attachmentIds(model.getAttachmentIds())
            .senderId(model.getSenderId())
            .senderName(model.getSenderName())
            .senderRole(model.getSenderRole())
            .recipient(model.getRecipient())
            .title(model.getTitle())
            .purpose(model.getPurpose())
            .message(model.getMessage())
            .createdAt(model.getCreatedAt())
            .modifiedAt(model.getModifiedAt());

        if (model instanceof ContractorTimelineJson contractorTimelineJson
            && contractorTimelineJson.getAttachments() != null) {
            builder.attachments(contractorTimelineJson.getAttachments());
        }

        return builder.build();
    }

    public abstract ContractorTimelineJson withAttachments(final Iterable<? extends OrderAttachmentJson> attachments);

}
