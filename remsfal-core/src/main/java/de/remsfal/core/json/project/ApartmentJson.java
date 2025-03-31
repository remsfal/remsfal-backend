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
import de.remsfal.core.model.project.ApartmentModel;
import de.remsfal.core.validation.PostValidation;
import de.remsfal.core.validation.Title;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "An apartment inside a building")
@JsonDeserialize(as = ImmutableApartmentJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ApartmentJson implements ApartmentModel {

    @Null
    @Nullable
    @Override
    public abstract String getId();

    @Title
    @NotBlank(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract String getTitle();

    @Nullable
    @Override
    public abstract String getLocation();

    @Nullable
    @Override
    public abstract String getDescription();

    @Nullable
    @Override
    public abstract Float getLivingSpace();

    @Nullable
    @Override
    public abstract Float getUsableSpace();

    @Nullable
    @Override
    public abstract Float getHeatingSpace();

    @Nullable
    @Override
    public abstract TenancyJson getTenancy();

    public static ApartmentJson valueOf(ApartmentModel apartment) {
        return apartment == null ? null : ImmutableApartmentJson.builder()
                .id(apartment.getId())
                .title(apartment.getTitle())
                .description(apartment.getDescription())
                .heatingSpace(apartment.getHeatingSpace())
                .livingSpace(apartment.getLivingSpace())
                .usableSpace(apartment.getUsableSpace())
                .location(apartment.getLocation())
                .tenancy(TenancyJson.valueOf(apartment.getTenancy()))
                .build();

    }

}
