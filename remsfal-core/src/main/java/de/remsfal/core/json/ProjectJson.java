package de.remsfal.core.json;

import jakarta.annotation.Nullable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

import java.util.Set;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.ProjectMemberModel;
import de.remsfal.core.model.ProjectModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A project")
@JsonDeserialize(as = ImmutableProjectJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class ProjectJson implements ProjectModel {

    @Null
    @Nullable
    public abstract String getId();

    @NotNull
    @NotBlank
    @Size(min = 1, max = 99, message = "The title must be between 1 and 255 characters")
    public abstract String getTitle();

    @Null
    @Nullable
    public abstract Set<ProjectMemberJson> getMembers();

    public static ProjectJson valueOf(final ProjectModel model) {
        final ImmutableProjectJson.Builder builder = ImmutableProjectJson.builder()
            .id(model.getId())
            .title(model.getTitle());
        for(ProjectMemberModel member : model.getMembers()) {
            builder.addMembers(ProjectMemberJson.valueOf(member));
        }
        return builder.build();
    }

}
