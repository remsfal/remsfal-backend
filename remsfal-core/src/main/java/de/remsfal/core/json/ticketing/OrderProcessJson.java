package de.remsfal.core.json.ticketing;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.ticketing.OrderProcessModel;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class OrderProcessJson implements OrderProcessModel {

    @Nullable
    @Schema(readOnly = true, description = "Unique identifier")
    @Override
    public abstract UUID getId();

    @Nullable
    @Schema(readOnly = true, description = "ID of the issue this process belongs to")
    @Override
    public abstract UUID getIssueId();

    @Nullable
    @Schema(readOnly = true, description = "ID of the project this process belongs to")
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
    @Schema(readOnly = true, description = "ID of the contractor")
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
    @Schema(readOnly = true)
    @Override
    public abstract Instant getCreatedAt();

    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract Instant getModifiedAt();

    @Nullable
    public abstract List<OrderAttachmentJson> getAttachments();

}
