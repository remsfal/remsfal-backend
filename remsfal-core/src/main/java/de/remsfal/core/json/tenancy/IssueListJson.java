package de.remsfal.core.json.tenancy;

import java.util.List;

import de.remsfal.core.model.ticketing.IssueModel;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A list of issues from a tenant's perspective")
@JsonDeserialize(as = ImmutableIssueListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class IssueListJson {
    // Validation is not required, because it is read-only for tenants.

    public abstract List<IssueItemJson> getIssues();

    public static IssueListJson valueOf(final List<? extends IssueModel> issues) {
        final ImmutableIssueListJson.Builder builder = ImmutableIssueListJson.builder();
        for(IssueModel model : issues) {
            builder.addIssues(IssueItemJson.valueOf(model));
        }
        return builder.build();
    }

}
