package de.remsfal.core.json.project;

import de.remsfal.core.validation.PostValidation;
import de.remsfal.core.validation.Title;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.project.PropertyModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A property")
@JsonDeserialize(as = ImmutablePropertyJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class PropertyJson implements PropertyModel {

    @Null
    @Override
    public abstract String getId();

    @Title
    @NotBlank(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract String getTitle();

    public static PropertyJson valueOf(final PropertyModel model) {
        return ImmutablePropertyJson.builder()
                .id(model.getId())
                .title(model.getTitle())
                .description(model.getDescription())
                .plotArea(model.getPlotArea())
                .build();
    }

}
