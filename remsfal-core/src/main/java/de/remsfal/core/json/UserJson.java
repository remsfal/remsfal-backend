package de.remsfal.core.json;

import java.time.LocalDate;
import java.time.LocalDateTime;

import de.remsfal.core.validation.PostValidation;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.CustomerModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "User information globally")
@JsonDeserialize(as = ImmutableUserJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class UserJson implements CustomerModel {

    @Null(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract String getId();

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
    @PastOrPresent
    @Override
    public abstract LocalDate getRegisteredDate();

    @Nullable
    @PastOrPresent
    @Override
    public abstract LocalDateTime getLastLoginDate();

    public static UserJson valueOf(final CustomerModel model) {
        return ImmutableUserJson.builder()
            .id(model.getId())
            .email(model.getEmail())
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
}
