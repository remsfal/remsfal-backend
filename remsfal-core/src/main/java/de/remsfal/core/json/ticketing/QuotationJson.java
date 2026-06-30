package de.remsfal.core.json.ticketing;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.QuotationModel;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A quotation response submitted by a contractor")
@JsonDeserialize(as = ImmutableQuotationJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class QuotationJson implements QuotationModel {

    @Nullable
    @Schema(readOnly = true, description = "Unique identifier of the quotation")
    @Override
    public abstract UUID getId();

    @Nullable
    @Schema(readOnly = true, description = "ID of the issue this quotation belongs to")
    @Override
    public abstract UUID getIssueId();

    @Nullable
    @Schema(readOnly = true, description = "ID of the quotation request this quotation responds to")
    @Override
    public abstract UUID getRequestId();

    @Nullable
    @Schema(readOnly = true, description = "ID of the project this quotation belongs to")
    @Override
    public abstract UUID getProjectId();

    @Nullable
    @Schema(readOnly = true, description = "ID of the user who submitted this quotation")
    @Override
    public abstract UUID getOffererId();

    @Nullable
    @Schema(readOnly = true, description = "Name of the user who submitted this quotation")
    @Override
    public abstract String getOfferedBy();

    @Nullable
    @Schema(readOnly = true, description = "ID of the contractor submitting the quotation")
    @Override
    public abstract UUID getContractorId();

    @Nullable
    @Schema(description = "Attachment IDs associated with the quotation")
    @Override
    public abstract List<UUID> getAttachments();

    @Nullable
    @Schema(description = "Timestamp until which the quotation is valid")
    @Override
    public abstract Instant getValidUntil();

    @Nullable
    @Schema(description = "Status of the quotation: VALID, INVALID, ACCEPTED, REJECTED")
    @Override
    public abstract QuotationStatus getStatus();

    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract Instant getCreatedAt();

    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract Instant getModifiedAt();

    public static QuotationJson valueOf(final QuotationModel model) {
        return ImmutableQuotationJson.builder()
            .id(model.getId())
            .issueId(model.getIssueId())
            .requestId(model.getRequestId())
            .projectId(model.getProjectId())
            .offererId(model.getOffererId())
            .offeredBy(model.getOfferedBy())
            .contractorId(model.getContractorId())
            .attachments(model.getAttachments())
            .validUntil(model.getValidUntil())
            .status(model.getStatus())
            .createdAt(model.getCreatedAt())
            .modifiedAt(model.getModifiedAt())
            .build();
    }

}
