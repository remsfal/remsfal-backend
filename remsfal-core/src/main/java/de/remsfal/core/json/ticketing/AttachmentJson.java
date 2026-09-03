package de.remsfal.core.json.ticketing;

import jakarta.annotation.Nullable;

import java.time.Instant;
import java.util.UUID;

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
    public abstract String getObjectName();

    @Nullable
    public abstract UUID getUploaderId();

    @Nullable
    public abstract String getUploadedBy();

    @Nullable
    public abstract Instant getCreatedAt();

}
