package de.remsfal.core.json.ticketing;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.OrderPlacementModel;
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
@Schema(description = "An order placement created by a manager based on a quotation")
@JsonDeserialize(as = ImmutableOrderPlacementJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class OrderPlacementJson implements OrderPlacementModel {

    @Nullable
    @Schema(readOnly = true, description = "Unique identifier of the order placement")
    @Override
    public abstract UUID getId();

    @Nullable
    @Schema(readOnly = true, description = "ID of the issue this order belongs to")
    @Override
    public abstract UUID getIssueId();

    @Nullable
    @Schema(readOnly = true, description = "ID of the quotation this order is based on")
    @Override
    public abstract UUID getQuotationId();

    @Nullable
    @Schema(readOnly = true, description = "ID of the project this order belongs to")
    @Override
    public abstract UUID getProjectId();

    @Nullable
    @Schema(readOnly = true, description = "ID of the user who placed the order")
    @Override
    public abstract UUID getOrdererId();

    @Nullable
    @Schema(readOnly = true, description = "Name of the user who placed the order")
    @Override
    public abstract String getOrderedBy();

    @Nullable
    @Schema(readOnly = true, description = "ID of the contractor receiving this order")
    @Override
    public abstract UUID getContractorId();

    @Nullable
    @Schema(readOnly = true, description = "ID of the organization of the contractor")
    @Override
    public abstract UUID getOrganizationId();

    @Nullable
    @Schema(readOnly = true, description = "ID of the user who confirmed or rejected the order")
    @Override
    public abstract UUID getConfirmorId();

    @Nullable
    @Schema(readOnly = true, description = "Name of the user who confirmed or rejected the order")
    @Override
    public abstract String getConfirmedBy();

    @Nullable
    @Schema(description = "Status of the order placement: PLACED, CONFIRMED, REJECTED, WITHDRAWN")
    @Override
    public abstract OrderPlacementStatus getStatus();

    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract Instant getCreatedAt();

    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract Instant getModifiedAt();

    public static OrderPlacementJson valueOf(final OrderPlacementModel model) {
        return ImmutableOrderPlacementJson.builder()
            .id(model.getId())
            .issueId(model.getIssueId())
            .quotationId(model.getQuotationId())
            .projectId(model.getProjectId())
            .ordererId(model.getOrdererId())
            .orderedBy(model.getOrderedBy())
            .contractorId(model.getContractorId())
            .organizationId(model.getOrganizationId())
            .confirmorId(model.getConfirmorId())
            .confirmedBy(model.getConfirmedBy())
            .status(model.getStatus())
            .createdAt(model.getCreatedAt())
            .modifiedAt(model.getModifiedAt())
            .build();
    }

}
