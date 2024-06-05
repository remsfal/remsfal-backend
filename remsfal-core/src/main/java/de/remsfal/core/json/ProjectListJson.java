package de.remsfal.core.json;

import java.util.List;

import de.remsfal.core.model.UserModel;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.ProjectModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A list of projects")
@JsonDeserialize(as = ImmutableProjectListJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class ProjectListJson {

    @Schema(description = "Index of the first element in projects list of total available entries, starting at 1",
        required = true, example = "1")
    public abstract Integer getFirst();

    @Schema(description = "Number of elements in projects list", minimum = "1", maximum = "100",
        defaultValue = "10", required = true)
    public abstract Integer getSize();

    @Schema(description = "Total number of available projects", required = true)
    public abstract Long getTotal();

    public abstract List<ProjectItemJson> getProjects();

    public static ProjectListJson valueOf(final List<ProjectModel> projects, final int first, final long total, final UserModel user) {
        final ImmutableProjectListJson.Builder builder = ImmutableProjectListJson.builder();
        for(ProjectModel model : projects) {
            builder.addProjects(ProjectItemJson.valueOf(model, user));
        }
        return builder
            .size(projects.size())
            .first(first)
            .total(total)
            .build();
    }

}
