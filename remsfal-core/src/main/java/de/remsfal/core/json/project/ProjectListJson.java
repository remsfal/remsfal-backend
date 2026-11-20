package de.remsfal.core.json.project;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.remsfal.core.model.project.ProjectMemberModel.MemberRole;
import de.remsfal.core.model.project.ProjectModel;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;

/**
 * A list of projects
 */
@Immutable
@ImmutableStyle
@Schema(description = "A list of projects")
@JsonDeserialize(as = ImmutableProjectListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ProjectListJson {

    @Schema(description = "Index of the first element in projects list of total available entries, starting at 1",
        readOnly = true, required = true, examples = "1")
    public abstract Integer getFirst();

    @Schema(description = "Number of elements in projects list", minimum = "1", maximum = "100",
        readOnly = true, defaultValue = "10", required = true)
    public abstract Integer getSize();

    @Schema(description = "Total number of available projects", readOnly = true, required = true)
    public abstract Long getTotal();

    public abstract List<ProjectItemJson> getProjects();

    /**
     * Create a project list.
     *
     * @param projects  the projects of the current page
     * @param first     index of the first element
     * @param total     total number of accessible projects
     * @param roles     effective member role of the user per project id
     * @return the JSON representation
     */
    public static ProjectListJson valueOf(final List<ProjectModel> projects,
        final int first, final long total, final Map<UUID, MemberRole> roles) {

        final ImmutableProjectListJson.Builder builder = ImmutableProjectListJson.builder();
        for (ProjectModel model : projects) {
            final MemberRole role = roles.get(model.getId());
            if (role != null) {
                builder.addProjects(ProjectItemJson.valueOf(model, role));
            }
        }
        return builder
            .size(projects.size())
            .first(first)
            .total(total)
            .build();
    }
}
