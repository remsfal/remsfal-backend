package de.remsfal.core.json.project;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.immutable.ImmutableStyle;
import de.remsfal.core.model.project.CommercialModel;
import de.remsfal.core.validation.PostValidation;
import de.remsfal.core.validation.Title;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "An commercial inside a building")
@JsonDeserialize(as = ImmutableCommercialJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class CommercialJson implements CommercialModel {

    @Null
    @Nullable
    @Override
    public abstract String getId();

    @Title
    @NotBlank(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract String getTitle();

    /**
     * Converts a {@link CommercialModel} to a {@link CommercialJson}.
     *
     * @param model the {@link CommercialModel} instance to convert.
     * @return an immutable {@link CommercialJson} instance.
     */
    public static CommercialJson valueOf(final CommercialModel model) {
        return ImmutableCommercialJson.builder()
                .id(model.getId())
                .title(model.getTitle())
                .location(model.getLocation())
                .description(model.getDescription())
//                .commercialSpace(model.getCommercialSpace())
  //              .usableSpace(model.getUsableSpace())
                .heatingSpace(model.getHeatingSpace())
                .build();
    }

}
