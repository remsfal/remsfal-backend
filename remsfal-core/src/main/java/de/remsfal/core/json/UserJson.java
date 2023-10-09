package de.remsfal.core.json;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.model.CustomerModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "User information globally")
@JsonDeserialize(as = ImmutableUserJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class UserJson implements CustomerModel {

    @Null
    @Nullable
    public abstract String getId();
    
    @Nullable
    @Size(min = 3, max = 99, message = "The name must be between 3 and 99 characters")
    public abstract String getName();

    @Email
    @Size(max = 255, message = "The email cannot be longer than 255 characters")
    public abstract String getEmail();

    @Nullable
    @PastOrPresent
    public abstract LocalDate getRegisteredDate();
    
    @Nullable
    @PastOrPresent
    public abstract LocalDateTime getLastLoginDate();


    public static UserJson valueOf(final CustomerModel model) {
        return ImmutableUserJson.builder()
            .id(model.getId())
            .name(model.getName())
            .email(model.getEmail())
            .registeredDate(model.getRegisteredDate())
            .lastLoginDate(model.getLastLoginDate())
            .build();
    }
}
