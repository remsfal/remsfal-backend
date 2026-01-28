package de.remsfal.core.json.project;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.project.ProjectMemberModel;
import de.remsfal.core.model.project.ProjectOrganizationModel;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "Organization assignment to a project")
@JsonDeserialize(as = ImmutableProjectOrganizationJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ProjectOrganizationJson implements ProjectOrganizationModel {

    @NotNull(groups = PostValidation.class)
    @Null(groups = PatchValidation.class)
    @Nullable
    @Override
    public abstract UUID getOrganizationId();

    @Null
    @Nullable
    @Override
    public abstract String getOrganizationName();

    @NotNull
    @Override
    public abstract ProjectMemberModel.MemberRole getRole();

    public static ProjectOrganizationJson valueOf(final ProjectOrganizationModel model) {
        return ImmutableProjectOrganizationJson.builder()
            .organizationId(model.getOrganizationId())
            .organizationName(model.getOrganizationName())
            .role(model.getRole())
            .build();
    }
}
