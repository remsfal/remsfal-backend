package de.remsfal.core.json.project;

import java.util.List;

import de.remsfal.core.model.project.SiteModel;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A list of sites")
@JsonDeserialize(as = ImmutableSiteListJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class SiteListJson {

    public abstract List<SiteItemJson> getSites();

    public static SiteListJson valueOf(final List<? extends SiteModel> sites) {
        final ImmutableSiteListJson.Builder builder = ImmutableSiteListJson.builder();
        for(SiteModel model : sites) {
            builder.addSites(SiteItemJson.valueOf(model));
        }
        return builder.build();
    }

}
