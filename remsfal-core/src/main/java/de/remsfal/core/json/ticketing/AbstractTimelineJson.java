package de.remsfal.core.json.ticketing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.core.model.ticketing.TimelineModel;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Fields shared by every timeline entry (tenant/manager issue timeline, contractor quotation
 * timeline). Not itself {@code @Immutable} — concrete leaf DTOs ({@link TenantTimelineJson},
 * {@link ContractorTimelineJson}) extend this and add whatever is specific to their context.
 */
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class AbstractTimelineJson implements TimelineModel {

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

}
