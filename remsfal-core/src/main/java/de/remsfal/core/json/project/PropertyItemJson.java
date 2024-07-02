package de.remsfal.core.json.project;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.remsfal.core.model.project.PropertyModel;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A project item with the user's member role only")
@JsonDeserialize(as = ImmutablePropertyJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class PropertyItemJson {

    @NotNull
    public abstract String getId();

    @NotNull
    public abstract String getTitle();

    @NotNull
    public abstract String getLandRegisterEntry();

    @NotNull
    public abstract String getDescription();

    @NotNull
    public abstract Integer getPlotArea();

    @NotNull
    public abstract Float getEffectiveSpace();  // living space + usable space + commercial space

    public static PropertyItemJson valueOf(final PropertyModel model) {
        final ImmutablePropertyItemJson.Builder builder = ImmutablePropertyItemJson.builder()
            .id(model.getId())
            .title(model.getTitle())
            .landRegisterEntry(model.getLandRegisterEntry())
            .description(model.getDescription())
            .plotArea(model.getPlotArea())
            .effectiveSpace(0F);
        return builder.build();
    }

}
