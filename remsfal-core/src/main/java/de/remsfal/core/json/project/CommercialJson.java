package de.remsfal.core.json.project;

import jakarta.annotation.Nullable;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.immutable.ImmutableStyle;
import de.remsfal.core.model.project.CommercialModel;

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
    public abstract String getId();

    @NotNull
    public abstract String getTitle();

    @Null
    @Nullable
    public abstract String getLocation();

    @Null
    @Nullable
    public abstract String getDescription();

    @Null
    @Nullable
    public abstract Float getCommercialSpace();

    @Null
    @Nullable
    public abstract Float getUsableSpace();

    @Null
    @Nullable
    public abstract Float getHeatingSpace();

    @Null
    @Nullable
    public abstract TenancyJson getTenancy();

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
                .commercialSpace(model.getCommercialSpace())
                .usableSpace(model.getUsableSpace())
                .heatingSpace(model.getHeatingSpace())
                .tenancy(TenancyJson.valueOf(model.getTenancy()))
                .build();
    }

}
