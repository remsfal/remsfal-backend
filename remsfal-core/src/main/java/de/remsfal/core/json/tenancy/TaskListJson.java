package de.remsfal.core.json.tenancy;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.ticketing.IssueModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A list of tasks from a tenant's perspective")
@JsonDeserialize(as = ImmutableTaskListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class TaskListJson {
    // Validation is not required, because it is read-only for tenants.

    public abstract List<TaskItemJson> getTasks();

    public static TaskListJson valueOf(final List<? extends IssueModel> tasks) {
        final ImmutableTaskListJson.Builder builder = ImmutableTaskListJson.builder();
        for(IssueModel model : tasks) {
            builder.addTasks(TaskItemJson.valueOf(model));
        }
        return builder.build();
    }

}
