package de.remsfal.core.json.project;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.project.ProjectMemberModel.MemberRole;
import de.remsfal.core.model.project.ProjectModel;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A project item with the user's member role only")
@JsonDeserialize(as = ImmutableProjectJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ProjectItemJson {

    @NotNull
    @Schema(readOnly = true)
    public abstract UUID getId();

    @NotNull
    public abstract String getName();

    @NotNull
    public abstract MemberRole getMemberRole();

    /**
     * Create a project item with the effective role of the user in the project.
     *
     * @param model      the project
     * @param memberRole the effective role of the user (direct or derived from an organization)
     * @return the JSON representation
     */
    public static ProjectItemJson valueOf(final ProjectModel model, final MemberRole memberRole) {
        return ImmutableProjectItemJson.builder()
            .id(model.getId())
            .name(model.getTitle())
            .memberRole(memberRole)
            .build();
    }

}
