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
    @Schema(readOnly = true, description = "Name of the project owner / billing recipient")
    @Override
    public abstract String getProjectOwner();

    @Nullable
    @Schema(readOnly = true, description = "Care of / representative on behalf of")
    @Override
    public abstract String getProjectCareOf();

    @Nullable
    @Schema(readOnly = true, description = "First billing address line (street)")
    @Override
    public abstract String getProjectBillingAddress1();

    @Nullable
    @Schema(readOnly = true, description = "Second billing address line (zip and city)")
    @Override
    public abstract String getProjectBillingAddress2();

    @Nullable
    @Schema(readOnly = true, description = "Third billing address line (province and country)")
    @Override
    public abstract String getProjectBillingAddress3();

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
    @Schema(readOnly = true, description = "Company name of the contractor")
    @Override
    public abstract String getContractorName();

    @Nullable
    @Schema(readOnly = true, description = "ID of the contractor's organization")
    @Override
    public abstract UUID getOrganizationId();

    @Nullable
    @Schema(description = "Status of the quotation: VALID, INVALID, ACCEPTED, REJECTED")
    @Override
    public abstract QuotationStatus getStatus();

    @Nullable
    public abstract List<OrderAttachmentJson> getAttachments();

    @Nullable
    @Schema(description = "Timestamp until which the quotation is valid")
    @Override
    public abstract Instant getValidUntil();

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

    /**
     * Creates a new {@link QuotationJson} instance with attachments added to an existing quotation.
     * <p>
     * This method allows adding attachment information to a QuotationJson without modifying the core model.
     * Useful for lazy-loading attachments only when needed.
     *
     * @param attachments the list of attachments to add
     * @return an immutable {@link QuotationJson} with attachments included
     */
    public abstract QuotationJson withAttachments(final Iterable<? extends OrderAttachmentJson> attachments);

}
