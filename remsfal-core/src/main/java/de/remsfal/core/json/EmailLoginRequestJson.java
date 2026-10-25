package de.remsfal.core.json;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

/**
 * Request of a one-time code for the passwordless email login.
 */
@Immutable
@ImmutableStyle
@Schema(description = "Request of a one-time login code that is sent to the given email address")
@JsonDeserialize(as = ImmutableEmailLoginRequestJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class EmailLoginRequestJson {

    @NotBlank
    @Email
    @Size(max = 254, message = "The email cannot be longer than 254 characters")
    @Schema(required = true, examples = {"user@example.com"})
    public abstract String getEmail();

    @Nullable
    @Size(max = 2, message = "Locale must be 2 characters long")
    @Schema(description = "Language of the login email if the address is not registered yet", examples = {"de"})
    public abstract String getLocale();

}
