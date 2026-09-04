package de.remsfal.core.json.ticketing;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.ContractorTimelineModel;
import de.remsfal.core.model.ticketing.ParticipantRole;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Null;

import java.util.List;
import java.util.UUID;

@Immutable
@ImmutableStyle
@Schema(description = "A contractor timeline entry")
@JsonDeserialize(as = ImmutableContractorTimelineJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ContractorTimelineJson extends AbstractTimelineJson implements ContractorTimelineModel {

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract UUID getRequestId();

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract UUID getContractorId();

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract UUID getOrganizationId();

    @Null
    @Nullable
    @JsonIgnore
    @Schema(readOnly = true, hidden = true)
    @Override
    public abstract UUID getTenancyId();

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract ParticipantRole getSenderRole();

    @Null
    @Nullable
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(readOnly = true)
    public abstract List<OrderAttachmentJson> getAttachments();

    public static ContractorTimelineJson valueOf(final ContractorTimelineModel model) {
        final ImmutableContractorTimelineJson.Builder builder = ImmutableContractorTimelineJson.builder()
            .requestId(model.getRequestId())
            .contractorId(model.getContractorId())
            .organizationId(model.getOrganizationId())
            .issueId(model.getIssueId())
            .timelineId(model.getTimelineId())
            .attachmentIds(model.getAttachmentIds())
            .senderId(model.getSenderId())
            .senderName(model.getSenderName())
            .senderRole(model.getSenderRole())
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
