package de.remsfal.core.json.quotation;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.quotation.QuotationRequestModel;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import java.util.UUID;

/**
 * JSON representation of a quotation request.
 *
 * @author GitHub Copilot
 */
@Immutable
@ImmutableStyle
@Schema(description = "A quotation request")
@JsonDeserialize(as = ImmutableQuotationRequestJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class QuotationRequestJson implements QuotationRequestModel {

    @Nullable
    @Override
    public abstract UUID getId();

    @Nullable
    @Override
    public abstract UUID getProjectId();

    @Nullable
    @Override
    public abstract UUID getIssueId();

    @Nullable
    @Override
    public abstract UUID getContractorId();

    @Nullable
    @Override
    public abstract UUID getTriggeredBy();

    @Nullable
    @Override
    public abstract String getDescription();

    @Nullable
    @Override
    public abstract Status getStatus();

    public static QuotationRequestJson valueOf(final QuotationRequestModel model) {
        if (model == null) {
            return null;
        }
        return ImmutableQuotationRequestJson.builder()
            .id(model.getId())
            .projectId(model.getProjectId())
            .issueId(model.getIssueId())
            .contractorId(model.getContractorId())
            .triggeredBy(model.getTriggeredBy())
            .description(model.getDescription())
            .status(model.getStatus())
            .build();
    }

}
