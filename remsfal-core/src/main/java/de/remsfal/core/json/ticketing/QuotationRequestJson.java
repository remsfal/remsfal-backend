package de.remsfal.core.json.ticketing;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.QuotationRequestModel;
import de.remsfal.core.model.ticketing.QuotationRequestModel.RequestStatus;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A request for quotation sent to a contractor")
@JsonDeserialize(as = ImmutableQuotationRequestJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class QuotationRequestJson {

    @Nullable
    @Schema(readOnly = true, description = "Unique identifier of the quotation request")
    public abstract UUID getId();

    @Nullable
    @Schema(readOnly = true, description = "ID of the issue this request belongs to")
    public abstract UUID getIssueId();

    @Nullable
    @Schema(readOnly = true, description = "ID of the project this request belongs to")
    public abstract UUID getProjectId();

    @Nullable
    @Schema(readOnly = true, description = "ID of the user who triggered this request")
    public abstract UUID getTriggerId();

    @Nullable
    @Schema(readOnly = true, description = "ID of the contractor this request was sent to")
    public abstract UUID getContractorId();

    @Nullable
    @Schema(readOnly = true, description = "ID of the organization of the contractor")
    public abstract UUID getOrganizationId();

    @Nullable
    @Size(max = 5000)
    @Schema(description = "Scope of work description for the contractor")
    public abstract String getScopeOfWork();

    @Nullable
    @Schema(description = "Status of the request: REQUESTED, WITHDRAWN, VIEWING_REQUIRED,"
            + "CONSULTATION_REQUIRED, REJECTED, SUBMITTED")
    public abstract RequestStatus getStatus();

    @Nullable
    @Schema(readOnly = true)
    public abstract Instant getCreatedAt();

    @Nullable
    @Schema(readOnly = true)
    public abstract Instant getModifiedAt();

    public static QuotationRequestJson valueOf(final QuotationRequestModel model) {
        return ImmutableQuotationRequestJson.builder()
            .id(model.getId())
            .issueId(model.getIssueId())
            .projectId(model.getProjectId())
            .triggerId(model.getTriggerId())
            .contractorId(model.getContractorId())
            .organizationId(model.getOrganizationId())
            .scopeOfWork(model.getScopeOfWork())
            .status(model.getStatus())
            .createdAt(model.getCreatedAt())
            .modifiedAt(model.getModifiedAt())
            .build();
    }

}
