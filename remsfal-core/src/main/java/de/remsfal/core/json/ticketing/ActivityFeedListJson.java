package de.remsfal.core.json.ticketing;

import java.util.List;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.ActivityFeedModel;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.annotation.Nullable;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A cursor-paginated list of activities")
@JsonDeserialize(as = ImmutableActivityFeedListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ActivityFeedListJson {
    // Validation is not required, because it is read-only.

    @Schema(description = "Opaque cursor to fetch the next page with; absent/null if there is no further page",
        readOnly = true)
    @Nullable
    public abstract String getNextCursor();

    @Schema(description = "Number of elements in this page", minimum = "0", maximum = "500",
        readOnly = true, required = true)
    public abstract Integer getSize();

    public abstract List<ActivityFeedJson> getActivities();

    public static ActivityFeedListJson valueOf(final List<? extends ActivityFeedModel> activities,
        final String nextCursor) {
        return ImmutableActivityFeedListJson.builder()
            .size(activities.size())
            .nextCursor(nextCursor)
            .activities(activities.stream()
                .map(ActivityFeedJson::valueOf)
                .toList())
            .build();
    }

}
