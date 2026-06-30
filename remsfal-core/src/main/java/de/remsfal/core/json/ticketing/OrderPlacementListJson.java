package de.remsfal.core.json.ticketing;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.OrderPlacementModel;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import java.util.List;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A list of order placements")
@JsonDeserialize(as = ImmutableOrderPlacementListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class OrderPlacementListJson {

    public abstract List<OrderPlacementJson> getItems();

    public static OrderPlacementListJson valueOf(final List<? extends OrderPlacementModel> items) {
        return ImmutableOrderPlacementListJson.builder()
            .items(items.stream().map(OrderPlacementJson::valueOf).toList())
            .build();
    }

}
