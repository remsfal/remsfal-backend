package de.remsfal.core.json;

import java.util.List;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ContractorModel;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@Immutable
@ImmutableStyle
@Schema(description = "A list of contractors")
@JsonDeserialize(as = ImmutableContractorListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ContractorListJson {

    @Schema(description = "Index of the first element", readOnly = true)
    public abstract Integer getOffset();

    @Schema(description = "Total number of available contractors", readOnly = true)
    public abstract Long getTotal();

    public abstract List<ContractorJson> getContractors();

    public static ContractorListJson valueOf(
        final List<? extends ContractorModel> models,
        final Integer offset,
        final Long total) {

        return ImmutableContractorListJson.builder()
            .offset(offset)
            .total(total)
            .contractors(models.stream().map(ContractorJson::valueOf).toList())
            .build();
    }
}
