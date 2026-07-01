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
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A quotation response submitted by a contractor")
@JsonDeserialize(as = ImmutableQuotationJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class QuotationJson extends OrderProcessJson implements QuotationModel {

    @Nullable
    @Schema(readOnly = true, description = "ID of the quotation request this quotation responds to")
    @Override
    public abstract UUID getRequestId();

    @Nullable
    @Schema(readOnly = true, description = "ID of the user who submitted this quotation")
    @Override
    public abstract UUID getOffererId();

    @Nullable
    @Schema(readOnly = true, description = "Name of the user who submitted this quotation")
    @Override
    public abstract String getOfferedBy();

    @Nullable
    @Schema(description = "Status of the quotation: VALID, INVALID, ACCEPTED, REJECTED")
    @Override
    public abstract QuotationStatus getStatus();

    @Nullable
    @Schema(description = "Timestamp until which the quotation is valid")
    @Override
    public abstract Instant getValidUntil();

    public static QuotationJson valueOf(final QuotationModel model) {
        return ImmutableQuotationJson.builder()
            .id(model.getId())
            .issueId(model.getIssueId())
            .requestId(model.getRequestId())
            .projectId(model.getProjectId())
            .projectOwner(model.getProjectOwner())
            .projectCareOf(model.getProjectCareOf())
            .projectBillingAddress1(model.getProjectBillingAddress1())
            .projectBillingAddress2(model.getProjectBillingAddress2())
            .projectBillingAddress3(model.getProjectBillingAddress3())
            .offererId(model.getOffererId())
            .offeredBy(model.getOfferedBy())
            .contractorId(model.getContractorId())
            .contractorName(model.getContractorName())
            .organizationId(model.getOrganizationId())
            .status(model.getStatus())
            .validUntil(model.getValidUntil())
            .createdAt(model.getCreatedAt())
            .modifiedAt(model.getModifiedAt())
            .build();
    }

    public abstract QuotationJson withAttachments(final Iterable<? extends OrderAttachmentJson> attachments);

}
