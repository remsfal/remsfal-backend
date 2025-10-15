package de.remsfal.core.json;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import de.remsfal.core.validation.PostValidation;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.CustomerModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "User information globally")
@JsonDeserialize(as = ImmutableUserJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class UserJson implements CustomerModel {

    public enum UserRole {
        MANAGER,   // Verwalter
        TENANT,    // Mieter
        CONTRACTOR // Auftragnehmer
    }

    @Null(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract UUID getId();

    @Null
    @Nullable
    public abstract Set<UserRole> getUserRoles();

    @Email
    @Nullable
    @Size(max = 255, message = "The email cannot be longer than 255 characters")
    @Override
    public abstract String getEmail();

    @JsonIgnore
    @Null
    @Nullable
    @Override
    public abstract String getName();

    @Nullable
    @Size(min = 3, max = 255, message = "The name must be between 3 and 255 characters")
    @Override
    public abstract String getFirstName();

    @Nullable
    @Size(min = 3, max = 255, message = "The name must be between 3 and 255 characters")
    @Override
    public abstract String getLastName();

    @Valid
    @Nullable
    @Override
    public abstract AddressJson getAddress();

    @Nullable
    @Override
    @Pattern(regexp = "^\\+[1-9]\\d{4,14}$", message = "The number must match the E.164 formatted phone numbers")
    public abstract String getMobilePhoneNumber();

    @Nullable
    @Override
    @Pattern(regexp = "^\\+[1-9]\\d{4,14}$", message = "The number must match the E.164 formatted phone numbers")
    public abstract String getBusinessPhoneNumber();

    @Nullable
    @Override
    @Pattern(regexp = "^\\+[1-9]\\d{4,14}$", message = "The number must match the E.164 formatted phone numbers")
    public abstract String getPrivatePhoneNumber();

    @Nullable
    @Null
    @Override
    public abstract LocalDate getRegisteredDate();

    @Nullable
    @Null
    @Override
    public abstract LocalDateTime getLastLoginDate();

    public static ImmutableUserJson valueOf(final CustomerModel model) {
        return ImmutableUserJson.builder()
            .id(model.getId())
            .email(model.getEmail())
            .active(model.isActive())
            .firstName(model.getFirstName())
            .lastName(model.getLastName())
            .address(AddressJson.valueOf(model.getAddress()))
            .mobilePhoneNumber(model.getMobilePhoneNumber())
            .businessPhoneNumber(model.getBusinessPhoneNumber())
            .privatePhoneNumber(model.getPrivatePhoneNumber())
            .registeredDate(model.getRegisteredDate())
            .lastLoginDate(model.getLastLoginDate())
            .build();
    }

    public static UserJson valueOf(final CustomerModel model, final Set<UserRole> userRoles) {
        return UserJson.valueOf(model)
            .withUserRoles(userRoles);
    }

}
