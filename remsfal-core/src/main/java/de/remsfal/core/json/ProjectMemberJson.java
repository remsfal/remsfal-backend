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
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "Project member information in context of a project")
@JsonDeserialize(as = ImmutableProjectMemberJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ProjectMemberJson implements ProjectMemberModel {

    @Null
    @Nullable
    @Override
    public abstract String getId();

    @Null
    @Nullable
    @Override
    public abstract String getName();

    @Null(groups = PatchValidation.class)
    @Email(groups = PostValidation.class)
    @NotBlank(groups = PostValidation.class)
    @Size(groups = PostValidation.class, max = 255, message = "The email cannot be longer than 255 characters")
    @Nullable
    @Override
    public abstract String getEmail();

    @Null
    @Nullable
    @Override
    public abstract Boolean isActive();

    @NotNull
    @Override
    public abstract MemberRole getRole();

    public static ProjectMemberJson valueOf(final ProjectMemberModel model) {
        return ImmutableProjectMemberJson.builder()
            .id(model.getId())
            .name(model.getName())
            .email(model.getEmail())
            .isActive(model.isActive())
            .role(model.getRole())
            .build();
    }
}
