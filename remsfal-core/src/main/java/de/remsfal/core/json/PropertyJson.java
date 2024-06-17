package de.remsfal.core.json;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.PropertyModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A property")
@JsonDeserialize(as = ImmutablePropertyJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class PropertyJson implements PropertyModel {

    @Nullable
    public abstract String getId();

    @NotNull
    public abstract String getTitle();

    @Nullable
    public abstract String getLandRegisterEntry();

    @Nullable
    public abstract String getDescription();

    @Nullable
    public abstract Integer getPlotArea();

    @Nullable
    public abstract Float getEffectiveSpace(); // living space + usable space + commercial space

    public static PropertyJson valueOf(PropertyModel model) {
	// TODO Auto-generated method stub
	return null;
    }

}
