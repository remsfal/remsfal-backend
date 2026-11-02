package de.remsfal.core.json.notification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.remsfal.core.ImmutableStyle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

@Value.Immutable
@ImmutableStyle
@Schema(description = "Firebase Installation ID oder FCM-Token eines Geräts für Push-Benachrichtigungen")
@JsonDeserialize(as = ImmutablePushSubscriptionJson.class)
public abstract class PushSubscriptionJson {

    @NotBlank
    @Schema(description = "Firebase Installation ID oder FCM-Registrierungstoken", required = true)
    public abstract String getFidToken();


    @NotBlank
    @Pattern(regexp = "WEB|ANDROID|IOS")
    @Schema(description = "Plattform des Geräts", required = true,
            enumeration = { "WEB", "ANDROID", "IOS" })
    public abstract String getPlatform();
}
