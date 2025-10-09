package de.remsfal.core.json.ticketing;

import java.util.List;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.TaskModel;

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
@Schema(description = "A list of tasks")
@JsonDeserialize(as = ImmutableTaskListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class TaskListJson {
    // Validation is not required, because it is read-only.

    public abstract List<TaskItemJson> getTasks();

    public static TaskListJson valueOf(final List<? extends TaskModel> tasks) {
        final ImmutableTaskListJson.Builder builder = ImmutableTaskListJson.builder();
        for(TaskModel model : tasks) {
            builder.addTasks(TaskItemJson.valueOf(model));
        }
        return builder.build();
    }

}
