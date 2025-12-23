package de.remsfal.core.json;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.OrganizationModel;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;
import java.util.UUID;

/**
 * JSON representation of an organization
 */
@Value.Immutable
@ImmutableStyle
@Schema(description = "An organization")
@JsonDeserialize(as = ImmutableOrganizationJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class OrganizationJson implements OrganizationModel {

    @Null(groups = PostValidation.class)
    @NotNull(groups = PatchValidation.class)
    @Override
    public abstract UUID getId();

    @NotBlank(groups = PostValidation.class)
    @Size(max = 255)
    @Override
    public abstract String getName();

    @Pattern(regexp = "^\\+[1-9]\\d{4,14}$", message = "Phone number must be in E.164 format")
    @Size(max = 15)
    @Override
    public abstract String getPhone();

    @Email
    @Size(max = 255)
    @Override
    public abstract String getEmail();

    @Size(max = 255)
    @Override
    public abstract String getTrade();

    @Nullable
    @Valid
    @Override
    public abstract AddressJson getAddress();

    /**
     * Create a JSON representation from a model.
     *
     * @param model the model
     * @return the JSON representation
     */
    public static OrganizationJson valueOf(final OrganizationModel model) {
        if (model == null) {
            return null;
        }

        final ImmutableOrganizationJson.Builder builder = ImmutableOrganizationJson.builder()
            .id(model.getId())
            .name(model.getName())
            .phone(model.getPhone())
            .email(model.getEmail())
            .trade(model.getTrade());

        if (model.getAddress() != null) {
            builder.address(AddressJson.valueOf(model.getAddress()));
        }

        return builder.build();
    }
}
