package de.remsfal.core.json;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ProjectMemberModel;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;

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
@Schema(description = "A list of project members")
@JsonDeserialize(as = ImmutableProjectMemberListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ProjectMemberListJson {

    @NotNull
    public abstract List<ProjectMemberJson> getMembers();

    public static ProjectMemberListJson valueOfSet(Set<? extends ProjectMemberModel> models) {
        final ImmutableProjectMemberListJson.Builder builder = ImmutableProjectMemberListJson.builder();
        for (ProjectMemberModel model : models) {
            builder.addMembers(ProjectMemberJson.valueOf(model));
        }
        return builder.build();
    }

}
