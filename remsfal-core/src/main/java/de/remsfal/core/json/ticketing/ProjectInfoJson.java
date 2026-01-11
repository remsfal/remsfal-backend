package de.remsfal.core.json.ticketing;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Value.Immutable
@JsonDeserialize(as = ImmutableProjectInfoJson.class)
@Schema(name = "ProjectInfo", description = "Simple project information for inbox events")
public interface ProjectInfoJson {

    @Schema(description = "Project ID")
    String id();

    @Schema(description = "Project title")
    String title();
}
