package de.remsfal.core.json;

import org.immutables.value.Value;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value.Immutable
@Schema(description = "Encapsulated data of a project tree node")
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public interface NodeDataJson {

    @Schema(description = "Type of the node (e.g., 'property', 'building')", required = true, examples = "property")
    String getType();

    @Schema(description = "Title of the node", examples = "Main Building")
    String getTitle();

    @Schema(description = "Description of the node", examples = "A multi-story office building")
    String getDescription();

    @Schema(description = "Name of the tenant associated with this node", examples = "Doe, John")
    String getTenant();

    @Schema(description = "Usable space in square meters", examples = "350.5")
    float getUsableSpace();
}
