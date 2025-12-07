package de.remsfal.core.json.quotation;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.remsfal.core.ImmutableStyle;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import java.util.List;
import java.util.UUID;

/**
 * JSON request for creating quotation requests.
 * 
 * @author GitHub Copilot
 */
@Immutable
@ImmutableStyle
@Schema(description = "Request to create quotation requests for contractors")
@JsonDeserialize(as = ImmutableCreateQuotationRequestJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class CreateQuotationRequestJson {

    @NotNull
    @NotEmpty
    @Schema(description = "List of contractor IDs to request quotations from", required = true)
    public abstract List<UUID> getContractorIds();

    @Nullable
    @Size(max = 5000)
    @Schema(description = "Description or additional information for the quotation request")
    public abstract String getDescription();

}
