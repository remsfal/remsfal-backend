package de.remsfal.core.json.ticketing;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.ContractorJson;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import java.util.List;
import java.util.UUID;

@Immutable
@ImmutableStyle
@Schema(description = "A request to create one quotation request per contractor")
@JsonDeserialize(as = ImmutableCreateQuotationRequestJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class CreateQuotationRequestJson {

    @NotEmpty
    public abstract List<@NotNull ContractorJson> getContractors();

    @Nullable
    @Size(max = 5000)
    public abstract String getScopeOfWork();

    @Nullable
    @Schema(description = "IDs of existing issue attachments to share with the contractors")
    public abstract List<@NotNull UUID> getAttachmentIds();

}
