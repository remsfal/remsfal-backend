package de.remsfal.core.json.project;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.json.AddressJson;
import de.remsfal.core.model.project.SiteModel;
import de.remsfal.core.validation.NullOrNotBlank;
import de.remsfal.core.validation.PostValidation;

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
    @Override
    public abstract String getId();

    @NullOrNotBlank
    @NotBlank(groups = PostValidation.class)
    @Size(max=255)
    @Nullable
    @Override
    public abstract String getTitle();

    @Valid
    @NotNull(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract AddressJson getAddress();
    
    @Nullable
    @Override
    public abstract String getDescription();

    @Valid
    @Nullable
    @Override
    public abstract TenancyJson getTenancy();

    @Nullable
    @Override
    public abstract Float getUsableSpace();

    public static SiteJson valueOf(final SiteModel model) {
        return ImmutableSiteJson.builder()
            .id(model.getId())
            .title(model.getTitle())
            .address(AddressJson.valueOf(model.getAddress()))
            .description(model.getDescription())
            .tenancy(TenancyJson.valueOf(model.getTenancy()))
            .usableSpace(model.getUsableSpace())
            .build();
    }

}
