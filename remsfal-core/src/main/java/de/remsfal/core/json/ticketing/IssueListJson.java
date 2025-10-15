package de.remsfal.core.json.ticketing;

import java.util.List;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.IssueModel;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A list of issues")
@JsonDeserialize(as = ImmutableIssueListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class IssueListJson {
    // Validation is not required, because it is read-only.

    @Schema(description = "Index of the first element in list of total available entries, starting at 1",
        required = true, examples = "1")
    public abstract Integer getFirst();

    @Schema(description = "Number of elements in list", minimum = "1", maximum = "100",
        defaultValue = "10", required = true)
    public abstract Integer getSize();

    @Schema(description = "Total number of available elements", required = true)
    public abstract Integer getTotal();

    public abstract List<IssueItemJson> getIssues();

    public static IssueListJson valueOf(final List<? extends IssueModel> issues,
        final int first, final int total) {

        return ImmutableIssueListJson.builder()
            .size(issues.size())
            .first(first)
            .total(total)
            .issues(issues.stream()
                .map(IssueItemJson::valueOf)
                .toList())
            .build();
    }

}
