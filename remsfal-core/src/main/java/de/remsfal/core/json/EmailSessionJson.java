package de.remsfal.core.json;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

/**
 * Email address and one-time code to start a session via the passwordless email login.
 */
@Immutable
@ImmutableStyle
@Schema(description = "Email address and the one-time login code sent to it")
@JsonDeserialize(as = ImmutableEmailSessionJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class EmailSessionJson {

    @NotBlank
    @Email
    @Size(max = 254, message = "The email cannot be longer than 254 characters")
    @Schema(required = true, examples = {"user@example.com"})
    public abstract String getEmail();

    @NotBlank
    @Pattern(regexp = "^\\d{8}$", message = "The code must consist of 8 digits")
    @Schema(required = true, examples = {"12345678"})
    public abstract String getCode();

}
