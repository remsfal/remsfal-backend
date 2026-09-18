package de.remsfal.core.json.ticketing;

import java.util.List;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.ContractorTimelineModel;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@Immutable
@ImmutableStyle
@Schema(description = "A list of contractor timelines")
@JsonDeserialize(as = ImmutableContractorTimelineListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ContractorTimelineListJson {
    // Validation is not required, because it is read-only.

    @Schema(description = "Timeline entries", readOnly = true)
    public abstract List<ContractorTimelineJson> getTimelines();

    public static ContractorTimelineListJson valueOf(final List<? extends ContractorTimelineModel> timelines) {
        return ImmutableContractorTimelineListJson.builder()
            .timelines(timelines.stream()
                .map(ContractorTimelineJson::valueOf)
                .toList())
            .build();
    }

}
