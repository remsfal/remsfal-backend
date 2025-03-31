package de.remsfal.core.json.project;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@Schema(description = "A tree node representing a project entity")
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class RentalUnitTreeNodeJson {

    @Schema(description = "Key of the node", required = true, examples = "Property 1")
    public abstract String getKey();

    @Schema(description = "Data encapsulating node attributes")
    public abstract RentalUnitNodeDataJson getData();

    @Schema(description = "Children nodes")
    public abstract List<RentalUnitTreeNodeJson> getChildren();

}
