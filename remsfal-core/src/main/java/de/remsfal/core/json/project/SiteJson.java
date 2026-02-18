package de.remsfal.core.json.project;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.AddressJson;
import de.remsfal.core.json.RentalUnitJson;
import de.remsfal.core.model.project.SiteModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A site as part of a property")
@JsonDeserialize(as = ImmutableSiteJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class SiteJson extends RentalUnitJson implements SiteModel {

    @Valid
    @Nullable
    @Override
    public abstract AddressJson getAddress();

    public static SiteJson valueOf(final SiteModel model) {
        return ImmutableSiteJson.builder()
            .id(model.getId())
            .title(model.getTitle())
            .address(AddressJson.valueOf(model.getAddress()))
            .location(model.getLocation())
            .description(model.getDescription())
            .outdoorArea(model.getOutdoorArea())
            .build();
    }

}
