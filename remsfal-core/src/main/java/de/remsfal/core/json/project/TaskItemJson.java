package de.remsfal.core.json.project;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.remsfal.core.model.project.TaskModel;
import de.remsfal.core.model.project.TaskModel.Status;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A task item with basic information")
@JsonDeserialize(as = ImmutableTaskItemJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class TaskItemJson {

    @NotNull
    public abstract String getId();

    @NotNull
    public abstract String getName();

    @NotNull
    public abstract Status getStatus();

    @NotNull
    public abstract String getOwner();

    public static TaskItemJson valueOf(TaskModel model) {
        return ImmutableTaskItemJson.builder()
            .id(model.getId())
            .name(model.getTitle())
            .status(model.getStatus())
            .owner(model.getOwnerId())
            .build();
    }

}
