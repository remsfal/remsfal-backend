package de.remsfal.core.json.ticketing;

import java.util.List;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.TenantTimelineModel;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@Immutable
@ImmutableStyle
@Schema(description = "A list of tenant timelines")
@JsonDeserialize(as = ImmutableTenantTimelineListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class TenantTimelineListJson {
    // Validation is not required, because it is read-only.

    @Schema(description = "Number of tenant timeline entries in the list", required = true)
    public abstract Integer getSize();

    public abstract List<TenantTimelineJson> getTimelines();

    public static TenantTimelineListJson valueOf(final List<? extends TenantTimelineModel> timelines) {
        return ImmutableTenantTimelineListJson.builder()
            .size(timelines.size())
            .timelines(timelines.stream()
                .map(TenantTimelineJson::valueOf)
                .toList())
            .build();
    }

}