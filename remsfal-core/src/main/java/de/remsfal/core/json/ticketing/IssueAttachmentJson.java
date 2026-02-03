package de.remsfal.core.json.ticketing;

import jakarta.annotation.Nullable;

import java.time.Instant;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.IssueAttachmentModel;

/**
 * @author GitHub Copilot
 */
@Immutable
@ImmutableStyle
@Schema(description = "An issue attachment")
@JsonDeserialize(as = ImmutableIssueAttachmentJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class IssueAttachmentJson implements IssueAttachmentModel {

    @Nullable
    @Override
    public abstract UUID getIssueId();

    @Nullable
    @Override
    public abstract UUID getAttachmentId();

    @Nullable
    @Override
    public abstract String getFileName();

    @Nullable
    @Override
    public abstract String getContentType();

    @Nullable
    @Override
    public abstract String getObjectName();

    @Nullable
    @Override
    public abstract UUID getUploadedBy();

    @Nullable
    @Override
    public abstract Instant getCreatedAt();

    public static IssueAttachmentJson valueOf(final IssueAttachmentModel model) {
        return ImmutableIssueAttachmentJson.builder()
            .issueId(model.getIssueId())
            .attachmentId(model.getAttachmentId())
            .fileName(model.getFileName())
            .contentType(model.getContentType())
            .objectName(model.getObjectName())
            .uploadedBy(model.getUploadedBy())
            .createdAt(model.getCreatedAt())
            .build();
    }

}
