package de.remsfal.core.json.ticketing;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.QuotationRequestModel;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
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
@Schema(description = "A request for quotation sent to a contractor")
@JsonDeserialize(as = ImmutableQuotationRequestJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class QuotationRequestJson implements QuotationRequestModel {

    @Nullable
    @Schema(readOnly = true, description = "Unique identifier of the quotation request")
    @Override
    public abstract UUID getId();

    @Nullable
    @Schema(readOnly = true, description = "ID of the issue this request belongs to")
    @Override
    public abstract UUID getIssueId();

    @Nullable
    @Schema(readOnly = true, description = "ID of the project this request belongs to")
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
    @Schema(readOnly = true, description = "ID of the user who initiated this request")
    @Override
    public abstract UUID getInitiatorId();

    @Nullable
    @Schema(readOnly = true, description = "Name of the user who initiated this request")
    @Override
    public abstract String getInitiatedBy();

    @Nullable
    @Schema(readOnly = true, description = "ID of the contractor this request was sent to")
    @Override
    public abstract UUID getContractorId();

    @Nullable
    @Schema(readOnly = true, description = "Company name of the contractor")
    @Override
    public abstract String getContractorName();

    @Nullable
    @Schema(readOnly = true, description = "ID of the organization of the contractor")
    @Override
    public abstract UUID getOrganizationId();

    @Nullable
    @Schema(description = "Status of the request: REQUESTED, WITHDRAWN, VIEWING_REQUIRED,"
            + "CONSULTATION_REQUIRED, REJECTED, SUBMITTED")
    @Override
    public abstract RequestStatus getStatus();

    @Nullable
    @Size(max = 5000)
    @Schema(description = "Scope of work description for the contractor")
    @Override
    public abstract String getScopeOfWork();

    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract Instant getCreatedAt();

    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract Instant getModifiedAt();

    @Nullable
    public abstract List<OrderAttachmentJson> getAttachments();

    public static QuotationRequestJson valueOf(final QuotationRequestModel model) {
        return ImmutableQuotationRequestJson.builder()
            .id(model.getId())
            .issueId(model.getIssueId())
            .projectId(model.getProjectId())
            .projectOwner(model.getProjectOwner())
            .projectCareOf(model.getProjectCareOf())
            .projectBillingAddress1(model.getProjectBillingAddress1())
            .projectBillingAddress2(model.getProjectBillingAddress2())
            .projectBillingAddress3(model.getProjectBillingAddress3())
            .initiatorId(model.getInitiatorId())
            .initiatedBy(model.getInitiatedBy())
            .contractorId(model.getContractorId())
            .contractorName(model.getContractorName())
            .organizationId(model.getOrganizationId())
            .status(model.getStatus())
            .scopeOfWork(model.getScopeOfWork())
            .createdAt(model.getCreatedAt())
            .modifiedAt(model.getModifiedAt())
            .build();
    }

    /**
     * Creates a new {@link QuotationRequestJson} instance with attachments added to an existing request.
     * <p>
     * This method allows adding attachment information to a QuotationRequestJson without modifying the core model.
     * Useful for lazy-loading attachments only when needed.
     *
     * @param attachments the list of attachments to add
     * @return an immutable {@link QuotationRequestJson} with attachments included
     */
    public abstract QuotationRequestJson withAttachments(final Iterable<? extends OrderAttachmentJson> attachments);

}
