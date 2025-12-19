package de.remsfal.core.json.ticketing;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.time.Instant;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.QuotationModel;
import de.remsfal.core.validation.PostValidation;

/**
 * JSON DTO for a quotation response.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A quotation response")
@JsonDeserialize(as = ImmutableQuotationJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class QuotationJson implements QuotationModel {

    @Null
    @Nullable
    @Override
    public abstract UUID getId();

    @Null(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract UUID getProjectId();

    @Null(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract UUID getIssueId();

    @Null(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract UUID getRequesterId();

    @Null(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract UUID getContractorId();

    @NotBlank(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract String getText();

    @Null
    @Nullable
    @Override
    public abstract Instant getCreatedAt();

    @Null(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract QuotationStatus getStatus();

    public static QuotationJson valueOf(final QuotationModel model) {
        return ImmutableQuotationJson.builder()
                .id(model.getId())
                .projectId(model.getProjectId())
                .issueId(model.getIssueId())
                .requesterId(model.getRequesterId())
                .contractorId(model.getContractorId())
                .text(model.getText())
                .createdAt(model.getCreatedAt())
                .status(model.getStatus())
                .build();
    }

}
