package de.remsfal.core.json.ticketing;

import jakarta.annotation.Nullable;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class AttachmentJson {

    @Nullable
    public abstract UUID getAttachmentId();

    @Nullable
    public abstract String getFileName();

    @Nullable
    public abstract String getContentType();

    @Nullable
    public abstract String getUploadedBy();

    @Nullable
    public abstract Instant getCreatedAt();

    @Nullable
    @Schema(readOnly = true, description = "Root-relative URL to download the attachment for the requesting user")
    public abstract URI getDownloadUrl();

}
