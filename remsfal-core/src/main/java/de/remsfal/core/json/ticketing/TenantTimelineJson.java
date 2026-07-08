package de.remsfal.core.json.ticketing;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.TenantTimelineModel;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

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
    public abstract List<UUID> getAttachmentId();

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
        return ImmutableTenantTimelineJson.builder()
            .issueId(model.getIssueId())
            .tenancyId(model.getTenancyId())
            .timelineId(model.getTimelineId())
            .projectId(model.getProjectId())
            .attachmentId(model.getAttachmentId())
            .senderId(model.getSenderId())
            .senderName(model.getSenderName())
            .title(model.getTitle())
            .message(model.getMessage())
            .createdAt(model.getCreatedAt())
            .modifiedAt(model.getModifiedAt())
            .build();
    }

}
