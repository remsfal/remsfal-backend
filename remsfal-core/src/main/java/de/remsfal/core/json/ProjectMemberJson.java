package de.remsfal.core.json;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.ProjectMemberModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "Project member information in context of a project")
@JsonDeserialize(as = ImmutableProjectMemberJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class ProjectMemberJson implements ProjectMemberModel {

    @Nullable
    public abstract String getId();
    
    @Null
    @Nullable
    public abstract String getName();

    @Email
    @NotBlank
    @Size(max = 255, message = "The email cannot be longer than 255 characters")
    public abstract String getEmail();

    @NotNull
    public abstract UserRole getRole();

    public static ProjectMemberJson valueOf(final ProjectMemberModel model) {
        return ImmutableProjectMemberJson.builder()
            .id(model.getId())
            .role(model.getRole())
            .name(model.getName())
            .email(model.getEmail())
            .build();
    }
}
