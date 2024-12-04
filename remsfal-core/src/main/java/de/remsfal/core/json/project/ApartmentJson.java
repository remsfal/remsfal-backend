package de.remsfal.core.json.project;

import de.remsfal.core.model.project.TenancyModel;
import jakarta.annotation.Nullable;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.project.ApartmentModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
//ApartmentItemJson & if needed ApartmentListJson supplementable
@Value.Immutable
@Schema(description = "An apartment inside a building")
@JsonDeserialize(as = ImmutableApartmentJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class ApartmentJson implements ApartmentModel {

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
    public abstract Float getLivingSpace();

    @Null
    @Nullable
    public abstract Float getUsableSpace();

    @Null
    @Nullable
    public abstract Float getHeatingSpace();

    //attribute rent does not exist
    @Null
    @Nullable
    public abstract Float getRent();

    public static ApartmentJson valueOf(ApartmentModel apartment) {
        if(apartment == null) {
            return null;
        }

        String location = apartment.getLocation();
        if(location == null) {
            location = "";
        }

        String description = apartment.getDescription();
        if(description == null) {
            description = "";
        }

        Float livingSpace = apartment.getLivingSpace();
        if(livingSpace == null) {
            livingSpace = 0f;
        }

        Float heatingSpace = apartment.getHeatingSpace();
        if(heatingSpace == null) {
            heatingSpace = 0f;
        }

        String title = apartment.getTitle();
        if(title == null) {
            title = "";
        }

        //tenancy ignored

        Float usableSpace = apartment.getUsableSpace();
        if(usableSpace == null) {
            usableSpace = 0f;
        }

        return ImmutableApartmentJson.builder()
                .id(apartment.getId())
                .title(title)
                .description(description)
                .heatingSpace(heatingSpace)
                .livingSpace(livingSpace)
                .usableSpace(usableSpace)
                .location(location)
                .build();

    }

}
