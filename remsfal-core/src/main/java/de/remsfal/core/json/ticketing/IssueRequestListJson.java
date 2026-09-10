package de.remsfal.core.json.ticketing;

import java.util.List;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.IssueRequestModel;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@Immutable
@ImmutableStyle
@Schema(description = "A list of issue requests")
@JsonDeserialize(as = ImmutableIssueRequestListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class IssueRequestListJson {
    // Validation is not required, because it is read-only.

    @Schema(description = "Requests", readOnly = true)
    public abstract List<IssueRequestJson> getRequests();

    public static IssueRequestListJson valueOf(final List<? extends IssueRequestModel> requests) {
        return ImmutableIssueRequestListJson.builder()
            .requests(requests.stream()
                .map(IssueRequestJson::valueOf)
                .toList())
            .build();
    }

}
