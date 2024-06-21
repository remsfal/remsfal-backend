package de.remsfal.core.json;

import de.remsfal.core.model.ProjectMemberModel;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.AddressModel;
import de.remsfal.core.model.SiteModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A site as part of a property")
@JsonDeserialize(as = ImmutableSiteJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class SiteJson implements SiteModel {

    @Null
    @Nullable
    public abstract String getId();

    @NotNull
    public abstract String getTitle();

    @NotNull
    public abstract AddressModel getAddress();

    public static SiteJson valueOf(SiteModel model) {
        return ImmutableSiteJson.builder()
                .id(model.getId())
                .title(model.getTitle())
                .address(AddressJson.valueOf(model.getAddress()))
                .description(model.getDescription())
                .rent(model.getRent())
                .usableSpace(model.getUsableSpace())
                .build();
    }
}
