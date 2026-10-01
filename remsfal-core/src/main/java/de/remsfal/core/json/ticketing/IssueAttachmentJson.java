package de.remsfal.core.json.ticketing;

import jakarta.annotation.Nullable;

import java.net.URI;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.UserContext;
import de.remsfal.core.model.ticketing.IssueAttachmentModel;

/**
 * Outbound representation of an issue attachment. Internal storage details (object name in the
 * object storage, uploader id) are intentionally not exposed; clients use {@link #getDownloadUrl()}.
 */
@Immutable
@ImmutableStyle
@Schema(description = "An issue attachment")
@JsonDeserialize(as = ImmutableIssueAttachmentJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class IssueAttachmentJson extends AttachmentJson {

    @Nullable
    public abstract UUID getIssueId();

    @Nullable
    public abstract UserContext getUploaderContext();

    public static IssueAttachmentJson valueOf(final IssueAttachmentModel model, final URI downloadUrl) {
        return ImmutableIssueAttachmentJson.builder()
            .issueId(model.getIssueId())
            .attachmentId(model.getAttachmentId())
            .fileName(model.getFileName())
            .contentType(model.getContentType())
            .uploadedBy(model.getUploadedBy())
            .uploaderContext(model.getUploaderContext())
            .createdAt(model.getCreatedAt())
            .downloadUrl(downloadUrl)
            .build();
    }

}
