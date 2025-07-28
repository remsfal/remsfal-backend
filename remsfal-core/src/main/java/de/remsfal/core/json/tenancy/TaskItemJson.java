package de.remsfal.core.json.tenancy;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.Status;
import de.remsfal.core.model.ticketing.IssueModel.Type;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A task item with basic information from a tenant's perspective")
@JsonDeserialize(as = ImmutableTaskItemJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class TaskItemJson {
    // Validation is not required, because it is read-only for tenants.

    public abstract String getId();

    public abstract String getName();

    public abstract String getTitle();

    public abstract Type getType();

    public abstract Status getStatus();

    public static TaskItemJson valueOf(final IssueModel model) {
        return ImmutableTaskItemJson.builder()
            .id(model.getId())
            .name(model.getTitle())
            .title(model.getTitle())
            .type(model.getType())
            .status(model.getStatus())
            .build();
    }

}
