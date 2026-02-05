package de.remsfal.core.json.organization;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.OrganizationEmployeeModel;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;
import java.util.UUID;

@Immutable
@ImmutableStyle
@Schema(description = "Employee information in context of an organization")
@JsonDeserialize(as = ImmutableOrganizationEmployeeJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class OrganizationEmployeeJson implements OrganizationEmployeeModel {

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract UUID getId();

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
    public abstract EmployeeRole getEmployeeRole();

    public static OrganizationEmployeeJson valueOf(final OrganizationEmployeeModel model) {
        return ImmutableOrganizationEmployeeJson.builder()
                .id(model.getId())
                .name(model.getName())
                .email(model.getEmail())
                .active(model.isActive())
                .employeeRole(model.getEmployeeRole())
                .build();
    }
}
