package de.remsfal.core.json;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

/**
 * Server-to-server notification sent by Sign in with Apple.
 */
@Immutable
@ImmutableStyle
@Schema(description = "Server-to-server notification of Sign in with Apple")
@JsonDeserialize(as = ImmutableAppleNotificationJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class AppleNotificationJson {

    @NotNull
    @NotBlank
    @Schema(description = "JWT signed by Apple that carries the event in its events claim")
    public abstract String getPayload();

}
