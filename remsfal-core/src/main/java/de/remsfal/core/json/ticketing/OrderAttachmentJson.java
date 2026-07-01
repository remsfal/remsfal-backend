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
import de.remsfal.core.model.ticketing.OrderAttachmentModel;

@Immutable
@ImmutableStyle
@Schema(description = "An attachment associated with a quotation request, quotation, or order placement")
@JsonDeserialize(as = ImmutableOrderAttachmentJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class OrderAttachmentJson implements OrderAttachmentModel {

    @Nullable
    @Override
    public abstract OrderProcessPhase getProcessPhase();

    @Nullable
    @Override
    public abstract UUID getProcessId();

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
    public abstract UUID getUploaderId();

    @Nullable
    @Override
    public abstract String getUploadedBy();

    @Nullable
    @Override
    public abstract Instant getCreatedAt();

    public static OrderAttachmentJson valueOf(final OrderAttachmentModel model) {
        return ImmutableOrderAttachmentJson.builder()
            .processPhase(model.getProcessPhase())
            .processId(model.getProcessId())
            .attachmentId(model.getAttachmentId())
            .fileName(model.getFileName())
            .contentType(model.getContentType())
            .objectName(model.getObjectName())
            .uploaderId(model.getUploaderId())
            .uploadedBy(model.getUploadedBy())
            .createdAt(model.getCreatedAt())
            .build();
    }

}
