package de.remsfal.core.json.project;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.RentalUnitModel;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import java.util.List;
import java.util.UUID;

@Immutable
@ImmutableStyle
@Schema(description = "A tree node representing a project entity")
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class RentalUnitTreeNodeJson {

    @Schema(description = "Key of the node", readOnly = true, required = true)
    public abstract UUID getKey();

    @Schema(description = "Data encapsulating node attributes", readOnly = true)
    public abstract RentalUnitNodeDataJson getData();

    @Schema(description = "Children nodes", readOnly = true)
    public abstract List<RentalUnitTreeNodeJson> getChildren();

    public static RentalUnitTreeNodeJson valueOf(final RentalUnitModel model) {
        return ImmutableRentalUnitTreeNodeJson.builder()
            .key(model.getId())
            .data(RentalUnitNodeDataJson.valueOf(model))
            .build();
    }

}
