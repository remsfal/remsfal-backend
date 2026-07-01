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

import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A request for quotation sent to a contractor")
@JsonDeserialize(as = ImmutableQuotationRequestJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class QuotationRequestJson extends OrderProcessJson implements QuotationRequestModel {

    @Nullable
    @Schema(readOnly = true, description = "ID of the user who initiated this request")
    @Override
    public abstract UUID getInitiatorId();

    @Nullable
    @Schema(readOnly = true, description = "Name of the user who initiated this request")
    @Override
    public abstract String getInitiatedBy();

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

    public abstract QuotationRequestJson withAttachments(final Iterable<? extends OrderAttachmentJson> attachments);

}
