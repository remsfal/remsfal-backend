package de.remsfal.core.dto;

import java.time.LocalDate;

import jakarta.annotation.Nullable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.UserModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "Security check to perform")
@JsonDeserialize(as = ImmutableUserJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class UserJson implements UserModel {

    @Null
    @Nullable
    public abstract String getId();

    @Size(min = 3, max = 99, message = "The name must be between 3 and 99 characters")
    @JsonProperty("user_name")
    public abstract String getName();

    @Size(max = 255, message = "The email cannot be longer than 255 characters")
    @Email(message = "Email should be valid")
    @JsonProperty("user_email")
    public abstract String getEmail();

    @Null
    @Nullable
    @PastOrPresent
    @JsonProperty("registered_date")
    public abstract LocalDate getRegisteredDate();

    public static UserJson valueOf(final UserModel model) {
        return ImmutableUserJson.builder()
            .id(model.getId())
            .name(model.getName())
            .email(model.getEmail())
            .build();
    }
}
