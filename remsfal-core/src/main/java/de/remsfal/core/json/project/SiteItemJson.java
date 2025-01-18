package de.remsfal.core.json.project;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.project.SiteModel;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A site item with basic information")
@JsonDeserialize(as = ImmutableSiteItemJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class SiteItemJson {

    @NotNull
    public abstract String getId();

    @NotNull
    public abstract String getName();

    @NotNull
    public abstract String getTitle();

    public static SiteItemJson valueOf(final SiteModel model) {
        return ImmutableSiteItemJson.builder()
            .id(model.getId())
            .name(model.getTitle())
            .title(model.getTitle())
            .build();
    }

}
