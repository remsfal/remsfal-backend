package de.remsfal.core.json.project;

import java.time.LocalDate;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.AddressJson;
import de.remsfal.core.model.project.TenantModel;
import de.remsfal.core.validation.PostValidation;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * JSON representation of a tenant in a rental agreement.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "Tenant information in a rental agreement")
@JsonDeserialize(as = ImmutableTenantJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class TenantJson implements TenantModel {

    @Null(groups = PostValidation.class)
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract UUID getId();

    @NotBlank(groups = PostValidation.class, message = "First name is required")
    @Nullable
    @Size(min = 1, max = 255, message = "First name must be between 1 and 255 characters")
    @Schema(examples = {"Max"}, required = true)
    @Override
    public abstract String getFirstName();

    @NotBlank(groups = PostValidation.class, message = "Last name is required")
    @Nullable
    @Size(min = 1, max = 255, message = "Last name must be between 1 and 255 characters")
    @Schema(examples = {"Mustermann"}, required = true)
    @Override
    public abstract String getLastName();

    @Email(message = "Must be a valid email address")
    @Nullable
    @Size(max = 255, message = "Email cannot be longer than 255 characters")
    @Schema(examples = {"tenant@example.com"})
    @Override
    public abstract String getEmail();

    @Nullable
    @Pattern(regexp = "^\\+[1-9]\\d{4,14}$", message = "The number must match the E.164 formatted phone numbers")
    @Schema(examples = {"+491234567890"})
    @Override
    public abstract String getMobilePhoneNumber();

    @Nullable
    @Pattern(regexp = "^\\+[1-9]\\d{4,14}$", message = "The number must match the E.164 formatted phone numbers")
    @Schema(examples = {"+491234567890"})
    @Override
    public abstract String getBusinessPhoneNumber();

    @Nullable
    @Pattern(regexp = "^\\+[1-9]\\d{4,14}$", message = "The number must match the E.164 formatted phone numbers")
    @Schema(examples = {"+491234567890"})
    @Override
    public abstract String getPrivatePhoneNumber();

    @Valid
    @Nullable
    @Override
    public abstract AddressJson getAddress();

    @Nullable
    @Size(max = 255, message = "Place of birth cannot be longer than 255 characters")
    @Schema(examples = {"Berlin"})
    @Override
    public abstract String getPlaceOfBirth();

    @Nullable
    @Schema(examples = {"1990-01-01"})
    @Override
    public abstract LocalDate getDateOfBirth();

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract UUID getUserId();

    public static ImmutableTenantJson valueOf(final TenantModel model) {
        if (model == null) {
            return null;
        }
        return ImmutableTenantJson.builder()
                .id(model.getId())
                .firstName(model.getFirstName())
                .lastName(model.getLastName())
                .email(model.getEmail())
                .mobilePhoneNumber(model.getMobilePhoneNumber())
                .businessPhoneNumber(model.getBusinessPhoneNumber())
                .privatePhoneNumber(model.getPrivatePhoneNumber())
                .address(AddressJson.valueOf(model.getAddress()))
                .placeOfBirth(model.getPlaceOfBirth())
                .dateOfBirth(model.getDateOfBirth())
                .userId(model.getUserId())
                .build();
    }
}
