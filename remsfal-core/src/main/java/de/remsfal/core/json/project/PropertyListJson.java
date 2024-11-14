package de.remsfal.core.json.project;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.remsfal.core.model.project.PropertyModel;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import java.util.List;

/**
 * Author: Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A list of properties")
@JsonDeserialize(as = ImmutablePropertyJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class PropertyListJson {

    @Schema(
            description = "Index of the first element in property list of total available entries, starting at 1",
            required = true,
            example = "1"
    )
    public abstract Integer getFirst();

    @Schema(
            description = "Number of elements in property list",
            minimum = "1",
            maximum = "100",
            defaultValue = "10",
            required = true
    )
    public abstract Integer getSize();

    @Schema(
            description = "Total number of available properties",
            required = true
    )
    public abstract Long getTotal();

    public abstract List<PropertyItemJson> getProperties();

    public static PropertyListJson valueOf(
            final List<? extends PropertyModel> properties,
            final int first,
            final long total
    ) {
        final ImmutablePropertyListJson.Builder builder = ImmutablePropertyListJson.builder();
        for (PropertyModel model : properties) {
            builder.addProperties(PropertyItemJson.valueOf(model));
        }
        return builder
                .size(properties.size())
                .first(first)
                .total(total)
                .build();
    }
}
