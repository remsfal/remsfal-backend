package de.remsfal.core.json.project;

import java.util.List;

import de.remsfal.core.model.project.BuildingModel;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A list of building")
@JsonDeserialize(as = ImmutableBuildingListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class BuildingListJson {

    public abstract List<BuildingItemJson> getBuildings();

    public static BuildingListJson valueOf(final List<? extends BuildingModel> buildings) {
        final ImmutableBuildingListJson.Builder builder = ImmutableBuildingListJson.builder();
        for(BuildingModel model : buildings) {
            builder.addBuildings(BuildingItemJson.valueOf(model));
        }
        return builder.build();
    }

}
