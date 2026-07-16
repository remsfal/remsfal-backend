package de.remsfal.core.json.ticketing;

import java.util.List;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.TimelineModel;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@Immutable
@ImmutableStyle
@Schema(description = "A list of issue timelines")
@JsonDeserialize(as = ImmutableTimelineListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class TimelineListJson {
    // Validation is not required, because it is read-only.

    @Schema(description = "Timeline entries", readOnly = true)
    public abstract List<TimelineJson> getTimelines();

    public static TimelineListJson valueOf(final List<? extends TimelineModel> timelines) {
        return ImmutableTimelineListJson.builder()
            .timelines(timelines.stream()
                .map(TimelineJson::valueOf)
                .toList())
            .build();
    }

}
