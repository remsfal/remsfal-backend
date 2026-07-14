package de.remsfal.core.json.ticketing;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.TenantTimelineModel;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.annotation.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Immutable
@ImmutableStyle
@Schema(description = "A tenant timeline entry")
@JsonDeserialize(as = ImmutableTenantTimelineJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class TenantTimelineJson implements TenantTimelineModel {

    @Nullable
    @Override
    public abstract UUID getIssueId();

    @Nullable
    @Override
    public abstract UUID getTenancyId();

    @Nullable
    @Override
    public abstract UUID getTimelineId();

    @Nullable
    @Override
    public abstract UUID getProjectId();

    @Nullable
    @Override
    @JsonIgnore
    public abstract List<UUID> getAttachmentIds();
    
    @Nullable
    public abstract List<IssueAttachmentJson> getAttachments();

    @Nullable
    @Override
    public abstract UUID getSenderId();

    @Nullable
    @Override
    public abstract String getSenderName();

    @Nullable
    @Override
    public abstract String getTitle();

    @Nullable
    @Override
    public abstract String getMessage();

    @Nullable
    @Override
    public abstract Instant getCreatedAt();

    @Nullable
    @Override
    public abstract Instant getModifiedAt();

    public static TenantTimelineJson valueOf(final TenantTimelineModel model) {
        final ImmutableTenantTimelineJson.Builder builder = ImmutableTenantTimelineJson.builder()
            .issueId(model.getIssueId())
            .tenancyId(model.getTenancyId())
            .timelineId(model.getTimelineId())
            .projectId(model.getProjectId())
            .attachmentIds(model.getAttachmentIds())
            .senderId(model.getSenderId())
            .senderName(model.getSenderName())
            .title(model.getTitle())
            .message(model.getMessage())
            .createdAt(model.getCreatedAt())
            .modifiedAt(model.getModifiedAt());

        if (model instanceof TenantTimelineJson timelineJson && timelineJson.getAttachments() != null) {
            builder.attachments(timelineJson.getAttachments());
        }

        return builder.build();
    }

    public abstract TenantTimelineJson withAttachments(final Iterable<? extends IssueAttachmentJson> attachments);

}