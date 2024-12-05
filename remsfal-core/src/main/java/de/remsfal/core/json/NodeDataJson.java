package de.remsfal.core.json;

import org.immutables.value.Value;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value.Immutable
@Schema(description = "Encapsulated data of a project tree node")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public interface NodeDataJson {

    @Schema(description = "Type of the node (e.g., 'property', 'building')", required = true, example = "property")
    String getType();

    @Schema(description = "Title of the node", example = "Main Building")
    String getTitle();

    @Schema(description = "Description of the node", example = "A multi-story office building")
    String getDescription();

    @Schema(description = "Name of the tenant associated with this node", example = "Doe, John")
    String getTenant();

    @Schema(description = "Usable space in square meters", example = "350.5")
    float getUsableSpace();
}
