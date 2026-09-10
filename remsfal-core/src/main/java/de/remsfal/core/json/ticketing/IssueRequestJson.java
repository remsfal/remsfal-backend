package de.remsfal.core.json.ticketing;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.IssueRequestModel;

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
@Schema(description = "A request from a contractor to a tenant about an issue")
@JsonDeserialize(as = ImmutableIssueRequestJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class IssueRequestJson implements IssueRequestModel {

    @Null
    @Nullable
    @JsonIgnore
    @Schema(readOnly = true, hidden = true)
    @Override
    public abstract UUID getIssueId();

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract UUID getOrganizationId();

    @NotNull
    @Nullable
    @Override
    public abstract String getMessage();

    @Nullable
    @Schema(description = "IDs of attachments the contractor is referring to or requesting the tenant to provide")
    @Override
    public abstract List<UUID> getAttachmentIds();

    @Nullable
    @Schema(description = "If true, the message is also copied into the tenant timeline of the issue")
    public abstract Boolean getMessageToTenant();

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

    public static IssueRequestJson valueOf(final IssueRequestModel model) {
        return ImmutableIssueRequestJson.builder()
            .issueId(model.getIssueId())
            .organizationId(model.getOrganizationId())
            .message(model.getMessage())
            .attachmentIds(model.getAttachmentIds())
            .createdAt(model.getCreatedAt())
            .modifiedAt(model.getModifiedAt())
            .build();
    }

}
