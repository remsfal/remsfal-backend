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

    public abstract List<IssueItemJson> getTasks();

    public static IssueListJson valueOf(final List<? extends IssueModel> tasks) {
        final ImmutableIssueListJson.Builder builder = ImmutableIssueListJson.builder();
        for(IssueModel model : tasks) {
            builder.addTasks(IssueItemJson.valueOf(model));
        }
        return builder.build();
    }

}
